package com.lelloman.pdfrenderer.internal

import com.lelloman.pdfrenderer.PdfDocument
import com.lelloman.pdfrenderer.PdfViewOrientation
import io.reactivex.Flowable

internal interface InternalPdfView {

    var orientation: PdfViewOrientation

    var isReversed: Boolean

    val visiblePage: Flowable<Int>

    fun setPdfDocument(pdfDocument: PdfDocument)

    fun showPage(pageIndex: Int)

    companion object {
        const val DEFAULT_IS_REVERSED = false
        val DEFAULT_ORIENTATION = PdfViewOrientation.HORIZONTAL
    }
}