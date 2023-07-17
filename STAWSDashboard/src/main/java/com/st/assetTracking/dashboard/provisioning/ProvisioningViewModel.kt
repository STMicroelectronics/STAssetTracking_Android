package com.st.assetTracking.dashboard.provisioning

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import com.st.assetTracking.dashboard.model.Device
import com.st.assetTracking.dashboard.persistance.DeviceListRepository
import kotlinx.coroutines.launch

/**
 * view model managin the device registration
 * @param deviceId name of the device to register
 * @param deviceListRepository list of the know devices
 */
class ProvisioningViewModel(val context: Context,
                            val deviceId: String,
                            val deviceName: String,
                            val deviceType: String,
                            val deviceMacAddress: String?,
                            val deviceEui: String?,
                            val deviceProfile: String?,
                            val deviceID: Int?,
                            val firmwareID: Int?,
                            private val deviceListRepository: DeviceListRepository) : ViewModel() {

    enum class RegistrationStatus {
        /**
         * check if the device is already known
         */
        ONLINE_CHECK,

        /**
         * device is already known
         */
        ALREADY_KNOWN,

        /**
         * device registered
         */
        REGISTRATION_APIKEY,

        /**
         * device registration complete
         */
        COMPLETE,

        /**
         * registration error
         */
        FAILED,
    }

    private val mRegistrationStatus = MutableLiveData<RegistrationStatus>()
    val registrationStatus: LiveData<RegistrationStatus>
        get() = mRegistrationStatus

    private var mDeviceRegistered : Device?=null

    fun getRegistedDevice() : Device? {
        return mDeviceRegistered
    }

    /**
     * register the device with [deviceId] and with an human-readable name [deviceName]
     */
    init {
        viewModelScope.launch {
            mRegistrationStatus.postValue(RegistrationStatus.ONLINE_CHECK)
            val device = deviceListRepository.getDeviceWithId(deviceId)
            if (device != null) {
                mRegistrationStatus.postValue(RegistrationStatus.REGISTRATION_APIKEY)
                val apiKey = deviceListRepository.registerApiKey()
                val sharePrefEditor: SharedPreferences.Editor = context.getSharedPreferences("TokenCollection", Context.MODE_PRIVATE).edit()
                sharePrefEditor.putString("ApiKey", apiKey.apiKey)
                sharePrefEditor.putString("Owner", apiKey.owner)
                sharePrefEditor.apply()
                mRegistrationStatus.postValue(RegistrationStatus.ALREADY_KNOWN)
                mDeviceRegistered = device
            } else {
                val newDevice = createDevice(deviceId, deviceName, deviceType, null, false, deviceMacAddress, deviceEui, deviceProfile, deviceID, firmwareID)
                if (deviceListRepository.registerDevice(newDevice)) {
                    val apiKey = deviceListRepository.registerApiKey()
                    val sharePrefEditor: SharedPreferences.Editor = context.getSharedPreferences("TokenCollection", Context.MODE_PRIVATE).edit()
                    sharePrefEditor.putString("ApiKey", apiKey.apiKey)
                    sharePrefEditor.putString("Owner", apiKey.owner)
                    sharePrefEditor.apply()

                    mDeviceRegistered = device

                    mRegistrationStatus.postValue(RegistrationStatus.COMPLETE)
                } else {
                    mRegistrationStatus.postValue(RegistrationStatus.FAILED)
                }

            }
        }
    }

    private fun createDevice(deviceId: String, deviceName: String, deviceType: String,Cert: String?, self_Signed: Boolean, mac: String?, deviceEui: String?, deviceProfile: String?, boardID: Int?, firmwareID: Int?) : Device {
        return Device(
                id = deviceId,
                name = deviceName,
                type = Device.Type.strToDeviceType(deviceType) ?: Device.Type.UNKNOWN,
                lastActivity = null,
                configuration = null,
                configurationSignaled = null,
                certificate = Cert,
                selfSigned = self_Signed,
                deviceProfile = deviceProfile,
                mac = mac,
                devEui = deviceEui,
                lastTemperatureData = null,
                lastPressureData = null,
                lastHumidityData = null,
                boardID = boardID,
                firmwareID = firmwareID
        )
    }

    class Factory(private val context: Context, private val deviceId: String, private val deviceName: String, private val deviceType: String, private val deviceMacAddress: String?, private val deviceEui: String?, private val deviceProfile: String?, private val deviceID: Int?, private val firmwareID: Int?, private val deviceListRepository: DeviceListRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ProvisioningViewModel(context, deviceId, deviceName, deviceType, deviceMacAddress, deviceEui, deviceProfile, deviceID, firmwareID, deviceListRepository) as T
        }
    }

}