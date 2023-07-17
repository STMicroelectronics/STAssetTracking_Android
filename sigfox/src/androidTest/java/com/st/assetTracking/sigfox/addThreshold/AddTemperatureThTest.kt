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
class AddTemperatureThTest {

    companion object{
        const val VALUE_RANGE = "[-20.0 … 100.0]"
        val SENSOR_NAME = R.string.sensor_temperature_name
    }

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(SigfoxTrackerConfig::class.java)

    @Test
    fun addTemperatureLessThreshold(){
        checkSensorLessThanInsertion(SENSOR_NAME,"12", "< 12.0 °C")
    }

    @Test
    fun addTemperatureBiggerThreshold(){
        checkSensorBiggerInsertion(SENSOR_NAME,"13", "≥ 13.0 °C")
    }

    @Test
    fun addTemperatureThresholdBelowMinShowError_1(){
        checkOutOfRangeError(SENSOR_NAME,"-20.1", VALUE_RANGE)

    }

    @Test
    fun addTemperatureThresholdBelowMinShowError_2(){
        checkOutOfRangeError(SENSOR_NAME,"-100",VALUE_RANGE )
    }


    @Test
    fun addTemperatureThresholdAboveMaxShowError_1(){
        checkOutOfRangeError(SENSOR_NAME,"100.1",VALUE_RANGE )
    }

    @Test
    fun addTemperatureThresholdAboveMaxShowError_2(){
        checkOutOfRangeError(SENSOR_NAME,"1000",VALUE_RANGE )
    }



}
