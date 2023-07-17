package com.st.assetTracking.dashboard.persistance

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.st.assetTracking.dashboard.communication.DeviceListManager
import com.st.assetTracking.dashboard.communication.DeviceManager
import com.st.assetTracking.dashboard.model.ApiKey
import com.st.assetTracking.dashboard.model.Device
import com.st.assetTracking.dashboard.model.DeviceProfile
import com.st.assetTracking.dashboard.model.LastDeviceLocations
import com.st.login.AuthData

class DeviceListRepository(private val authData: AuthData,
                           private val remoteDeviceListDao: DeviceListManager
                           ) {

    sealed class DeviceListLoading {
        object Loading : DeviceListLoading()
        data class Load(val devices: List<Device>) : DeviceListLoading()
        object IOError : DeviceListLoading()
        object UnknownError : DeviceListLoading()
    }

    /**
     * this live data require to the cloud the update list.
     */
    /*val devices: LiveData<DeviceListLoading> = liveData {
        emit(DeviceListLoading.Loading)
        when (val remoteData = remoteDeviceListDao.getDeviceList()) {
            is DeviceListManager.GetDeviceListResult.Success -> {
                emit(DeviceListLoading.Load(remoteData.devices))
            }
            is DeviceListManager.GetDeviceListResult.UnknownError -> {
                emit(DeviceListLoading.UnknownError)
            }
            is DeviceListManager.GetDeviceListResult.IOError -> {
                emit(DeviceListLoading.IOError)
            }
        }
    }*/
    private val mRegistrationStatus = MutableLiveData<DeviceListLoading>()
    val registrationStatus: MutableLiveData<DeviceListLoading>
        get() = mRegistrationStatus

    suspend fun buildRemoteDevices() {
        mRegistrationStatus.postValue(DeviceListLoading.Loading)
         when (val remoteData = remoteDeviceListDao.getDeviceList()) {
            is DeviceListManager.GetDeviceListResult.Success -> {
                mRegistrationStatus.postValue(DeviceListLoading.Load(remoteData.devices))
            }
            is DeviceListManager.GetDeviceListResult.UnknownError -> {
                mRegistrationStatus.postValue(DeviceListLoading.UnknownError)
            }
            is DeviceListManager.GetDeviceListResult.IOError -> {
                mRegistrationStatus.postValue(DeviceListLoading.IOError)
            }
        }
    }

    /**
     * create the repository to access data from a device with id [deviceId]
     */
    fun buildDeviceRepositoryFor(context: Context, deviceId: String): DeviceDataRepository {
        return DeviceDataRepository(
                remoteDeviceListDao.buildDeviceManager(authData, context),
                deviceId
        )
    }

    /**
     * create an object o upload or query the remote data generate by the device with id [deviceId]
     */
    fun buildRemoteDeviceManagerFor(context: Context, deviceId: String): DeviceManager {
        return remoteDeviceListDao.buildDeviceManager(authData, context)
    }

    /**
     * get the device with a specific id or null if not found
     * this function is querying the local db, it is upgrading the data downloading the it from the cloud
     * and trying again
     */
    suspend fun getDeviceWithId(id: String): Device? {
        when (val remoteData = remoteDeviceListDao.getDeviceList()) {
            is DeviceListManager.GetDeviceListResult.Success -> {
                remoteData.devices.forEach { remoteDevice ->
                    if(remoteDevice.id == id){
                        return remoteDevice
                    }
                }
            }
            else -> {}
        }
        //not found on local cache + error query the remote
        return null
    }

    /**
     * add a new device
     */
    suspend fun registerDevice(device: Device): Boolean {
        val registrationResult = remoteDeviceListDao.registerDevice(device)
        Log.i("AWS registration", "registrationResult=$registrationResult")
        return when (registrationResult) {
            DeviceListManager.RegisterDeviceResult.Success,
            DeviceListManager.RegisterDeviceResult.AlreadyRegistered -> {
                true
            }
            DeviceListManager.RegisterDeviceResult.InvalidId,
            DeviceListManager.RegisterDeviceResult.IOError,
            DeviceListManager.RegisterDeviceResult.UnknownError -> {
                Log.e("DeviceListRepository", "Error: $registrationResult")
                false
            }
        }
    }

    /**
     * Create and use API keys in order to upload data
     */
    suspend fun registerApiKey(): ApiKey {
        val apiKeyResult = remoteDeviceListDao.getApiKey()
        Log.i("RESULT_APIKEY", apiKeyResult.apiKey)
        return apiKeyResult
    }

    /**
     * Retrieve Device Profile
     */
    suspend fun getDeviceProfile(): List<DeviceProfile> {
        val deviceProfileResult = remoteDeviceListDao.getDeviceProfile()
        Log.i("RESULT_DEVICE_PROFILE", deviceProfileResult.toString())
        return deviceProfileResult
    }

    /**
     * Retrieve Default Device Profile
     */
    suspend fun getDefaultDeviceProfile(): List<DeviceProfile> {
        val deviceProfileResult = remoteDeviceListDao.getDefaultDeviceProfile()
        Log.i("RESULT_DEVICE_PROFILE", deviceProfileResult.toString())
        return deviceProfileResult
    }

    /**
     * Delete Device
     */
    suspend fun removeDeviceWithId(id: String) : Boolean{
        return when (remoteDeviceListDao.removeDevice(id)) {
            DeviceListManager.RegisterDeviceResult.Success -> true
            else -> false
        }
    }

    /**
     * GET Devices Last Locations
     */
    suspend fun getLastDevicesLocation(): List<LastDeviceLocations>? {
        return remoteDeviceListDao.getDevicesPositions()
    }
}
