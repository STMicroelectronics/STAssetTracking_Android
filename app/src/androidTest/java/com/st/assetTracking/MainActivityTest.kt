package com.st.assetTracking

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.st.assetTracking.blueTile.BlueTileMainActivity
import com.st.assetTracking.sigfox.SigfoxTrackerConfig
import com.st.assetTracking.util.clickOnViewChild
import com.st.assetTracking.util.onRecycleItemWithText


@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    var mActivityTestRule = IntentsTestRule(MainActivity::class.java)

    @Test
    fun startSigfoxConfiguration() {
        onView(withId(R.id.main_package_list))
                .perform(onRecycleItemWithText(R.string.package_sifgox_name, click()))
        intended(hasComponent(SigfoxTrackerConfig::class.java.name))
    }

    @Test
    fun startBlueTileConfiguration() {
        onView(withId(R.id.main_package_list))
                .perform(onRecycleItemWithText(R.string.package_bluetile_name, click()))
        intended(hasComponent(BlueTileMainActivity::class.java.name))
    }

    @Test
    fun startSmartagApp() {
        onView(withId(R.id.main_package_list))
                .perform(onRecycleItemWithText(R.string.package_smarTag_name, click()))
        intended(toPackage("com.st.smartTag"))
    }

    @Test
    fun moreInfoClickOpenAnExternalSite() {
        onView(withId(R.id.main_package_list))
                .perform(onRecycleItemWithText(R.string.package_smarTag_name,
                        clickOnViewChild(R.id.package_info)))
        intended(hasAction(Intent.ACTION_VIEW))

    }

}
