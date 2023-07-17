package com.st.assetTracking.dashboard.uploadData.uploader

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.st.assetTracking.dashboard.communication.DeviceManager
import com.st.assetTracking.dashboard.model.LocationData
import com.st.assetTracking.dashboard.persistance.DeviceListRepository
import com.st.assetTracking.dashboard.util.LocationService
import com.st.assetTracking.data.GenericDSHSample
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.util.*

/**
 * view model that manage the data upload on cloud
 * @param deviceId name of the device to register
 * @param deviceListRepository list of the know devices
 */
class UploaderGenericDataViewModel(
    val context: Context,
    val deviceId: String,
    val technology: String,
    val data: List<GenericDSHSample>,
    val locationService: LocationService,
    val deviceManager: DeviceManager,
    val deviceListRepository: DeviceListRepository
) : ViewModel() {

    enum class GenericUploadStatus {
        /**
         * check if the device is already known
         */
        ONLINE_CHECK,

        /**
         * retrieve API KEY
         */
        REGISTRATION_APIKEY,

        /**
         * retrieve CURRENT LOCATION
         */
        CURRENT_LOCATION,

        /**
         * Upload Generic Data Sample
         */
        UPLOAD_DATA,

        /**
         * device registration complete
         */
        COMPLETE,

        /**
         * registration error
         */
        FAILED,
    }

    private val mGenericUploadStatus = MutableLiveData<GenericUploadStatus>()
    val genericUploadStatus: LiveData<GenericUploadStatus>
        get() = mGenericUploadStatus

    /** Upload GENERIC Data */
    init {
        viewModelScope.launch {
            mGenericUploadStatus.postValue(GenericUploadStatus.ONLINE_CHECK)
            val device = deviceListRepository.getDeviceWithId(deviceId)

            if (device != null) {
                mGenericUploadStatus.postValue(GenericUploadStatus.REGISTRATION_APIKEY)
                val apiKey = deviceListRepository.registerApiKey()

                mGenericUploadStatus.postValue(GenericUploadStatus.CURRENT_LOCATION)
                val location = locationService.currentLocation()?.let {
                    LocationData(it.latitude.toFloat(), it.longitude.toFloat(), Date(it.time))
                }

                mGenericUploadStatus.postValue(GenericUploadStatus.UPLOAD_DATA)
                try {
                    val result = deviceManager.uploadNewGenericData(apiKey, deviceId, technology, data, location)
                    if (result == DeviceManager.SaveDataResult.Success) {
                        mGenericUploadStatus.postValue(GenericUploadStatus.COMPLETE)
                    } else {
                        mGenericUploadStatus.postValue(GenericUploadStatus.FAILED)
                    }
                }catch (e: UnknownHostException){
                    mGenericUploadStatus.postValue(GenericUploadStatus.FAILED)
                    Log.d("Upload Error", e.toString())
                }
            } else {
                mGenericUploadStatus.postValue(GenericUploadStatus.FAILED)
            }
        }
    }

    class Factory(
        private val context: Context,
        private val deviceId: String,
        private val technology: String,
        private val data: List<GenericDSHSample>,
        private val locationService: LocationService,
        private val deviceManager: DeviceManager,
        private val deviceListRepository: DeviceListRepository) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return UploaderGenericDataViewModel(context, deviceId, technology, data, locationService, deviceManager, deviceListRepository) as T
        }
    }

}