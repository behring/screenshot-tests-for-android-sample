package com.thoughtworks.mp.screenshot_tests_for_android_sample

import android.content.Context
import android.view.LayoutInflater
import androidx.test.platform.app.InstrumentationRegistry
import com.facebook.litho.LithoView
import com.facebook.testing.screenshot.Screenshot
import com.facebook.testing.screenshot.ViewHelpers
import org.junit.Test


class MainActivityUITest {
    @Test
    fun testDefault() {
        val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val inflater = LayoutInflater.from(targetContext)
        val view = inflater.inflate(R.layout.activity_main, null, false)
        ViewHelpers.setupView(view).setExactWidthDp(300).layout()
        Screenshot.snap(view).record()
    }
}