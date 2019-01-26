package com.lelloman.pdfrenderer.internal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.PageTransformer
import android.util.Log
import android.view.*
import android.widget.ImageView
import com.lelloman.pdfrenderer.PdfDocument
import com.lelloman.pdfrenderer.PdfViewOrientation
import com.lelloman.pdfrenderer.R
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject


internal class PagedPdfView(context: Context) : ViewPager(context), InternalPdfView {

    override var orientation = InternalPdfView.DEFAULT_ORIENTATION
        set(value) {
            field = value
            onOrientationSet()
        }

    private var pdfDocument: PdfDocument = PdfDocument.STUB

    private val layoutInflater = LayoutInflater.from(context)

    private val verticalPageTransformer = PageTransformer { view, position ->
        view.translationX = view.width * position * -1
        view.translationY = position * view.height
    }

    private val horizontalMotionEventHandler: (MotionEvent) -> MotionEvent = { it }

    private val verticalMotionEventHandler: (MotionEvent) -> MotionEvent = {
        it.apply {
            val ratio = width.toFloat() / height
            setLocation(y * ratio, x / ratio)
        }
    }

    private var adjustedMotionEvent: (MotionEvent) -> MotionEvent = horizontalMotionEventHandler

    private val viewHolder = mutableSetOf<View>()

    private val adapter = object : PagerAdapter() {

        override fun isViewFromObject(p0: View, p1: Any): Boolean = p0 === p1

        override fun getCount() = pdfDocument.pageCount

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val pdfPageIndex = if (isReversed) {
                (count - 1) - position
            } else {
                position
            }
            val view = layoutInflater.inflate(R.layout.pdf_view_item, container, false)
            viewHolder.add(view)
            val imageView = view.findViewById<ImageView>(R.id.imageView)
            val progressBar = view.findViewById<View>(R.id.progressBar)
            val layoutObserver = object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    renderPageIntoImageView(pdfPageIndex, view, imageView, progressBar)
                    view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
            view.viewTreeObserver.addOnGlobalLayoutListener(layoutObserver)
            container.addView(view)
            view.tag = pdfPageIndex
            return view
        }

        override fun getItemPosition(item: Any): Int {
            val pdfPageIndex = (item as? View)?.tag as? Int ?: return PagerAdapter.POSITION_NONE

            return if(isReversed) {
                (count - 1) - pdfPageIndex
            } else {
                pdfPageIndex
            }
        }

        override fun destroyItem(container: ViewGroup, originalPosition: Int, item: Any) {
            (item as? View)?.let {
                viewHolder.remove(item)
                container.removeView(item)
            } ?: throw IllegalArgumentException("item parameter was expected to be a View, got $item instead.")
        }
    }

    private val visiblePageSubject = BehaviorSubject.createDefault(0)

    override val visiblePage: Flowable<Int> = visiblePageSubject
        .hide()
        .toFlowable(BackpressureStrategy.LATEST)
        .distinctUntilChanged()

    override var isReversed: Boolean = InternalPdfView.DEFAULT_IS_REVERSED
        set(value) {
            field = value
            adapter.notifyDataSetChanged()
        }

    init {
        offscreenPageLimit = 2
        setAdapter(adapter)
        addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(pageIndex: Int) {
                visiblePageSubject.onNext(if(isReversed) {
                    (pdfDocument.pageCount - 1) - pageIndex
                } else {
                    pageIndex
                })
            }
        })

        onOrientationSet()
    }

    override fun showPage(pageIndex: Int) {
        visiblePageSubject.onNext(pageIndex)
        setCurrentItem(pageIndex, false)
    }

    private fun onOrientationSet() {
        val (pageTransformer, motionEventTransformer) = when (orientation) {
            PdfViewOrientation.HORIZONTAL -> {
                // resets translation eventually applied by vertical page transformer
                viewHolder.forEach {
                    it.translationY = 0f
                    it.translationX = 0f
                }
                null to horizontalMotionEventHandler
            }
            PdfViewOrientation.VERTICAL -> {
                verticalPageTransformer to verticalMotionEventHandler
            }
        }
        setPageTransformer(true, pageTransformer)
        adjustedMotionEvent = motionEventTransformer
    }

    private fun renderPageIntoImageView(pageIndex: Int, container: View, imageView: ImageView, progressBar: View) =
        Single
            .just(container.width to container.height)
            .filter { it.first > 0 && it.second > 0 }
            .map {
                Bitmap.createBitmap(container.width, container.height, Bitmap.Config.ARGB_8888).apply {
                    pdfDocument.render(this, pageIndex)
                }
            }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                imageView.setImageBitmap(it)
                imageView.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }, {
                Log.e(PagedPdfView::class.java.simpleName, "Error while creating Bitmap and rendering page", it)
            })

    override fun setPdfDocument(pdfDocument: PdfDocument) {
        this.pdfDocument = pdfDocument
        adapter.notifyDataSetChanged()
        if (pdfDocument.pageCount > 0) {
            setCurrentItem(0, false)
            visiblePageSubject.onNext(0)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val intercepted = super.onInterceptTouchEvent(adjustedMotionEvent(ev))
        adjustedMotionEvent(ev)
        return intercepted
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return super.onTouchEvent(adjustedMotionEvent(ev))
    }
}