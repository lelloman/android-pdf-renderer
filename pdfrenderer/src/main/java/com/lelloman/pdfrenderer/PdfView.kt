package com.lelloman.pdfrenderer

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.MainThread
import com.lelloman.pdfrenderer.internal.InternalPdfView
import com.lelloman.pdfrenderer.internal.PagedPdfView
import com.lelloman.pdfrenderer.internal.ScrolledPdfView
import io.reactivex.Flowable

class PdfView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var document = PdfDocument.STUB
        @MainThread
        set(value) {
            if (field == value) return
            field = value
            activePdfView.setPdfDocument(value)
        }

    var style: PdfViewStyle = PdfViewStyle.PAGED
        @MainThread
        set(value) {
            if (value == field) return
            field = value
            onStyleChanged()
        }

    var orientation = PdfViewOrientation.HORIZONTAL
        @MainThread
        set(value) {
            if (value == field) return
            field = value
            scrolled.orientation = value
            paged.orientation = value
        }

    var isReversed = false
        @MainThread
        set(value) {
            if (value == field) return
            field = value
            scrolled.isReversed = value
            paged.isReversed = value
        }

    private val paged by lazy {
        PagedPdfView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            orientation = this@PdfView.orientation
            visibility = GONE
        }
    }

    private val scrolled by lazy {
        ScrolledPdfView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            orientation = this@PdfView.orientation
            visibility = GONE
        }
    }

    val visiblePage: Flowable<Int> = Flowable
        .merge(
            paged.visiblePage.filter { style == PdfViewStyle.PAGED },
            scrolled.visiblePage.filter { style == PdfViewStyle.SCROLLED }
        )
        .distinctUntilChanged()

    private var activePdfView: InternalPdfView = paged
    private val activeView get() = activePdfView as View

    init {
        var styleFromAttrs: PdfViewStyle? = null
        var orientationFromAttrs: PdfViewOrientation? = null
        var isReversedFromAttrs: Boolean? = null

        context.theme.obtainStyledAttributes(
            attrs, R.styleable.PdfView, 0, 0
        ).apply {
            try {
                styleFromAttrs = getInt(R.styleable.PdfView_pdfViewStyle, -1).let { attrValue ->
                    PdfViewStyle.values().firstOrNull { it.attrValue == attrValue }
                }
                orientationFromAttrs = getInt(R.styleable.PdfView_pdfViewOrientation, -1).let { attrValue ->
                    PdfViewOrientation.values().firstOrNull { it.attrValue == attrValue }
                }
                isReversedFromAttrs = if (hasValue(R.styleable.PdfView_pdfViewIsReversed)) {
                    getBoolean(R.styleable.PdfView_pdfViewIsReversed, false)
                } else {
                    null
                }
            } finally {
                recycle()
            }
        }

        styleFromAttrs?.let { style = it }
        orientationFromAttrs?.let { orientation = it }
        isReversedFromAttrs?.let { isReversed = it }

        addView(paged)
        addView(scrolled)
        onStyleChanged()
    }

    @MainThread
    fun showPage(pageIndex: Int) {
        activePdfView.showPage(pageIndex)
    }

    private fun onStyleChanged() {
        val visiblePage = activePdfView.visiblePage.blockingFirst()
        activeView.visibility = GONE
        activePdfView.setPdfDocument(PdfDocument.STUB)

        activePdfView = when (style) {
            PdfViewStyle.PAGED -> paged
            PdfViewStyle.SCROLLED -> scrolled
        }
        activeView.visibility = View.VISIBLE
        activePdfView.setPdfDocument(document)
        activePdfView.showPage(visiblePage)
    }
}