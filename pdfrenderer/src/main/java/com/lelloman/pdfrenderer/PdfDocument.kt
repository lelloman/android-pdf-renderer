package com.lelloman.pdfrenderer

import android.graphics.Bitmap

interface PdfDocument {
    val pageCount: Int

    fun render(bitmap: Bitmap, pageIndex: Int)

    fun dispose()
}