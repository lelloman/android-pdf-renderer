package com.lelloman.pdfrenderer

import android.os.ParcelFileDescriptor
import com.lelloman.pdfrenderer.internal.PdfDocumentImpl

object PdfDocumentFactory {

    fun make(parcelFileDescriptor: ParcelFileDescriptor): PdfDocument =
        PdfDocumentImpl(parcelFileDescriptor)
}