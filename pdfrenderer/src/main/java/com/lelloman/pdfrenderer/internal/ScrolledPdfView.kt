package com.lelloman.pdfrenderer.internal

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.lelloman.pdfrenderer.PdfDocument
import com.lelloman.pdfrenderer.PdfViewOrientation
import com.lelloman.pdfrenderer.R
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.*
import java.util.concurrent.TimeUnit

internal class ScrolledPdfView(context: Context) : RecyclerView(context), InternalPdfView {

    override var orientation = PdfViewOrientation.VERTICAL
        set(value) {
            field = value
            linearLayoutManger.orientation = value.asLayoutManagerValue()
        }

    private var pdfDocument: PdfDocument? = null

    private val layoutInflater = LayoutInflater.from(context)

    private val visiblePageSubject = BehaviorSubject.createDefault(0)

    override val visiblePage: Flowable<Int> = visiblePageSubject
        .hide()
        .toFlowable(BackpressureStrategy.LATEST)
        .distinctUntilChanged()

    private var targetSize = BehaviorSubject.createDefault(Size(0, 0))

    private val bitmapCache = LinkedList<Bitmap>()

    private val adapterImpl = object : Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = layoutInflater
            .inflate(R.layout.pdf_view_item, parent, false)
            .let { ViewHolder(it) }

        override fun getItemCount() = pdfDocument?.pageCount ?: 0

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bind(position)
        }

        override fun onViewRecycled(holder: ViewHolder) {
            super.onViewRecycled(holder)
            holder.unBind()
        }
    }

    private val linearLayoutManger = LinearLayoutManager(context, orientation.asLayoutManagerValue(), false)

    init {
        layoutManager = linearLayoutManger
        adapter = adapterImpl
    }

    override fun showPage(pageIndex: Int) {
        visiblePageSubject.onNext(pageIndex)
        linearLayoutManger.scrollToPosition(pageIndex)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        val pageCount = pdfDocument?.pageCount ?: 0
        val item = if (pageCount < 1) {
            0
        } else {
            val (offset, edgeLength) = when (orientation) {
                PdfViewOrientation.VERTICAL -> {
                    computeVerticalScrollOffset() to height
                }
                PdfViewOrientation.HORIZONTAL -> {
                    computeHorizontalScrollOffset() to width
                }
            }
            (edgeLength / 2 + offset) / edgeLength
        }
        visiblePageSubject.onNext(item)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        targetSize.onNext(Size(w, h))
    }

    override fun setPdfDocument(pdfDocument: PdfDocument) {
        this.pdfDocument = pdfDocument
        adapterImpl.notifyDataSetChanged()
    }

    private fun obtainBitmap(size: Size): Bitmap {
        var bitmap: Bitmap? = null
        synchronized(bitmapCache) {
            while (bitmapCache.isNotEmpty() && bitmap == null) {
                val cachedBitmap = bitmapCache.removeFirst()
                if (cachedBitmap.width != size.width || cachedBitmap.height != size.height) {
                    cachedBitmap.recycle()
                } else {
                    bitmap = cachedBitmap
                }
            }
        }

        return bitmap ?: Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
    }

    private fun cacheBitmap(bitmap: Bitmap) {
        Completable
            .fromAction {
                bitmap.eraseColor(0)
                synchronized(bitmapCache) {
                    bitmapCache.add(bitmap)
                }
            }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    private fun PdfViewOrientation.asLayoutManagerValue() = when (this) {
        PdfViewOrientation.HORIZONTAL -> LinearLayoutManager.HORIZONTAL
        PdfViewOrientation.VERTICAL -> LinearLayoutManager.VERTICAL
    }

    private class Size(val width: Int, val height: Int) {
        val hasSurface = width > 0 && height > 0
    }

    internal inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val imageView = view.findViewById<ImageView>(R.id.imageView)
        private val progressBar = view.findViewById<View>(R.id.progressBar)

        private var itemPosition = -1

        private val subscriptions = CompositeDisposable()

        internal fun bind(position: Int) {
            itemPosition = position
            val subscription = targetSize
                .filter(Size::hasSurface)
                .delay(250, TimeUnit.MILLISECONDS)
                .map {
                    obtainBitmap(it).apply { pdfDocument?.render(this, itemPosition) }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    imageView.setImageBitmap(it)
                    imageView.tag = it
                    imageView.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
            subscriptions.add(subscription)
        }

        internal fun unBind() {
            subscriptions.clear()
            imageView.setImageBitmap(null)
            (imageView.tag as? Bitmap)?.let(::cacheBitmap)
            imageView.tag = null
            imageView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        }
    }
}