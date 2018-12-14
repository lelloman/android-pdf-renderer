package com.lelloman.pdfrenderer.demo

import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.lelloman.pdfrenderer.PdfDocumentImpl
import com.lelloman.pdfrenderer.PdfView
import io.reactivex.disposables.CompositeDisposable
import java.io.File

class PagedActivity : AppCompatActivity() {

    private val pdfView by lazy { findViewById<View>(R.id.pdfView) as PdfView }
    private val document by lazy {
        val pdfFile = File(filesDir, "ppp.pdf")
        if (pdfFile.exists()) pdfFile.delete()
        if (!pdfFile.exists()) {
            assets.open("test.pdf").buffered().copyTo(pdfFile.outputStream())
        }
        PdfDocumentImpl(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY))
    }

    private val subscriptions = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paged)

        pdfView.setPdfDocument(document)
    }

    override fun onStart() {
        super.onStart()
        subscriptions.add(pdfView.visiblePage.subscribe {
            title = "$it/${document.pageCount}"
        })
    }

    override fun onStop() {
        super.onStop()
        subscriptions.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        document.dispose()
    }
}
