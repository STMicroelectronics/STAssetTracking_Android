package com.st.assetTracking.sigfox

import androidx.lifecycle.*
import com.st.assetTracking.atrBle1.sensorTileBox.settings.LogSettingsRepository
import com.st.assetTracking.threshold.model.SensorThreshold
import com.st.assetTracking.threshold.view.util.SensorSamplingInputChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
* View model used to read and write the sensor configuration
* @param settingsRepository object used to read and write the threshold configuration
*/
internal class SettingsSigfoxViewModel(private val settingsRepository: LogSettingsRepository) : ViewModel() {

    private val mShowContentLoadingBar = MutableLiveData<Boolean>()
    val showContentLoadingBar: LiveData<Boolean>
        get() = mShowContentLoadingBar

    private val mSaveOperationSuccess = MutableLiveData<Boolean>()
    val saveOperationSuccess: LiveData<Boolean>
        get() = mSaveOperationSuccess

    val thresholdListClean = settingsRepository.sensorThresholds

    val sensorReadingInterval = settingsRepository.sensorReadInterval

    init {
        viewModelScope.launch(Dispatchers.IO) {
            mShowContentLoadingBar.postValue(true)
            settingsRepository.loadSigfoxDataAsync()
            mShowContentLoadingBar.postValue(false)
        }
    }

    fun addThreshold(newTh: SensorThreshold) {
        settingsRepository.addThreshold(newTh)
    }

    fun saveCurrentSettings(sensorSampleValue: Short?, cloudSyncInterval: Short?) {
        val validRange = SensorSamplingInputChecker.VALID_RANGE
        // clamp the value between into the valid range or take the min if null
        val value = sensorSampleValue?.toInt()?.coerceIn(validRange) ?: validRange.start
        var valueCloudSync = cloudSyncInterval
        if(valueCloudSync == null){
            valueCloudSync = 15.toShort()
        }
        viewModelScope.launch {
            mShowContentLoadingBar.postValue(true)
            val saveComplete = settingsRepository.setSensorSigfoxSampleValue(value.toShort(), valueCloudSync)
            mSaveOperationSuccess.postValue(saveComplete)
            mShowContentLoadingBar.postValue(false)
        }

    }

    fun clearConfiguration(){
        settingsRepository.clearTreshold()
    }

    class Factory(private val settingsRepository: LogSettingsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SettingsSigfoxViewModel(settingsRepository) as T
        }

    }

}
