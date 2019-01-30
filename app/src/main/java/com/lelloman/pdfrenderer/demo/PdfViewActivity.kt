package com.lelloman.pdfrenderer.demo

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.lelloman.pdfrenderer.demo.databinding.ActivityPdfViewBinding
import org.koin.android.ext.android.inject

class PdfViewActivity : AppCompatActivity() {

    private val viewModel: PdfViewViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityPdfViewBinding>(
            this,
            R.layout.activity_pdf_view
        )
        window.decorView.keepScreenOn = true
        viewModel.observeVisiblePageChanges(binding.pdfView.visiblePage)
        binding.viewModel = viewModel
        binding.setLifecycleOwner(this)
    }
}