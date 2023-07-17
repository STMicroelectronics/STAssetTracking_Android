package com.st.assetTracking.dashboard.uploadData.registerDevice

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.*
import com.amazonaws.services.cognitoidentityprovider.model.DeviceType
import com.st.assetTracking.dashboard.model.Device
import com.st.assetTracking.dashboard.persistance.DeviceListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * view model managin the device registration
 * @param deviceId name of the device to register
 * @param deviceListRepository list of the know devices
 */
internal class RegisterDeviceViewModel(val context: Context, val deviceId: String,
                                       private val deviceListRepository: DeviceListRepository) : ViewModel() {

    enum class RegistrationStatus {
        /**
         * check if the device is already known
         */
        ONLINE_CHECK,

        /**
         * a new device is needed
         */
        REGISTRATION_NEEDED,

        /**
         * a new device creation is ongoing
         */
        REGISTRATION_ONGOING,

        /**
         * device registered
         */
        REGISTRATION_APIKEY,

        /**
         * device registered
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

    /**
     * check if the device is already known
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
                mRegistrationStatus.postValue(RegistrationStatus.COMPLETE)
            } else {
                mRegistrationStatus.postValue(RegistrationStatus.REGISTRATION_NEEDED)
            }
        }
        mRegistrationStatus.postValue(RegistrationStatus.REGISTRATION_NEEDED)
    }

    /**
     * register the device with [deviceId] and with an human-readable name [deviceName]
     */
    fun registerDeviceWithName(context: Context, deviceName: String, deviceType: String) {

        val device = createDevice(deviceId, deviceName, deviceType)

        viewModelScope.launch {
            mRegistrationStatus.postValue(RegistrationStatus.REGISTRATION_ONGOING)
            if (deviceListRepository.registerDevice(device)) {

                val apiKey = deviceListRepository.registerApiKey()
                val sharePrefEditor: SharedPreferences.Editor = context.getSharedPreferences("TokenCollection", Context.MODE_PRIVATE).edit()
                sharePrefEditor.putString("ApiKey", apiKey.apiKey)
                sharePrefEditor.putString("Owner", apiKey.owner)
                sharePrefEditor.apply()

                mRegistrationStatus.postValue(RegistrationStatus.COMPLETE)
            } else {
                mRegistrationStatus.postValue(RegistrationStatus.FAILED)
            }
        }
    }

    private fun createDevice(deviceId: String, deviceName: String, deviceType: String) : Device{
        return Device(
                id = deviceId,
                name = deviceName,
                type = Device.Type.strToDeviceType(deviceType) ?: Device.Type.UNKNOWN,
                lastActivity = null,
                configuration = null,
                configurationSignaled = null,
                certificate = null,
                selfSigned = false,
                deviceProfile = null,
                mac = null,
                devEui = null,
                lastTemperatureData = null,
                lastPressureData = null,
                lastHumidityData = null,
                boardID = null,
                firmwareID = null
        )
    }

    class Factory(private val context: Context, private val deviceId: String, private val deviceListRepository: DeviceListRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return RegisterDeviceViewModel(context, deviceId, deviceListRepository) as T
        }
    }

}