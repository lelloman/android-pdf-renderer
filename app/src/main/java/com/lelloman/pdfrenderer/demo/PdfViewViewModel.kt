package com.lelloman.pdfrenderer.demo

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.lelloman.pdfrenderer.PdfDocument
import com.lelloman.pdfrenderer.PdfViewOrientation
import com.lelloman.pdfrenderer.PdfViewStyle
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class PdfViewViewModel(
    pdfDocumentProvider: PdfDocumentProvider
) : ViewModel() {

    private val mutableOrientation = MutableLiveData<PdfViewOrientation>().apply {
        value = PdfViewOrientation.HORIZONTAL
    }
    private val mutableIsReversed = MutableLiveData<Boolean>().apply {
        value = false
    }
    private val mutableStyle = MutableLiveData<PdfViewStyle>().apply {
        value = PdfViewStyle.PAGED
    }

    private val mutableCurrentPageString = MutableLiveData<String>().apply {
        value = ""
    }

    private val mutablePdfDocument = MutableLiveData<PdfDocument>().apply {
        value = PdfDocument.STUB
    }

    val orientation: LiveData<PdfViewOrientation> = mutableOrientation
    val isReversed: LiveData<Boolean> = mutableIsReversed
    val style: LiveData<PdfViewStyle> = mutableStyle

    val pdfDocument: LiveData<PdfDocument> = mutablePdfDocument

    val verticalOrientationTextViewAlpha: LiveData<Float> = Transformations.map(orientation) {
        if (it == PdfViewOrientation.VERTICAL) ALPHA_SELECTED else ALPHA_NOT_SELECTED
    }

    val horizontalOrientationTextViewAlpha: LiveData<Float> = Transformations.map(orientation) {
        if (it == PdfViewOrientation.HORIZONTAL) ALPHA_SELECTED else ALPHA_NOT_SELECTED
    }

    val scrolledStyleTextViewAlpha: LiveData<Float> = Transformations.map(style) {
        if (it == PdfViewStyle.SCROLLED) ALPHA_SELECTED else ALPHA_NOT_SELECTED
    }

    val pagedStyleTextViewAlpha: LiveData<Float> = Transformations.map(style) {
        if (it == PdfViewStyle.PAGED) ALPHA_SELECTED else ALPHA_NOT_SELECTED
    }

    val currentPageString: LiveData<String> = mutableCurrentPageString

    private var visiblePageChangeSubscription: Disposable? = null

    init {
        mutablePdfDocument.postValue(pdfDocumentProvider.providePdfDocument())
    }

    fun onVerticalOrientationClicked() {
        this.mutableOrientation.postValue(PdfViewOrientation.VERTICAL)
    }

    fun onHorizontalOrientationClicked() {
        this.mutableOrientation.postValue(PdfViewOrientation.HORIZONTAL)
    }

    fun onPagedStyleClicked() {
        this.mutableStyle.postValue(PdfViewStyle.PAGED)
    }

    fun onScrolledStyleClicked() {
        this.mutableStyle.postValue(PdfViewStyle.SCROLLED)
    }

    fun onIsReversedChanged(toggled: Boolean) {
        mutableIsReversed.postValue(toggled)
    }

    fun observeVisiblePageChanges(visiblePageChanges: Flowable<Int>) {
        visiblePageChangeSubscription?.dispose()
        visiblePageChangeSubscription = visiblePageChanges
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { pageIndex ->
                if (pageIndex != null) {
                    mutableCurrentPageString.postValue("${pageIndex + 1}/${pdfDocument.value?.pageCount ?: 0}")
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        pdfDocument.value?.dispose()
        visiblePageChangeSubscription?.dispose()
    }

    private companion object {
        const val ALPHA_SELECTED = 1.0f
        const val ALPHA_NOT_SELECTED = 0.4f
    }
}