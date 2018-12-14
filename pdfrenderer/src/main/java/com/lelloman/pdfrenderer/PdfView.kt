package com.lelloman.pdfrenderer

import io.reactivex.Flowable

interface PdfView {

    var orientation: Orientation

    val visiblePage: Flowable<Int>

    fun setPdfDocument(pdfDocument: PdfDocument)

    enum class Orientation(val attrValue: Int) {
        HORIZONTAL(0),
        VERTICAL(1);
    }
}