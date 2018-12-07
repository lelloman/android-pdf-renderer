package com.lelloman.pdfrenderer

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.IOException
import java.lang.Math.*

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
        val pageRatio = page.width / page.height.toFloat()
        val bitmapRatio = bitmap.width / bitmap.height.toFloat()
        val (destWidth, destHeight) = if(pageRatio > bitmapRatio) {
            bitmap.width to round(bitmap.width / pageRatio)
        } else {
            round(bitmap.height * pageRatio) to bitmap.height
        }
        val horizontalMargin = abs(bitmap.width - destWidth) / 2
        val verticalMargin = abs(bitmap.height - destHeight) / 2

        val destClip = Rect(
            horizontalMargin,
            verticalMargin,
            max(bitmap.width, destWidth) - horizontalMargin,
            max(bitmap.height, destHeight) - verticalMargin
        )
        page.render(bitmap, destClip, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
    }

    override fun dispose() {
        pdfRenderer.close()
    }
}