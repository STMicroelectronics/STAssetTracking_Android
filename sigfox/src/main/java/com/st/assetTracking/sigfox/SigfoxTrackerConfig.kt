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

package com.st.assetTracking.sigfox

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.st.assetTracking.sigfox.model.SensorThreshold
import com.st.assetTracking.sigfox.model.ThresholdSensorType
import com.st.assetTracking.sigfox.view.AddSensorThresholdFragment
import com.st.assetTracking.sigfox.view.AddWakeUpThresholdFragment
import com.st.assetTracking.sigfox.view.OrientationSelectorFragment
import com.st.assetTracking.sigfox.view.SensorTypeSelectorFragment
import com.st.assetTracking.sigfox.viewModel.*

class SigfoxTrackerConfig : AppCompatActivity() {

    companion object {
        private val SHOW_SETTINGS_FRAGMENT_TAG = SigfoxTrackerConfig::class.java.name+".SHOW_SETTINGS_FRAGMENT_TAG"
        private val DIALOG_FRAGMENT_TAG = SigfoxTrackerConfig::class.java.name+".DIALOG_FRAGMENT_TAG"
    }

    private lateinit var mNavigator:NavigationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sigfox_tracker_config)
        mNavigator = ViewModelProviders.of(this).get(NavigationViewModel::class.java)
        mNavigator.currentView.observe(this, Observer {
            if (it!=null){
                changeView(it)
            }

        })
        if(mNavigator.currentView.value == null){
            mNavigator.moveTo(ShowSampleSettings)
        }
    }

    private fun changeView(destinationView: DestinationView) {
        when(destinationView){
            is ShowSampleSettings -> showSampleSettings()
            is SelectSensor -> showSelectSensor()
            is AddThreshold -> showAddThreshold(destinationView.sensor)
        }
    }

    private fun showAddThreshold(sensor: ThresholdSensorType) {
        when(sensor){
            ThresholdSensorType.Temperature,
            ThresholdSensorType.Pressure ,
            ThresholdSensorType.Humidity -> showInsertThreshold(sensor)
            ThresholdSensorType.WakeUp -> showAddWakeUpThreshold()
            ThresholdSensorType.Tilt -> addTiltThreshold()
            ThresholdSensorType.Orientation -> showSelectOrientation()
        }
    }

    private fun showAddWakeUpThreshold() {
        val dialog = AddWakeUpThresholdFragment()
        dialog.show(supportFragmentManager,DIALOG_FRAGMENT_TAG)
    }

    private fun showInsertThreshold(sensor: ThresholdSensorType) {
        val dialog = AddSensorThresholdFragment.instanceForSensor(sensor)
        dialog.show(supportFragmentManager,DIALOG_FRAGMENT_TAG)
    }

    private fun addTiltThreshold(){
        val sampleSettings = ViewModelProviders.of(this).get(SampleSettingsViewModel::class.java)
        sampleSettings.addSensorThreshold(SensorThreshold.tiltThreshold())
        mNavigator.moveTo(ShowSampleSettings)
    }

    private fun showSelectSensor() {
        val dialog = SensorTypeSelectorFragment()
        dialog.show(supportFragmentManager,DIALOG_FRAGMENT_TAG)
    }

    private fun showSampleSettings() {
        val prev = supportFragmentManager.findFragmentByTag(SHOW_SETTINGS_FRAGMENT_TAG)
        if(prev==null){
            val fragment = SampleSettingsFragment.newInstance()
            supportFragmentManager.beginTransaction()
                    .add(R.id.sigfox_config_rootView,fragment, SHOW_SETTINGS_FRAGMENT_TAG)
                    .commit()
        }
    }

    private fun showSelectOrientation(){
        val dialog = OrientationSelectorFragment()
        dialog.show(supportFragmentManager,DIALOG_FRAGMENT_TAG)
    }
}
