/*
 *  Copyright (c) 2019  STMicroelectronics – All rights reserved
 *  The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice, this list of conditions
 *    and the following disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright notice, this list of
 *    conditions and the following disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 *  - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *    STMicroelectronics company nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 *  - All of the icons, pictures, logos and other images that are provided with the source code
 *    in a directory whose title begins with st_images may only be used for internal purposes and
 *    shall not be redistributed to any third party or modified in any way.
 *
 *  - Any redistributions in binary form shall not include the capability to display any of the
 *    icons, pictures, logos and other images that are provided with the source code in a directory
 *    whose title begins with st_images.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 *  AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 *  OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 *  OF SUCH DAMAGE.
 */

package com.st.assetTracking.threshold.view.thresholdSelector

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.android.material.textfield.TextInputLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.st.assetTracking.threshold.R
import com.st.assetTracking.threshold.model.ThresholdComparison
import com.st.assetTracking.threshold.model.ThresholdSensorType
import com.st.assetTracking.threshold.view.unitResourceString
import com.st.assetTracking.threshold.view.util.FloatInputRangeChecker

/**
 * ask to the user to add the float threshold
 * when the user press the add button the onActivityResult is called
 */
internal class SelectThresholdValueFragment : Fragment() {

    companion object {
        private const val SENSOR_TYPE_KEY = "SENSOR"
        fun instanceForSensor(sensor: ThresholdSensorType): Fragment {

            val fragment = SelectThresholdValueFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(SENSOR_TYPE_KEY, sensor)
            }
            return fragment
        }

        private const val COMPARISON_EXTRA_KEY = "AddSensorThresholdFragment.COMPARISON_EXTRA_KEY"
        private const val VALUE_EXTRA_KEY_1 = "AddSensorThresholdFragment.VALUE_EXTRA_KEY_1"
        private const val VALUE_EXTRA_KEY_2 = "AddSensorThresholdFragment.VALUE_EXTRA_KEY_2"

        private fun wrapResult(value1: Float, value2: Float): Intent {
            return Intent().apply {
                putExtra(VALUE_EXTRA_KEY_1, value1)
                putExtra(VALUE_EXTRA_KEY_2, value2)
            }
        }

        fun extractValue1(intent: Intent?): Float? {
            return intent?.getFloatExtra(VALUE_EXTRA_KEY_1, Float.NaN)
        }

        fun extractValue2(intent: Intent?): Float? {
            return intent?.getFloatExtra(VALUE_EXTRA_KEY_2, Float.NaN)
        }

    }

    private lateinit var mThresholdValue1: TextView
    private lateinit var mThresholdValue2: TextView

    private lateinit var mComparisonRadioButtons: RadioGroup
    private val sensorType by lazy { requireArguments()[SENSOR_TYPE_KEY] as ThresholdSensorType }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_add_sensor_threshold, container, false)

        val textLayout1 = rootView.findViewById<TextInputLayout>(R.id.addThreshold_threshold_layout)
        val textLayout2 = rootView.findViewById<TextInputLayout>(R.id.addThreshold_threshold_layout2)

        mThresholdValue1 = rootView.findViewById(R.id.addThreshold_threshold_value)
        mThresholdValue1.addTextChangedListener(FloatInputRangeChecker(textLayout1, sensorType.range))

        mThresholdValue2 = rootView.findViewById(R.id.addThreshold_threshold_value2)
        mThresholdValue2.addTextChangedListener(FloatInputRangeChecker(textLayout2, sensorType.range))

        rootView.findViewById<TextView>(R.id.addThreshold_threshold_unit).setText(sensorType.unitResourceString)
        rootView.findViewById<TextView>(R.id.addThreshold_threshold_unit2).setText(sensorType.unitResourceString)

        //mComparisonRadioButtons = rootView.findViewById(R.id.addThreshold_comparisonGroup)

        rootView.findViewById<View>(R.id.addThreshold_addButton).setOnClickListener {
            addNewThreshold()
        }

        rootView.findViewById<View>(R.id.addThreshold_cancelButton).setOnClickListener {
            cancelRequest()
        }

        return rootView
    }

    private fun getComparisonType(): ThresholdComparison {
        /*return if (mComparisonRadioButtons.checkedRadioButtonId == R.id.addThreshold_lessThanButton)
            ThresholdComparison.Less
        else
            ThresholdComparison.BiggerOrEqual*/
        /**
         * TODO: Fix
         */
        return ThresholdComparison.Less

    }
/*
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        val sensorName = getString(sensorType.resourceString)
        dialog.setTitle(getString(R.string.addThreshold_title_format,sensorName))
        return dialog
    }
*/

    private fun addNewThreshold() {

        /**
         * TODO: Fix
         */
        val value1 = mThresholdValue1.text.toString().toFloatOrNull()
        val value2 = mThresholdValue2.text.toString().toFloatOrNull()

        if (value1 != null && value2 != null) {
            returnSelection(value1, value2)
            //returnSelection(comparisonTypeGreater, value2)
        } else {
            cancelRequest()
        }

    }

    private fun returnSelection(value1: Float, value2: Float) {
        safeTargetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK,
                wrapResult(value1, value2))
    }
}
