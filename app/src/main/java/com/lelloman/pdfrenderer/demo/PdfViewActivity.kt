package com.lelloman.pdfrenderer.demo

import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.lelloman.pdfrenderer.FullPdfView
import com.lelloman.pdfrenderer.PdfDocumentImpl
import com.lelloman.pdfrenderer.PdfView
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import java.io.File

class PdfViewActivity : AppCompatActivity() {

    private val pdfView by lazy { findViewById<FullPdfView>(R.id.pdfView) }
    private val subscriptions = CompositeDisposable()

    private val document by lazy {
        val pdfFile = File(filesDir, "ppp.pdf")
        if (pdfFile.exists()) pdfFile.delete()
        if (!pdfFile.exists()) {
            assets.open("test.pdf").buffered().copyTo(pdfFile.outputStream())
        }
        PdfDocumentImpl(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY))
    }

    private val toolbarController by lazy { ToolbarController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_pdf)
        pdfView.setPdfDocument(document)

    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.actionHorizontal -> {
            pdfView.orientation = PdfView.Orientation.HORIZONTAL
            invalidateOptionsMenu()
            true
        }
        R.id.actionVertical -> {
            pdfView.orientation = PdfView.Orientation.VERTICAL
            invalidateOptionsMenu()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        subscriptions.add(pdfView.visiblePage.subscribe {
            title = "${1 + it}/${document.pageCount}"
        })
        subscriptions.add(
            toolbarController
                .orientation
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    pdfView.orientation = it
                }
        )
    }

    override fun onStop() {
        super.onStop()
        subscriptions.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        document.dispose()
    }

    private inner class ToolbarController {

        private val verticalImageView = findViewById<View>(R.id.imageViewVertical)
        private val horizontalImageView = findViewById<View>(R.id.imageViewHorizontal)

        private val orientationSubject = BehaviorSubject
            .createDefault(this@PdfViewActivity.pdfView.orientation)

        val orientation: Flowable<PdfView.Orientation> = orientationSubject
            .hide()
            .toFlowable(BackpressureStrategy.LATEST)
            .distinctUntilChanged()
            .doOnNext {
                verticalImageView.alpha = if (it == PdfView.Orientation.VERTICAL) 1f else .4f
                horizontalImageView.alpha = if (it == PdfView.Orientation.HORIZONTAL) 1f else .4f
            }

        init {
            verticalImageView.setOnClickListener { orientationSubject.onNext(PdfView.Orientation.VERTICAL) }
            horizontalImageView.setOnClickListener { orientationSubject.onNext(PdfView.Orientation.HORIZONTAL) }
        }
    }
}