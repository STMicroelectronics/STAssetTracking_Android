package com.st.assetTracking.sigfox.addThreshold

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
class AddHumidityThTest {

    companion object{
        const val VALUE_RANGE = "[0.0 … 100.0]"
        val SENSOR_NAME = R.string.sensor_humidity_name
    }

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(SigfoxTrackerConfig::class.java)

    @Test
    fun addLessThanThreshold(){
        checkSensorLessThanInsertion(SENSOR_NAME,"50", "< 50.0 %RH")
    }

    @Test
    fun addBiggerThanThreshold(){
        checkSensorBiggerInsertion(SENSOR_NAME,"20.3", "≥ 20.3 %RH")
    }

    @Test
    fun addThresholdBelowMinShowError_1(){
        checkOutOfRangeError(SENSOR_NAME,"-0.1", VALUE_RANGE)

    }

    @Test
    fun addThresholdBelowMinShowError_2(){
        checkOutOfRangeError(SENSOR_NAME,"-10",VALUE_RANGE )
    }


    @Test
    fun addThresholdAboveMaxShowError_1(){
        checkOutOfRangeError(SENSOR_NAME,"100.1",VALUE_RANGE )
    }

    @Test
    fun addThresholdAboveMaxShowError_2(){
        checkOutOfRangeError(SENSOR_NAME,"200",VALUE_RANGE )
    }



}
