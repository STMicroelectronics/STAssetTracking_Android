package com.st.assetTracking.threshold.model

/**
 * Valid range for the different type of sensor
 */
enum class ThresholdSensorType(val range: ClosedRange<Float>) {
    Temperature(-20.0f..100.0f),
    Pressure(500.0f..1260.0f),
    Humidity(0.0f..100.0f),
    WakeUp(1.0f..16.0f),
    Tilt(0.0f..1.0f),
    Orientation(0.0f..6.0f)
}