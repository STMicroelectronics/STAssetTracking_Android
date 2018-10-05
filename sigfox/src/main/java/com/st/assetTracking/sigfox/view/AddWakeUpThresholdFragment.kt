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

package com.st.assetTracking.sigfox.view


import android.app.Dialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import com.google.android.material.textfield.TextInputLayout
import androidx.appcompat.app.AppCompatDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.st.assetTracking.sigfox.R
import com.st.assetTracking.sigfox.model.SensorThreshold
import com.st.assetTracking.sigfox.model.ThresholdSensorType
import com.st.assetTracking.sigfox.util.FloatInputRangeChecker
import com.st.assetTracking.sigfox.viewModel.*


internal class AddWakeUpThresholdFragment : AppCompatDialogFragment() {


    private lateinit var mThresholdValue:TextView
    private val mNavigator by lazy { ViewModelProviders.of(requireActivity()).get(NavigationViewModel::class.java) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView =  inflater.inflate(R.layout.fragment_add_wakeup_theshold, container, false)


        val textLayout = rootView.findViewById<TextInputLayout>(R.id.addWakeUpThreshold_threshold_layout)
        mThresholdValue = rootView.findViewById(R.id.addWakeUpThreshold_threshold_value)
        mThresholdValue.addTextChangedListener(FloatInputRangeChecker(textLayout,ThresholdSensorType.WakeUp.range))

        rootView.findViewById<View>(R.id.addWakeUpThreshold_addButton).setOnClickListener {
            addNewThreshold()
        }

        rootView.findViewById<View>(R.id.addWakeUpThreshold_cancelButton).setOnClickListener {
            dismiss()
        }

        return rootView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        dialog.setTitle(R.string.addWakeUpThreshold_title)
        return dialog
    }


    private fun addNewThreshold() {
        val value = mThresholdValue.text.toString().toFloatOrNull()
        if(value != null){
            val newThreshold = SensorThreshold.wakeUpThreshold(value)
            val viewModel = ViewModelProviders.of(requireActivity()).get(SampleSettingsViewModel::class.java)
            viewModel.addSensorThreshold(newThreshold)
            mNavigator.moveTo(ShowSampleSettings)
        }
        dismiss()
    }


}
