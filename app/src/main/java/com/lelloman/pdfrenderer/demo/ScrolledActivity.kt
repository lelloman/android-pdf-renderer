package com.lelloman.pdfrenderer.demo

import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.lelloman.pdfrenderer.PdfDocumentImpl
import com.lelloman.pdfrenderer.PdfView
import java.io.File

class ScrolledActivity : AppCompatActivity() {

    private val pdfView by lazy { findViewById<View>(R.id.pdfView) as PdfView }
    private val document by lazy {
        val pdfFile = File(filesDir, "ppp.pdf")
        if (pdfFile.exists()) pdfFile.delete()
        if (!pdfFile.exists()) {
            assets.open("test.pdf").buffered().copyTo(pdfFile.outputStream())
        }
        PdfDocumentImpl(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolled)
        pdfView.setPdfDocument(document)
    }

    override fun onDestroy() {
        super.onDestroy()
        document.dispose()
    }
}