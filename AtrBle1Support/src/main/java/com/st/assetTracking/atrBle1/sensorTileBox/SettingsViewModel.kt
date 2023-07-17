package com.st.assetTracking.atrBle1.sensorTileBox

import androidx.lifecycle.*
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Node.Type
import com.st.assetTracking.atrBle1.sensorTileBox.communication.LogStatus
import com.st.assetTracking.atrBle1.sensorTileBox.settings.LogSettingsRepository
import com.st.assetTracking.threshold.model.SensorThreshold
import com.st.assetTracking.threshold.view.model.toViewData
import com.st.assetTracking.threshold.view.util.SensorSamplingInputChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * View model used to read and write the sensor configuration
 * @param settingsRepository object used to read and write the threshold configuration
 */
internal class SettingsViewModel(private val node: Node, private val settingsRepository: LogSettingsRepository) : ViewModel() {

    private val mShowContentLoadingBar = MutableLiveData<Boolean>()
    val showContentLoadingBar: LiveData<Boolean>
        get() = mShowContentLoadingBar

    private val mAtrFirmware = MutableLiveData<Boolean>()
    val atrFirmware: LiveData<Boolean>
        get() = mAtrFirmware

    private val mFw = MutableLiveData<String>()
    val firmware: LiveData<String>
        get() = mFw

    private val mUid = MutableLiveData<String>()
    val uid: LiveData<String>
        get() = mUid

    private val mcompleteProvisioning = MutableLiveData(false)
    val completeProvisioning: LiveData<Boolean>
        get() = mcompleteProvisioning

    private val mSaveOperationSuccess = MutableLiveData<Boolean>()
    val saveOperationSuccess: LiveData<Boolean>
        get() = mSaveOperationSuccess

    val thresholdList = settingsRepository.sensorThresholds.map { thresholdList ->
        thresholdList.map { it.toViewData() }
    }

    val thresholdListClean = settingsRepository.sensorThresholds

    val boardIsLogging: LiveData<Boolean> = settingsRepository.logStatus.map { status ->
        status == LogStatus.Started
    }

    val sensorReadingInterval = settingsRepository.sensorReadInterval

    init {
        viewModelScope.launch(Dispatchers.IO) {
            mShowContentLoadingBar.postValue(true)

            if(node.type == Type.SENSOR_TILE_BOX) {
                val fw = settingsRepository.checkATRFirmware()
                if(fw.contains("fp-atr-ble1")){
                    mAtrFirmware.postValue(true)
                    mFw.postValue(fw)
                    delay(500)
                    val uid = settingsRepository.getUID()
                    mUid.postValue(uid)
                    settingsRepository.loadDataAsync()
                }else {
                    mShowContentLoadingBar.postValue(false)
                    mAtrFirmware.postValue(false)
                }
            } else {
                val uid = settingsRepository.getUID()
                mUid.postValue(uid)
            }

            mShowContentLoadingBar.postValue(false)
        }
    }

    fun addThreshold(newTh: SensorThreshold) {
        settingsRepository.addThreshold(newTh)
    }

    fun removeSensorThresholdWithIndex(adapterPosition: Int) {
        settingsRepository.removeThresholdAt(adapterPosition)
    }

    fun saveCurrentSettings(sensorSampleValue: Short?) {
        val validRange = SensorSamplingInputChecker.VALID_RANGE
        // clamp the value between into the valid range or take the min if null
        val value = sensorSampleValue?.toInt()?.coerceIn(validRange) ?: validRange.start
        viewModelScope.launch {
            mShowContentLoadingBar.postValue(true)
            val saveComplete = settingsRepository.setSensorSampleValue(value.toShort())
            mSaveOperationSuccess.postValue(saveComplete)
            settingsRepository.startLog()
            mShowContentLoadingBar.postValue(false)
        }

    }

    fun clearConfiguration(){
        settingsRepository.clearTreshold()
    }

    fun enableLogging() {
        viewModelScope.launch {
            settingsRepository.startLog()
        }
    }

    fun disableLogging() {
        viewModelScope.launch {
            settingsRepository.stopLog()
        }
    }

    fun setCompletedProvisioning() {
        mcompleteProvisioning.postValue(true)
    }

    class Factory(private val node: Node, private val settingsRepository: LogSettingsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(node, settingsRepository) as T
        }

    }

}
