package com.st.assetTracking.threshold.model

import java.io.Serializable
import java.util.*

/**
 * class containing the extreme data from each sensor
 *
 * [acquisitionStart] when the acquisition started
 * [temperature] extreme data from the temperature, null if the temperature logging is disabled
 * [pressure] extreme data from the pressure, null if the pressure logging is disabled
 * [humidity] extreme data from the humidity, null if the humidity logging is disabled
 * [vibration] extreme data from the acceleration, null if the acceleration logging is disabled
 */
data class AcquisitionExtremes(
        val acquisitionStart: Date,
        val temperature: DataExtreme?,
        val pressure: DataExtreme?,
        val humidity: DataExtreme?,
        val vibration: DataExtreme?
) : Serializable