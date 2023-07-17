package com.st.polaris.ble.provisioning

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.ExtConfiguration.CustomCommand
import com.st.BlueSTSDK.Features.ExtConfiguration.FeatureExtConfiguration
import com.st.BlueSTSDK.Node
import com.st.assetTracking.dashboard.model.DeviceProfile
import com.st.assetTracking.dashboard.persistance.DeviceListRepository

class ExtConfigurationViewModel : ViewModel() {

    var context: Context? = null

    private val _commandlist_answer = MutableLiveData<String?>(null)
    val commandlist_answer: LiveData<String?>
        get() = _commandlist_answer

    private val _info_answer = MutableLiveData<String?>(null)
    val info_answer: LiveData<String?>
        get() = _info_answer

    private val _uid_answer = MutableLiveData<String?>(null)
    val uid_answer: LiveData<String?>
        get() = _uid_answer

    private val _customcommandlist_answer = MutableLiveData<List<CustomCommand>?>(null)
    val customcommandlist_answer: LiveData<List<CustomCommand>?>
        get() = _customcommandlist_answer

    private val mDeviceProfiles = MutableLiveData<List<DeviceProfile>>()
    val deviceProfiles: LiveData<List<DeviceProfile>>
        get() = mDeviceProfiles

    private val mcompleteProvisioning = MutableLiveData(false)
    val completeProvisioning: LiveData<Boolean>
        get() = mcompleteProvisioning

    companion object {
        const val TAG = "ExtConfViewModel"
    }

    var mFeature: FeatureExtConfiguration? = null

    private var mcuid: String? = null

    fun enableNotification(node: Node) {
        mFeature = node.getFeature(FeatureExtConfiguration::class.java)
        mFeature?.apply {
            addFeatureListener(featureListener)
            enableNotification()
            writeCommandWithoutArgument(FeatureExtConfiguration.READ_COMMANDS)
        }
    }

    suspend fun getDeviceProfiles(deviceListRepository: DeviceListRepository){
        val defaultDeviceProfile = deviceListRepository.getDefaultDeviceProfile()
        val deviceProfiles = deviceListRepository.getDeviceProfile()

        val deviceProfilesList : List<DeviceProfile> = defaultDeviceProfile + deviceProfiles

        mDeviceProfiles.postValue(deviceProfilesList)
    }

    fun setCompletedProvisioning() {
        mcompleteProvisioning.postValue(true)
    }

    fun disableNotification(node: Node) {
        node.getFeature(FeatureExtConfiguration::class.java)?.apply {
            removeFeatureListener(featureListener)
            disableNotification()
        }

        mFeature = null
    }

    private val featureListener = Feature.FeatureListener { _, sample ->
        Log.i(TAG, "sample received")
        if (sample is FeatureExtConfiguration.CommandSample) {
            val responseObj = sample.command
            if (responseObj != null) {
                //Try to retrieve the command list
                var answer = FeatureExtConfiguration.resultCommandList(responseObj)
                if (answer != null) {
                    _commandlist_answer.postValue(answer)
                }

                //Try to retrieve the Info
                answer = FeatureExtConfiguration.resultCommandInfo(responseObj)
                if (answer != null) {
                    _info_answer.postValue(answer)
                }

                //Try to retrieve the uid
                answer = FeatureExtConfiguration.resultCommandSTM32UID(responseObj)
                if (answer != null) {
                    _uid_answer.postValue(answer)
                }

                //Try to retrieve the Custom Commands List
                val listCommand = FeatureExtConfiguration.resultCustomCommandList(responseObj)
                if (listCommand != null) {
                    _customcommandlist_answer.postValue(listCommand)
                }


            }
        }
    }

    fun readUid() {
        mFeature?.writeCommandWithoutArgument(FeatureExtConfiguration.READ_UID)
    }

    fun readCustomCommands() {
        mFeature?.writeCommandWithoutArgument(FeatureExtConfiguration.READ_CUSTOM_COMMANDS)
    }

    fun sendCustomCommandString(name: String, value: String) {
        mFeature?.writeCommandSetArgumentString(name, value)
    }

    fun sendCustomCommandVoid(name: String) {
        mFeature?.writeCommandWithoutArgument(name)
    }

    fun commandListReceived() {
        _commandlist_answer.postValue(null)
    }

    fun infoReceived() {
        _info_answer.postValue(null)
    }

    fun uidReceived() {
        _uid_answer.postValue(null)
    }

    fun setMCUID(mcuId: String?) {
        mcuid = mcuId
    }

    fun getMCUID(): String? {
        return mcuid
    }
}