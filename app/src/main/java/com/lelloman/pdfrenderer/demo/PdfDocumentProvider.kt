package com.lelloman.pdfrenderer.demo

import android.content.Context
import android.os.ParcelFileDescriptor
import com.lelloman.pdfrenderer.PdfDocument
import com.lelloman.pdfrenderer.PdfDocumentFactory
import java.io.File

interface PdfDocumentProvider {
    fun providePdfDocument(): PdfDocument
}

class PdfDocumentProviderImpl(
    private val context: Context
) : PdfDocumentProvider {
    override fun providePdfDocument(): PdfDocument {
        return File(context.filesDir, "tmp.pdf")
            .let {
                if (it.exists()) it.delete()
                context.assets.open("test.pdf").buffered().copyTo(it.outputStream())
                PdfDocumentFactory.make(ParcelFileDescriptor.open(it, ParcelFileDescriptor.MODE_READ_ONLY))
            }
    }
}