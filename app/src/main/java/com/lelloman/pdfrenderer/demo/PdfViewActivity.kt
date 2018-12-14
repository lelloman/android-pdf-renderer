package com.lelloman.pdfrenderer.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.lelloman.pdfrenderer.PdfDocumentImpl
import com.lelloman.pdfrenderer.PdfView
import io.reactivex.disposables.CompositeDisposable
import java.io.File

class PdfViewActivity : AppCompatActivity() {

    private val pdfView by lazy { findViewById<View>(R.id.pdfView) as PdfView }

    private val layoutResId: Int
        get() = when (intent.getStringExtra(EXTRA_VIEW_TYPE)) {
            VIEW_TYPE_PAGED -> R.layout.activity_paged
            VIEW_TYPE_SCROLLED -> R.layout.activity_scrolled
            else -> throw IllegalArgumentException("Invalid value set for Intent extra $EXTRA_VIEW_TYPE")
        }

    private val subscriptions = CompositeDisposable()

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
        setContentView(layoutResId)
        pdfView.setPdfDocument(document)
    }

    override fun onStart() {
        super.onStart()
        subscriptions.add(pdfView.visiblePage.subscribe {
            title = "${1 + it}/${document.pageCount}"
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

    companion object {
        private const val EXTRA_VIEW_TYPE = "ViewType"
        private const val VIEW_TYPE_PAGED = "Paged"
        private const val VIEW_TYPE_SCROLLED = "Scrolled"

        fun startPaged(activity: Activity) {
            activity.startActivity(
                Intent(activity, PdfViewActivity::class.java)
                    .putExtra(EXTRA_VIEW_TYPE, VIEW_TYPE_PAGED)
            )
        }

        fun startScrolled(activity: Activity) {
            activity.startActivity(
                Intent(activity, PdfViewActivity::class.java)
                    .putExtra(EXTRA_VIEW_TYPE, VIEW_TYPE_SCROLLED)
            )
        }
    }
}