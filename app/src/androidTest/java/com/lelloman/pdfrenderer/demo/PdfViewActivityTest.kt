package com.lelloman.pdfrenderer.demo

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.lelloman.instrumentedtestutils.ViewActions.swipeLeft
import com.lelloman.instrumentedtestutils.ViewActions.swipeRight
import com.lelloman.instrumentedtestutils.checkMatches
import com.lelloman.instrumentedtestutils.nonNullAny
import com.lelloman.instrumentedtestutils.viewWithId
import com.lelloman.pdfrenderer.PdfDocument
import com.lelloman.pdfrenderer.PdfViewOrientation
import com.lelloman.pdfrenderer.PdfViewStyle
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class PdfViewActivityTest {

    private open class SypablePdfDocument(pdfDocument: PdfDocument) : PdfDocument by pdfDocument

    @get:Rule
    val activityTestRule = ActivityTestRule(PdfViewActivity::class.java, true, false)

    private lateinit var pdfDocument: PdfDocument

    @Before
    fun setUp() {
        val originalProvider = DemoApplication.pdfDocumentProvider
        DemoApplication.pdfDocumentProvider = {
            val pdfDocumentProvider = originalProvider(it)
            object : PdfDocumentProvider {
                override fun providePdfDocument(): PdfDocument =
                    spy(SypablePdfDocument(pdfDocumentProvider.providePdfDocument())).apply { pdfDocument = this }
            }
        }
        activityTestRule.launchActivity(null)
    }

    @Test
    fun swipesPagesWithPagedStyle() {
        assertPdfViewSettings(PdfViewStyle.PAGED, PdfViewOrientation.HORIZONTAL, false)

        assertPageNumberShown(1)
        swipeRight(R.id.pdfView)
        assertPageNumberShown(2)
        swipeRight(R.id.pdfView)
        assertPageNumberShown(3)
        swipeLeft(R.id.pdfView)
        assertPageNumberShown(2)
        swipeLeft(R.id.pdfView)
        assertPageNumberShown(1)

        (0 until 5).forEach {
            verify(pdfDocument).render(nonNullAny(), eq(it))
        }
        reset(pdfDocument)
        clickOnIsReversed()

        assertPdfViewSettings(PdfViewStyle.PAGED, PdfViewOrientation.HORIZONTAL, true)

        assertPageNumberShown(1)
        swipeLeft(R.id.pdfView)
        assertPageNumberShown(2)
        swipeLeft(R.id.pdfView)
        assertPageNumberShown(3)
        swipeRight(R.id.pdfView)
        assertPageNumberShown(2)
        swipeRight(R.id.pdfView)
        assertPageNumberShown(1)

        verify(pdfDocument).render(nonNullAny(), eq(3))
        verify(pdfDocument).render(nonNullAny(), eq(4))
    }

    @Test
    fun swipesPagesWithScrolledStyle() {
        assertPdfViewSettings(PdfViewStyle.PAGED, PdfViewOrientation.HORIZONTAL, false)
        clickOnScrolledStyled()
        assertPdfViewSettings(PdfViewStyle.SCROLLED, PdfViewOrientation.HORIZONTAL, false)

        assertPageNumberShown(1)
        swipeRight(R.id.pdfView)
        assertPageNumberShown(2)
        swipeRight(R.id.pdfView)
        assertPageNumberShown(3)
        swipeLeft(R.id.pdfView)
        assertPageNumberShown(2)
        swipeLeft(R.id.pdfView)
        assertPageNumberShown(1)

        clickOnIsReversed()
        assertPdfViewSettings(PdfViewStyle.SCROLLED, PdfViewOrientation.HORIZONTAL, true)

        swipeLeft(R.id.pdfView)
        assertPageNumberShown(2)
        swipeLeft(R.id.pdfView)
        assertPageNumberShown(3)
    }

    private fun clickOnIsReversed() {
        onView(withId(R.id.toggleButtonIsReversed)).perform(click())
    }

    private fun clickOnScrolledStyled() {
        onView(withId(R.id.textViewScrolled)).perform(click())
    }

    private fun assertPdfViewSettings(style: PdfViewStyle, orientation: PdfViewOrientation, isReversed: Boolean) {
        viewWithId(R.id.pdfView).apply {
            checkMatches(pdfViewHasOrientation(orientation))
            checkMatches(pdfViewHasStyle(style))
            checkMatches(pdfViewIsReversed(isReversed))
        }
    }

    private fun assertPageNumberShown(pageNumber: Int) {
        val text = "$pageNumber/$TEST_PDF_PAGE_COUNT"
        viewWithId(R.id.textViewPageNumber).checkMatches(withText(text))
    }

    private companion object {
        const val TEST_PDF_PAGE_COUNT = 10
    }
}