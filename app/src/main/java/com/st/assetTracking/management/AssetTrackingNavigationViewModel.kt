package com.st.assetTracking.management

import android.content.Context
import androidx.lifecycle.*
import com.st.assetTracking.dashboard.communication.DeviceListManager
import com.st.assetTracking.dashboard.communication.aws.AwsAssetTrackingService

//import com.st.assetTracking.dashboard.persistance.AssetTrackingDashboardDB
import com.st.assetTracking.dashboard.persistance.DeviceDataRepository
import com.st.assetTracking.dashboard.persistance.DeviceListRepository
import com.st.login.AuthData

class AssetTrackingNavigationViewModel: ViewModel() {

    sealed class Destination {
        data class DeviceList(val authData: AuthData, val onlineDeviceList: DeviceListManager) : Destination()
        object Logined : Destination()
    }

    private val _currentView = MutableLiveData<Destination>(Destination.Logined)
    val currentView: LiveData<Destination>
        get() = _currentView

    var deviceListRepository: DeviceListRepository? = null
        private set

    fun moveTo(destination: Destination) {
        if (destination is Destination.DeviceList) {
            deviceListRepository = DeviceListRepository(
                destination.authData,
                destination.onlineDeviceList
            )
        }
        _currentView.postValue(destination)
    }

    fun onLogined(authData: AuthData, context: Context) {
        val deviceList = AwsAssetTrackingService(authData, context)
        moveTo(Destination.DeviceList(authData, deviceList))
    }

}