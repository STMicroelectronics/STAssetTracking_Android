package com.st.assetTracking.threshold.view.util

import com.google.android.material.textfield.TextInputLayout

class SensorSamplingInputChecker(text: TextInputLayout) : IntInputRangeChecker(text, VALID_RANGE) {

    companion object {
        val VALID_RANGE: ClosedRange<Int> = 1..60
    }
}