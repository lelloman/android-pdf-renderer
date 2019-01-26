package com.lelloman.pdfrenderer.demo

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.lelloman.pdfrenderer.PdfViewOrientation
import com.lelloman.pdfrenderer.PdfViewStyle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
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
