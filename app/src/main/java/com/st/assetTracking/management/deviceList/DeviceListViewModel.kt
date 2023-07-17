package com.st.assetTracking.management.deviceList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.st.assetTracking.dashboard.persistance.DeviceListRepository

internal class DeviceListViewModel(deviceListRepository: DeviceListRepository) : ViewModel() {

    /*
    private val mDeviceListStatus = MutableLiveData<DeviceListStatus>(DeviceListStatus.Retrieving)
    val deviceListStatus: LiveData<DeviceListStatus>
        get() = mDeviceListStatus
*/
    var deviceListStatus = deviceListRepository.registrationStatus

    suspend fun reloadDB(deviceListRepository: DeviceListRepository) {
        deviceListRepository.buildRemoteDevices()
        //deviceListStatus = deviceListRepository.devices
    }

    suspend fun deleteItem(deviceListRepository: DeviceListRepository, deviceID: String): Boolean {
        //reloadDB(deviceListRepository)
        return deviceListRepository.removeDeviceWithId(deviceID)
    }

    class Factory(private val deviceListRepository: DeviceListRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DeviceListViewModel(deviceListRepository) as T
        }
    }
}