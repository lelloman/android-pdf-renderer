package com.lelloman.pdfrenderer.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
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
        binding.lifecycleOwner = this
    }
}