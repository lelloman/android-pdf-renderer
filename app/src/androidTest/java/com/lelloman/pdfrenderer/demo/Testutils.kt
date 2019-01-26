package com.lelloman.pdfrenderer.demo

import android.support.test.espresso.matcher.BoundedMatcher
import android.view.View
import com.lelloman.pdfrenderer.PdfView
import com.lelloman.pdfrenderer.PdfViewOrientation
import com.lelloman.pdfrenderer.PdfViewStyle
import org.hamcrest.Description


fun pdfViewHasOrientation(orientation: PdfViewOrientation): BoundedMatcher<View, PdfView> =
    object : BoundedMatcher<View, PdfView>(PdfView::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText("Given View is a PdfView with orientation set to $orientation")
        }

        override fun matchesSafely(item: PdfView?) = item?.orientation == orientation
    }

fun pdfViewHasStyle(style: PdfViewStyle): BoundedMatcher<View, PdfView> =
    object : BoundedMatcher<View, PdfView>(PdfView::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText("Given View is a PdfView with style set to $style")
        }

        override fun matchesSafely(item: PdfView?) = item?.style == style
    }

fun pdfViewIsReversed(isReversed: Boolean): BoundedMatcher<View, PdfView> =
    object : BoundedMatcher<View, PdfView>(PdfView::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText("Given View is a PdfView with isReversed set to $isReversed")
        }

        override fun matchesSafely(item: PdfView?) = item?.isReversed == isReversed
    }