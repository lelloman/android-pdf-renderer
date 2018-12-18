package com.lelloman.pdfrenderer

import android.os.ParcelFileDescriptor
import com.lelloman.pdfrenderer.internal.PdfDocumentImpl
import java.io.File

object PdfDocumentFactory {

    fun make(parcelFileDescriptor: ParcelFileDescriptor): PdfDocument =
        PdfDocumentImpl(parcelFileDescriptor)

    fun make(file: File) : PdfDocument = ParcelFileDescriptor
        .open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        .let(::PdfDocumentImpl)
}