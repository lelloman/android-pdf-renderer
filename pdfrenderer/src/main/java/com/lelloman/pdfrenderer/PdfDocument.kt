package com.lelloman.pdfrenderer

import android.graphics.Bitmap

interface PdfDocument {
    val pageCount: Int

    fun render(bitmap: Bitmap, pageIndex: Int)

    fun dispose()

    companion object {
        val STUB = object : PdfDocument {
            override val pageCount = 0
            override fun render(bitmap: Bitmap, pageIndex: Int) = Unit
            override fun dispose() = Unit
        }
    }
}