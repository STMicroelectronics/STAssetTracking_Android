package com.st.assetTracking.sigfox.addThreshold

import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.st.assetTracking.sigfox.R
import com.st.assetTracking.sigfox.SigfoxTrackerConfig
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class AddWakeUpThTest {

    companion object{
        const val VALUE_RANGE = "[1.0 … 16.0]"
        val SENSOR_NAME = R.string.sensor_wakeUp_name
    }

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(SigfoxTrackerConfig::class.java)

    @Test
    fun addWakeUpThreshold(){
        showThDialogForSensor(SENSOR_NAME)

        insertThreshold("2.0", R.id.addWakeUpThreshold_threshold_value)

        onView(withId(R.id.addWakeUpThreshold_addButton)).perform(ViewActions.click())

        checkThreshold(SENSOR_NAME,"≥ 2.0 g")
    }

    private fun checkOutOfRangeError(@StringRes sensorName:Int, thValue:String, expectedErrorRange:String){
        showThDialogForSensor(sensorName)

        insertThreshold(thValue, R.id.addWakeUpThreshold_threshold_value)

        checkErrorMessageContains(expectedErrorRange,R.id.addWakeUpThreshold_threshold_layout)
    }

    @Test
    fun addThresholdBelowMinShowError_1(){
        checkOutOfRangeError(SENSOR_NAME,"0.1", VALUE_RANGE)

    }


    @Test
    fun addThresholdAboveMaxShowError_1(){
        checkOutOfRangeError(SENSOR_NAME,"16.1",VALUE_RANGE )
    }

    @Test
    fun addThresholdAboveMaxShowError_2(){
        checkOutOfRangeError(SENSOR_NAME,"100",VALUE_RANGE )
    }



}
