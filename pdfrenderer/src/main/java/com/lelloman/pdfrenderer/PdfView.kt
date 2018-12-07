package com.lelloman.pdfrenderer

import io.reactivex.Observable

interface PdfView {

    val visiblePage: Observable<Int>

    fun setPdfDocument(pdfDocument: PdfDocument)
}