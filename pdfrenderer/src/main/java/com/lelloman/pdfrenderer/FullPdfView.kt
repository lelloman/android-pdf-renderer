package com.lelloman.pdfrenderer

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import io.reactivex.Flowable

class FullPdfView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs), PdfView {

    private var document = PdfDocument.STUB

    var style: Style = Style.PAGED
        set(value) {
            if (value == field) return
            field = value
            onStyleChanged()
        }

    override var orientation = PdfView.Orientation.VERTICAL
        set(value) {
            field = value
            scrolled.orientation = value
            paged.orientation = value
        }

    private val paged = PagedPdfView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        orientation = this@FullPdfView.orientation
        visibility = GONE
    }

    private val scrolled = ScrolledPdfView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        orientation = this@FullPdfView.orientation
        visibility = GONE
    }

    override val visiblePage: Flowable<Int> = Flowable
        .merge(paged.visiblePage, scrolled.visiblePage)
        .distinctUntilChanged()

    private var activePdfView: PdfView = paged
    private val activeView get() = activePdfView as View

    init {
        addView(paged)
        addView(scrolled)
        onStyleChanged()
    }

    override fun setPdfDocument(pdfDocument: PdfDocument) {
        document = pdfDocument
        activePdfView.setPdfDocument(document)
    }

    override fun showPage(pageIndex: Int) {
        activePdfView.showPage(pageIndex)
    }

    private fun onStyleChanged() {
        val visiblePage = activePdfView.visiblePage.blockingFirst()
        activeView.visibility = GONE
        activePdfView.setPdfDocument(PdfDocument.STUB)

        activePdfView = when (style) {
            Style.PAGED -> paged
            Style.SCROLLED -> scrolled
        }
        activeView.visibility = View.VISIBLE
        activePdfView.setPdfDocument(document)
        activePdfView.showPage(visiblePage)
    }

    enum class Style {
        PAGED, SCROLLED
    }
}