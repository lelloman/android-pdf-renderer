package com.lelloman.pdfrenderer.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.ToggleButton
import com.lelloman.pdfrenderer.PdfDocumentFactory
import com.lelloman.pdfrenderer.PdfView
import com.lelloman.pdfrenderer.PdfViewOrientation
import com.lelloman.pdfrenderer.PdfViewStyle
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import java.io.File

class ControlsViewPdfViewActivity : AppCompatActivity() {

    private val pdfView by lazy { findViewById<PdfView>(R.id.pdfView) }
    private val subscriptions = CompositeDisposable()

    private val document by lazy {
        val pdfFile = File(filesDir, "ppp.pdf")
        if (pdfFile.exists()) pdfFile.delete()
        if (!pdfFile.exists()) {
            assets.open("test.pdf").buffered().copyTo(pdfFile.outputStream())
        }
        PdfDocumentFactory.make(
            ParcelFileDescriptor.open(
                pdfFile,
                ParcelFileDescriptor.MODE_READ_ONLY
            )
        )
    }

    private val toolbarController by lazy { ToolbarController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_controls_view_pdf_view)
        window.decorView.keepScreenOn = true
        pdfView.document = document

    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.actionHorizontal -> {
            pdfView.orientation = PdfViewOrientation.HORIZONTAL
            invalidateOptionsMenu()
            true
        }
        R.id.actionVertical -> {
            pdfView.orientation = PdfViewOrientation.VERTICAL
            invalidateOptionsMenu()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        subscriptions.add(pdfView.visiblePage.subscribe {
            toolbarController.setCurrentPageIndex(it)
        })
        subscriptions.add(
            toolbarController
                .orientation
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    pdfView.orientation = it
                }
        )
        subscriptions.add(
            toolbarController
                .style
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    pdfView.style = it
                }
        )
        subscriptions.add(toolbarController
            .isReversed
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                pdfView.isReversed = it
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

        private val pageNumberTextView = findViewById<TextView>(R.id.textViewPageNumber)

        private val verticalImageView = findViewById<View>(R.id.imageViewVertical)
        private val horizontalImageView = findViewById<View>(R.id.imageViewHorizontal)

        private val scrolledTextView = findViewById<View>(R.id.textViewScrolled)
        private val pagedTextView = findViewById<View>(R.id.textViewPaged)

        private val isReversedToggleButton = findViewById<ToggleButton>(R.id.toggleButtonIsReversed)

        private val orientationSubject = BehaviorSubject
            .createDefault(this@ControlsViewPdfViewActivity.pdfView.orientation)

        private val styleSubject = BehaviorSubject
            .createDefault(this@ControlsViewPdfViewActivity.pdfView.style)

        private val isReversedSubject = BehaviorSubject
            .createDefault(this@ControlsViewPdfViewActivity.pdfView.isReversed)

        val orientation: Flowable<PdfViewOrientation> = orientationSubject
            .hide()
            .toFlowable(BackpressureStrategy.LATEST)
            .distinctUntilChanged()
            .doOnNext {
                verticalImageView.alpha = if (it == PdfViewOrientation.VERTICAL) 1f else .4f
                horizontalImageView.alpha = if (it == PdfViewOrientation.HORIZONTAL) 1f else .4f
            }

        val style: Flowable<PdfViewStyle> = styleSubject
            .hide()
            .toFlowable(BackpressureStrategy.LATEST)
            .distinctUntilChanged()
            .doOnNext {
                pagedTextView.alpha = if (it == PdfViewStyle.PAGED) 1f else .4f
                scrolledTextView.alpha = if (it == PdfViewStyle.SCROLLED) 1f else .4f
            }

        val isReversed: Flowable<Boolean> = isReversedSubject
            .hide()
            .toFlowable(BackpressureStrategy.LATEST)
            .distinctUntilChanged()
            .doOnNext {
                isReversedToggleButton.isChecked = it
            }

        init {
            verticalImageView.setOnClickListener { orientationSubject.onNext(PdfViewOrientation.VERTICAL) }
            horizontalImageView.setOnClickListener { orientationSubject.onNext(PdfViewOrientation.HORIZONTAL) }
            pagedTextView.setOnClickListener { styleSubject.onNext(PdfViewStyle.PAGED) }
            scrolledTextView.setOnClickListener { styleSubject.onNext(PdfViewStyle.SCROLLED) }
            isReversedToggleButton.setOnCheckedChangeListener { buttonView, isChecked ->
                isReversedSubject.onNext(isChecked)
            }
        }

        @SuppressLint("SetTextI18n")
        fun setCurrentPageIndex(pageIndex: Int) {
            pageNumberTextView.text = "${pageIndex + 1}/${document.pageCount}"
        }
    }
}