package com.lelloman.pdfrenderer

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.PageTransformer
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.ImageView
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject


class PagedPdfView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewPager(context, attrs), PdfView {

    override var orientation = PdfView.Orientation.HORIZONTAL
        set(value) {
            field = value
            onOrientationSet()
        }

    private var pdfDocument: PdfDocument? = null

    private val layoutInflater = LayoutInflater.from(context)

    private val horizontalPageTransformer = PageTransformer { _, _ -> }

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

    private val adapter = object : PagerAdapter() {

        override fun isViewFromObject(p0: View, p1: Any): Boolean = p0 === p1

        override fun getCount() = pdfDocument?.pageCount ?: 0

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = layoutInflater.inflate(R.layout.pdf_view_item, container, false)

            val imageView = view.findViewById<ImageView>(R.id.imageView)
            val progressBar = view.findViewById<View>(R.id.progressBar)
            val layoutObserver = object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    renderPageIntoImageView(position, view, imageView, progressBar)
                    view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
            view.viewTreeObserver.addOnGlobalLayoutListener(layoutObserver)
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
            container.removeView(item as View)
        }
    }

    private val visiblePageSubject = BehaviorSubject.create<Int>()

    override val visiblePage: Flowable<Int> = visiblePageSubject
        .hide()
        .toFlowable(BackpressureStrategy.LATEST)
        .distinctUntilChanged()

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.PdfView)
            try {
                val orientationInt = a.getInt(R.styleable.PdfView_orientation, orientation.attrValue)
                orientation = PdfView.Orientation.values().first { it.attrValue == orientationInt }
            } catch (exception: Throwable) {

            } finally {
                a.recycle()
            }
        }

        offscreenPageLimit = 2
        setAdapter(adapter)
        addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(pageIndex: Int) {
                visiblePageSubject.onNext(pageIndex)
            }
        })

        onOrientationSet()
    }

    private fun onOrientationSet() {
        val (pageTransformer, motionEventTransformer) = when (orientation) {
            PdfView.Orientation.HORIZONTAL -> {
                horizontalPageTransformer to horizontalMotionEventHandler
            }
            PdfView.Orientation.VERTICAL -> {
                verticalPageTransformer to verticalMotionEventHandler
            }
        }
        setPageTransformer(true, pageTransformer)
        adjustedMotionEvent = motionEventTransformer
    }

    private fun renderPageIntoImageView(pageIndex: Int, container: View, imageView: ImageView, progressBar: View) =
        Single
            .fromCallable {
                Bitmap.createBitmap(container.width, container.height, Bitmap.Config.ARGB_8888).apply {
                    pdfDocument?.render(this, pageIndex)
                }
            }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                imageView.setImageBitmap(it)
                imageView.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }, {
                Log.e(TAG, "Error while creating Bitmap and rendering page", it)
            })

    override fun setPdfDocument(pdfDocument: PdfDocument) {
        this.pdfDocument = pdfDocument
        adapter.notifyDataSetChanged()
        setCurrentItem(0, false)
        visiblePageSubject.onNext(0)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val intercepted = super.onInterceptTouchEvent(adjustedMotionEvent(ev))
        adjustedMotionEvent(ev)
        return intercepted
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return super.onTouchEvent(adjustedMotionEvent(ev))
    }

    private companion object {
        val TAG = PagedPdfView::class.java.simpleName
    }
}