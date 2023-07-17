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
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.st.assetTracking.threshold.R
import com.st.assetTracking.threshold.model.ThresholdSensorType

import com.st.assetTracking.threshold.view.adapter.SensorTypeSelectorAdapter

/**
 *  show the list of sensor and when the user select one, call the on activity result on the target
 *  fragment or the parent fragment if the target is null
 */
internal class SensorTypeSelectorFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_sensor_type_selector, container, false)

        rootView.findViewById<RecyclerView>(R.id.sensorTypeSelector_list).adapter =
                SensorTypeSelectorAdapter(ThresholdSensorType.values().toList(), object : SensorTypeSelectorAdapter.OnSensorSelectedCallback {
                    override fun onSensorSelected(item: ThresholdSensorType) {
                        val result = wrapSelectedSensor(item)
                        safeTargetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, result)
                    }
                })

        return rootView
    }

    companion object {
        private const val SENSOR_EXTRA = "SensorTypeSelectorFragment.SENSOR_EXTRA"

        private fun wrapSelectedSensor(sensor: ThresholdSensorType): Intent {
            return Intent().apply {
                putExtra(SENSOR_EXTRA, sensor)
            }
        }

        fun extractSelectedSensor(intent: Intent?): ThresholdSensorType? {
            return intent?.getSerializableExtra(SENSOR_EXTRA) as? ThresholdSensorType
        }
    }

}
