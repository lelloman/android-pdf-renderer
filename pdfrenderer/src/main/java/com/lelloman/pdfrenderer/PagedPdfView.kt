package com.lelloman.pdfrenderer

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.*

class PagedPdfView(context: Context, attrs: AttributeSet) : ViewPager(context, attrs), PdfView {

    private var pdfDocument: PdfDocument? = null

    private val layoutInflater = LayoutInflater.from(context)

    private val adapter = object : PagerAdapter() {

        override fun isViewFromObject(p0: View, p1: Any): Boolean = p0 === p1

        override fun getCount() = pdfDocument?.pageCount ?: 0

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = layoutInflater.inflate(R.layout.paged_pdf_view_item, container, false)

            val imageView = view.findViewById<ImageView>(R.id.pageImageView)
            val layoutObserver = object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val bitmap = Bitmap.createBitmap(imageView.width, imageView.height, Bitmap.Config.ARGB_8888)
                    pdfDocument?.render(bitmap, position)
                    imageView.setImageBitmap(bitmap)
                    imageView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
            imageView.viewTreeObserver.addOnGlobalLayoutListener(layoutObserver)
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
            container.removeView(item as View)
        }
    }

    private val visiblePageSubject = BehaviorSubject.create<Int>()

    override val visiblePage: Observable<Int> = visiblePageSubject.hide().distinctUntilChanged()

    init {
        setAdapter(adapter)
        addOnPageChangeListener(object : OnPageChangeListener{
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(pageIndex: Int) {
                visiblePageSubject.onNext(pageIndex)
            }
        })
    }

    override fun setPdfDocument(pdfDocument: PdfDocument) {
        this.pdfDocument = pdfDocument
        adapter.notifyDataSetChanged()
        setCurrentItem(0, false)
        visiblePageSubject.onNext(0)
    }
}