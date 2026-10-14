package com.atlaes.learnpilot

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.atlaes.learnpilot", appContext.packageName)
    }

    @Test
    fun launcherResolvesToMainActivity() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val launchIntent = appContext.packageManager.getLaunchIntentForPackage(appContext.packageName)
        assertNotNull("The installed app must expose a launcher activity", launchIntent)
        val component = launchIntent!!.resolveActivity(appContext.packageManager)
        assertNotNull(component)
        assertEquals("com.atlaes.learnpilot", component!!.packageName)
        assertEquals("com.atlaes.learnpilot.MainActivity", component.className)
    }
}
