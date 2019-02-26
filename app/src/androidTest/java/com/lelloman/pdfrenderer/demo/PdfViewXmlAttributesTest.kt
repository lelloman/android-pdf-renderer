package com.lelloman.pdfrenderer.demo

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import com.lelloman.pdfrenderer.PdfViewOrientation
import com.lelloman.pdfrenderer.PdfViewStyle
import org.junit.Rule
import org.junit.Test


class PdfViewXmlAttributesTest {

    @get:Rule
    val activityTestRule = ActivityTestRule(StaticAttrsPdfViewActivity::class.java, true, false)

    private fun launchActivity(
        isReversed: Boolean,
        style: PdfViewStyle,
        orientation: PdfViewOrientation
    ) = StaticAttrsPdfViewActivity
        .makeStartIntent(
            isReversed = isReversed,
            style = style,
            orientation = orientation
        )
        .let(activityTestRule::launchActivity)

    private fun finishActivity() = activityTestRule.finishActivity()

    @Test
    fun testAllStaticAttributesCombinations() {
        listOf(true, false).forEach { isReversed ->
            PdfViewStyle.values().forEach { style ->
                PdfViewOrientation.values().forEach { orientation ->
                    launchActivity(
                        isReversed = isReversed,
                        style = style,
                        orientation = orientation
                    )

                    onView(withId(R.id.pdfView)).apply {
                        check(matches(pdfViewIsReversed(isReversed)))
                        check(matches(pdfViewHasStyle(style)))
                        check(matches(pdfViewHasOrientation(orientation)))
                    }

                    finishActivity()
                }
            }
        }
    }
}
