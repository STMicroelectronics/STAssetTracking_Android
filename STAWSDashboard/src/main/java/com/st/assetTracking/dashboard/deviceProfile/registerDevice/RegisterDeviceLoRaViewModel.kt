package com.st.assetTracking.dashboard.deviceProfile.registerDevice

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import com.st.assetTracking.dashboard.model.Device
import com.st.assetTracking.dashboard.model.DeviceProfile
import com.st.assetTracking.dashboard.persistance.DeviceListRepository
import kotlinx.coroutines.launch

/**
 * view model managin the device registration
 * @param deviceId name of the device to register
 * @param deviceListRepository list of the know devices
 */
internal class RegisterDeviceLoRaViewModel(val context: Context, val deviceId: String,
                                           private val deviceListRepository: DeviceListRepository) : ViewModel() {

    enum class RegistrationStatus {
        /**
         * get device profile
         */
        GET_DEVICE_PROFILE,

        /**
         * check if the device is already known
         */
        ONLINE_CHECK,

        /**
         * a new device creation is needed
         */
        REGISTRATION_NEEDED,

        /**
         * a new device creation is ongoing
         */
        REGISTRATION_ONGOING,

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

    private val mDeviceProfiles = MutableLiveData<List<DeviceProfile>>()
    val deviceProfiles: LiveData<List<DeviceProfile>>
        get() = mDeviceProfiles

    /**
     * check if the device is already known
     */
    init {
        viewModelScope.launch {
            mRegistrationStatus.postValue(RegistrationStatus.GET_DEVICE_PROFILE)
            val deviceProfiles = deviceListRepository.getDeviceProfile()

            mDeviceProfiles.postValue(deviceProfiles)

            mRegistrationStatus.postValue(RegistrationStatus.ONLINE_CHECK)
            val device = deviceListRepository.getDeviceWithId(deviceId)
            if (device != null) {
                mRegistrationStatus.postValue(RegistrationStatus.COMPLETE)
            } else {
                mRegistrationStatus.postValue(RegistrationStatus.REGISTRATION_NEEDED)
            }
        }
    }

    /**
     * register the device with [deviceId] and with an human-readable name [deviceName]
     */
    fun registerDeviceWithName(context: Context, deviceName: String, deviceType: String, deviceProfileSelected : DeviceProfile) {
        mRegistrationStatus.postValue(RegistrationStatus.REGISTRATION_ONGOING)

        val sharePrefEditor: SharedPreferences.Editor = context.getSharedPreferences("DeviceProfile", Context.MODE_PRIVATE).edit()
        sharePrefEditor.putString("appEUI", deviceProfileSelected.context.application_eui)
        sharePrefEditor.putString("appKEY", deviceProfileSelected.context.application_key)
        sharePrefEditor.apply()

        val device = createDevice(deviceId, deviceName, deviceType, deviceProfileSelected.id)

        viewModelScope.launch {
            if (deviceListRepository.registerDevice(device)) {
                mRegistrationStatus.postValue(RegistrationStatus.COMPLETE)
            } else {
                mRegistrationStatus.postValue(RegistrationStatus.FAILED)
            }
        }
    }

    private fun createDevice(deviceId: String, deviceName: String, deviceType: String, deviceProfileId: String) : Device {
        return Device(
                id = deviceId,
                name = deviceName,
                type = Device.Type.strToDeviceType(deviceType) ?: Device.Type.UNKNOWN,
                lastActivity = null,
                configuration = null,
                configurationSignaled = null,
                certificate = null,
                selfSigned = false,
                deviceProfile = deviceProfileId,
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
            return RegisterDeviceLoRaViewModel(context, deviceId, deviceListRepository) as T
        }
    }

}