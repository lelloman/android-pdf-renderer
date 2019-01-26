package com.lelloman.pdfrenderer.demo

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.lelloman.pdfrenderer.demo.databinding.ActivityPdfViewBinding
import java.io.File

class PdfViewActivity : AppCompatActivity() {

    private val documentFile by lazy {
        File(filesDir, "tmp.pdf").apply {
            if (exists()) delete()
            assets.open("test.pdf").buffered().copyTo(outputStream())
        }
    }

    lateinit var viewModel: PdfViewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        val binding = DataBindingUtil.setContentView<ActivityPdfViewBinding>(
            this,
            R.layout.activity_pdf_view
        )
        window.decorView.keepScreenOn = true
        viewModel.loadPdfDocument(documentFile)
        viewModel.observeVisiblePageChanges(binding.pdfView.visiblePage)
        binding.viewModel = viewModel
        binding.setLifecycleOwner(this)
    }
}