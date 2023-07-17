package com.st.assetTracking.dashboard.uploadData

import android.content.Context
import androidx.lifecycle.*
import com.st.assetTracking.dashboard.communication.DeviceManager
import com.st.assetTracking.dashboard.communication.aws.AwsAssetTrackingService
import com.st.assetTracking.dashboard.persistance.DeviceListRepository
import com.st.assetTracking.data.DataSample
import com.st.login.AuthData

/**
 * view model to manage the data uploading
 */
internal class UploadDataNavigatorViewModel(
        val deviceId: String,
        val deviceData: List<DataSample>,
        val deviceType: String,
        val deviceTechnology: String
        ) : ViewModel() {

    internal sealed class Destination {

        /**
         * create the device
         */
        object CreateDevice : Destination()

        /**
         * upload the data
         */
        object UploadDeviceData : Destination()

        /**
         * uploaded completed
         */
        object UploadCompleted : Destination()

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
        _currentView.postValue(Destination.UploadDeviceData)
    }

    fun onUploadCompleted() {
        _currentView.postValue(Destination.UploadCompleted)
    }


    class Factory(
            private val deviceId: String,
            private val deviceData: List<DataSample>,
            private val deviceType: String,
            private val deviceTechnology: String,
            ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return UploadDataNavigatorViewModel(
                deviceId,
                deviceData,
                deviceType,
                deviceTechnology
            ) as T
        }
    }

}