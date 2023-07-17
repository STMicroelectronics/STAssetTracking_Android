package com.st.nfcSmarTag.v2.util

import com.st.nfcSmarTag.R
import com.st.smartaglibrary.v2.catalog.VirtualSensor

fun mapSensorTypeToImage(catalogVS: VirtualSensor): Int{
    return when (catalogVS.type) {
        "battery_percentage" -> R.drawable.battery_percentage
        "battery_voltage" -> R.drawable.battery_voltage
        "tem" -> R.drawable.sensor_temperature_icon
        "pre" -> R.drawable.sensor_pressure_icon
        "hum" -> R.drawable.sensor_humidity_icon
        "imu_acc" -> R.drawable.sensor_wake_up_icon
        "6d_acc" -> R.drawable.sensor_acc_event_orientation
        "tilt_acc" -> R.drawable.sensor_acc_event_tilt
        "acc/gyro" -> R.drawable.sensor_acc_gyro
        else -> { R.drawable.sensor_generic }
    }
}