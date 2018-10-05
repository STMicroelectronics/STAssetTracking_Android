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

package com.st.assetTracking.sigfox.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.st.assetTracking.sigfox.R
import com.st.assetTracking.sigfox.model.Orientation
import com.st.assetTracking.sigfox.model.SamplingSettings
import com.st.assetTracking.sigfox.model.SensorThreshold
import com.st.assetTracking.sigfox.model.ThresholdSensorType
import java.util.*

internal class SampleSettingsViewModel : ViewModel() {


    private val _samplingInterval = MutableLiveData<Short>()
    val samplingInterval :LiveData<Short>
        get() = _samplingInterval

    private val _cloudSyncInterval = MutableLiveData<Short>()
    val cloudSyncInterval :LiveData<Short>
        get() = _cloudSyncInterval

    private val _showProgressBar = MutableLiveData<Boolean>()
    val showProgressBar : LiveData<Boolean>
        get() = _showProgressBar

    fun setSamplingInterval(interval:Short?){
        _samplingInterval.value = interval ?: DEFAULT_SENSOR_SAMPLING_S
    }

    fun setCloudSyncInterval(interval: Short?) {
        _cloudSyncInterval.value = interval ?: DEFAULT_CLOUD_SYNC_S
    }

    var samplingSettings: SamplingSettings
        get() {
            return SamplingSettings(samplingInterval = _samplingInterval.value
                    ?: DEFAULT_SENSOR_SAMPLING_S,
                    cloudSyncInterval = _cloudSyncInterval.value ?: DEFAULT_CLOUD_SYNC_S,
                    threshold = mSensorThreshold
            )
        }
        set(value) {
            setSamplingInterval(value.samplingInterval)
            setCloudSyncInterval(value.cloudSyncInterval)
            mSensorThreshold.clear()
            mSensorThreshold.addAll(value.threshold)
            _sensorThreshold.value = mSensorThreshold.toDataView()
        }

    private val _showDialogMessage = MutableLiveData<Int>()
    val dialogMessage : LiveData<Int>
        get() = _showDialogMessage


    private val mSensorThreshold = mutableListOf<SensorThreshold>()
    private val _sensorThreshold = MutableLiveData<List<SensorThresholdViewData>>()
    val sensorThresholds: LiveData<List<SensorThresholdViewData>>
        get() = _sensorThreshold

    fun addSensorThreshold( newThreshold: SensorThreshold){
        mSensorThreshold.add(newThreshold)
        notifyUpdateSensorThreshold()
    }

    private fun notifyUpdateSensorThreshold(){
        _sensorThreshold.postValue(mSensorThreshold.toDataView())
    }

    fun removeSensorThresholdWithIndex(index:Int){
       if(mSensorThreshold.size<=index && index<0) //check that the element exist
           return
        mSensorThreshold.removeAt(index)
        notifyUpdateSensorThreshold()
    }

    fun boardConnecting() = _showProgressBar.postValue(true)
    fun boardDisconnect() = _showProgressBar.postValue(false)
    fun onTransferComplete() {
        _showDialogMessage.postValue(R.string.settings_transferComplete)
    }
    fun onTransferError() {
        _showDialogMessage.postValue(R.string.settings_transferFailed)
    }
    fun onConnectionError() {
        _showDialogMessage.postValue(R.string.settings_connectionError)
        boardDisconnect()
    }


    companion object {
        private const val DEFAULT_CLOUD_SYNC_S = 15.toShort()
        private const val DEFAULT_SENSOR_SAMPLING_S = 1.toShort()
    }
}

private fun List<SensorThreshold>.toDataView():List<SensorThresholdViewData> {
    return map {
        when (it.sensor) {
            ThresholdSensorType.Humidity,
            ThresholdSensorType.Temperature,
            ThresholdSensorType.Pressure,
            ThresholdSensorType.WakeUp -> {
                it.toEnvironmentalViewData()
            }
            ThresholdSensorType.Orientation->{
                it.toOrientationViewData()
            }
            ThresholdSensorType.Tilt ->{
                it.toTiltViewData()
            }
        }

    }
}

private fun SensorThreshold.toEnvironmentalViewData(): EnvironmentalThresholdViewData {
    val valueStr = String.format(Locale.getDefault(),"%.1f",threshold)
    return EnvironmentalThresholdViewData(sensor.resourceImage,sensor.resourceString,
            compareSymbolStr = comparison.string,
            valueStr = valueStr,
            unitStrId = sensor.unitResourceString)
}

private fun SensorThreshold.toOrientationViewData(): OrientationThresholdViewData {
    val event = Orientation.fromRawValue(threshold.toShort())
    return OrientationThresholdViewData(sensor.resourceImage,sensor.resourceString,event.resourceImage,event.resourceString)
}

private fun SensorThreshold.toTiltViewData() : TiltThresholdViewData{
    return TiltThresholdViewData()
}