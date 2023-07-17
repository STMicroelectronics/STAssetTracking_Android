package com.st.assetTracking.threshold.view.model

import com.st.assetTracking.threshold.model.Orientation
import com.st.assetTracking.threshold.model.SensorThreshold
import com.st.assetTracking.threshold.model.ThresholdSensorType
import com.st.assetTracking.threshold.model.ThresholdSensorType.*
import com.st.assetTracking.threshold.view.resourceImage
import com.st.assetTracking.threshold.view.resourceString
import com.st.assetTracking.threshold.view.string
import com.st.assetTracking.threshold.view.unitResourceString
import java.util.Locale

private fun SensorThreshold.toEnvironmentalViewData(): EnvironmentalThresholdViewData {
    val valueStr = String.format(Locale.getDefault(), "%.1f", threshold)
    return EnvironmentalThresholdViewData(sensor.resourceImage, sensor.resourceString,
            compareSymbolStr = comparison.string,
            valueStr = valueStr,
            unitStrId = sensor.unitResourceString)
}

private fun SensorThreshold.toOrientationViewData(): OrientationThresholdViewData {
    val event = Orientation.fromRawValue(threshold.toInt().toShort())
    return OrientationThresholdViewData(sensor.resourceImage, sensor.resourceString, event.resourceImage, event.resourceString)
}

private fun SensorThreshold.toTiltViewData(): TiltThresholdViewData {
    return TiltThresholdViewData()
}

fun SensorThreshold.toViewData(): SensorThresholdViewData {
    return when (this.sensor) {
        Humidity,
        Temperature,
        Pressure,
        WakeUp -> {
            toEnvironmentalViewData()
        }
        ThresholdSensorType.Orientation -> {
            toOrientationViewData()
        }
        Tilt -> {
            toTiltViewData()
        }
    }
}