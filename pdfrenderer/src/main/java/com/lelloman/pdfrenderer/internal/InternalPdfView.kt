package com.lelloman.pdfrenderer.internal

import com.lelloman.pdfrenderer.PdfDocument
import com.lelloman.pdfrenderer.PdfViewOrientation
import io.reactivex.Flowable

internal interface InternalPdfView {

    var orientation: PdfViewOrientation

    val visiblePage: Flowable<Int>

    fun setPdfDocument(pdfDocument: PdfDocument)

    fun showPage(pageIndex: Int)
}