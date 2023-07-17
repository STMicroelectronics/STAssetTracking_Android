package com.st.assetTracking.dashboard.deviceProfile

import android.content.Context
import androidx.lifecycle.*
import com.st.assetTracking.dashboard.communication.DeviceManager
import com.st.assetTracking.dashboard.communication.aws.AwsAssetTrackingService
//import com.st.assetTracking.dashboard.persistance.AssetTrackingDashboardDB
import com.st.assetTracking.dashboard.persistance.DeviceListRepository
import com.st.login.AuthData

/**
 * view model to manage the data uploading
 */
internal class DeviceProfileViewModel(
        val deviceId: String,
        val deviceType: String
        ) : ViewModel() {

    internal sealed class Destination {

        /**
         * create the device
         */
        object CreateDevice : Destination()

        /**
         * uploaded completed
         */
        object GetDeviceProfileComplete : Destination()

        /**
         * initial state, check if the user is log in
         */
        object Logined : Destination()
    }

    private val _currentView = MutableLiveData<Destination>(Destination.Logined)

    /**
     * view to display
     */
    val currentView: LiveData<Destination>
        get() = _currentView

    /**
     * object used to check if the device is known
     */
    var deviceListRepository: DeviceListRepository? = null
        private set

    /**
     * object to use to upload the data
     */
    var deviceManager: DeviceManager? = null
        private set

    fun onLoginComplete(loginData: AuthData, context: Context) {
        val deviceListRemote = AwsAssetTrackingService(loginData, context)
        deviceListRepository = DeviceListRepository(
            loginData,
            deviceListRemote
        )
        _currentView.postValue(Destination.CreateDevice)
    }

    fun onDeviceRegistered(context: Context, deviceId: String) {
        deviceManager = deviceListRepository?.buildRemoteDeviceManagerFor(context, deviceId)
        _currentView.postValue(Destination.GetDeviceProfileComplete)
    }



    class Factory(
            private val deviceId: String,
            private val deviceType: String
            ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DeviceProfileViewModel(
                deviceId,
                deviceType
            ) as T
        }
    }

}