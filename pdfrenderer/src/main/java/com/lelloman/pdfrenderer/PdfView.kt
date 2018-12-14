package com.lelloman.pdfrenderer

import io.reactivex.Observable

interface PdfView {

    var orientation: Orientation

    val visiblePage: Observable<Int>

    fun setPdfDocument(pdfDocument: PdfDocument)

    enum class Orientation(val attrValue: Int) {
        HORIZONTAL(0),
        VERTICAL(1);
    }
}