package com.st.assetTracking.threshold.model

import java.io.Serializable
import java.util.*

/**
 * Class containing the min/max value and the date where they was recorded
 */
data class DataExtreme(
        val minDate: Date,
        val minValue: Float,
        val maxDate: Date,
        val maxValue: Float
) : Serializable