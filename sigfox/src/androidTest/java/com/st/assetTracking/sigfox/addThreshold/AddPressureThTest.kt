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
class AddPressureThTest {

    companion object{
        const val VALUE_RANGE = "[500.0 … 1260.0]"
        val SENSOR_NAME = R.string.sensor_pressure_name
    }

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(SigfoxTrackerConfig::class.java)

    @Test
    fun addLessThanThreshold(){
        checkSensorLessThanInsertion(SENSOR_NAME,"1000", "< 1000.0 mbar")
    }

    @Test
    fun addBiggerThanThreshold(){
        checkSensorBiggerInsertion(SENSOR_NAME,"1000", "≥ 1000.0 mbar")
    }

    @Test
    fun addThresholdBelowMinShowError_1(){
        checkOutOfRangeError(SENSOR_NAME,"499.9", VALUE_RANGE)

    }

    @Test
    fun addThresholdBelowMinShowError_2(){
        checkOutOfRangeError(SENSOR_NAME,"0",VALUE_RANGE )
    }


    @Test
    fun addThresholdAboveMaxShowError_1(){
        checkOutOfRangeError(SENSOR_NAME,"1260.1",VALUE_RANGE )
    }

    @Test
    fun addThresholdAboveMaxShowError_2(){
        checkOutOfRangeError(SENSOR_NAME,"2000",VALUE_RANGE )
    }



}
