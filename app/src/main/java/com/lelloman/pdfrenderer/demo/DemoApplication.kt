package com.lelloman.pdfrenderer.demo

import android.app.Application
import android.content.Context
import org.koin.android.ext.android.startKoin
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

class DemoApplication : Application() {

    private val appModule = module {

        factory { pdfDocumentProvider(get()) }

        viewModel { PdfViewViewModel(get()) }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(appModule))
    }

    companion object {
        var pdfDocumentProvider: (context: Context) -> PdfDocumentProvider = ::PdfDocumentProviderImpl
    }
}