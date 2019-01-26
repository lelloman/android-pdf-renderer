package com.lelloman.pdfrenderer.demo

import android.content.Intent
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.support.v7.app.AppCompatActivity
import com.lelloman.pdfrenderer.PdfDocumentFactory
import com.lelloman.pdfrenderer.PdfView
import com.lelloman.pdfrenderer.PdfViewOrientation
import com.lelloman.pdfrenderer.PdfViewStyle
import java.io.File

class StaticAttrsPdfViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.keepScreenOn = true

        val isReversed = intent?.getBooleanExtra(EXTRA_IS_REVERSED, DEFAULT_IS_REVERSED) ?: DEFAULT_IS_REVERSED
        val style = intent?.getStringExtra(EXTRA_STYLE)?.let(PdfViewStyle::valueOf) ?: DEFAULT_STYLE
        val orientation =
            intent?.getStringExtra(EXTRA_ORIENTATION)?.let(PdfViewOrientation::valueOf) ?: DEFAULT_ORIENTATION

        val isReversedSuffix = if (isReversed) "_reversed" else ""
        val styleString = when (style) {
            PdfViewStyle.PAGED -> "paged"
            PdfViewStyle.SCROLLED -> "scrolled"
        }
        val orientationString = when (orientation) {
            PdfViewOrientation.HORIZONTAL -> "horizontal"
            PdfViewOrientation.VERTICAL -> "vertical"
        }

        val layoutName = "activity_${styleString}_$orientationString$isReversedSuffix"
        val layoutId = resources.getIdentifier(layoutName, "layout", this.packageName)
        setContentView(layoutId)

        val pdfView = findViewById<PdfView>(R.id.pdfView)
        val pdfFile = File(filesDir, "ppp.pdf")
        if (pdfFile.exists()) pdfFile.delete()

        assets.open("test.pdf").buffered().copyTo(pdfFile.outputStream())

        pdfView.document = PdfDocumentFactory.make(
            ParcelFileDescriptor.open(
                pdfFile,
                ParcelFileDescriptor.MODE_READ_ONLY
            )
        )
    }

    companion object {

        private const val DEFAULT_IS_REVERSED = false
        private val DEFAULT_STYLE = PdfViewStyle.PAGED
        private val DEFAULT_ORIENTATION = PdfViewOrientation.HORIZONTAL

        private const val EXTRA_IS_REVERSED = "IsReversed"
        private const val EXTRA_STYLE = "Style"
        private const val EXTRA_ORIENTATION = "Orientation"

        fun makeStartIntent(
            isReversed: Boolean = DEFAULT_IS_REVERSED,
            style: PdfViewStyle = DEFAULT_STYLE,
            orientation: PdfViewOrientation = DEFAULT_ORIENTATION
        ) = Intent().apply {
            putExtra(EXTRA_IS_REVERSED, isReversed)
            putExtra(EXTRA_STYLE, style.name)
            putExtra(EXTRA_ORIENTATION, orientation.name)
        }
    }
}