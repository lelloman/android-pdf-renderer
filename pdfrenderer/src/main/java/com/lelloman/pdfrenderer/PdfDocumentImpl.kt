package com.lelloman.pdfrenderer

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.IOException

class PdfDocumentImpl(parcelFileDescriptor: ParcelFileDescriptor) : PdfDocument {

    private val pdfRenderer: PdfRenderer

    override val pageCount get() = pdfRenderer.pageCount

    init {
        try {
            pdfRenderer = PdfRenderer(parcelFileDescriptor)
        } catch (exception: IOException) {
            TODO("rethrow something")
        }
    }

    override fun render(bitmap: Bitmap, pageIndex: Int) {
        val page = pdfRenderer.openPage(pageIndex)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
    }

    override fun dispose() {
        pdfRenderer.close()
    }
}