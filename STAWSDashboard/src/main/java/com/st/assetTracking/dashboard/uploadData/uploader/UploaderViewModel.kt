package com.st.assetTracking.dashboard.uploadData.uploader

import android.os.Build
import androidx.lifecycle.*
import com.st.assetTracking.dashboard.communication.DeviceManager
import com.st.assetTracking.dashboard.model.LocationData
import com.st.assetTracking.dashboard.util.LocationService
import com.st.assetTracking.data.DataSample
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.util.*

/**
 * ViewModel that is reading the current position and uploading the data to the cloud
 * @param locationService object used to read the mobile position
 * @param deviceManager object used to upload the data
 * @param data data to upload
 */
internal class UploaderViewModel(
    private val deviceId: String,
    private val locationService: LocationService,
    private val deviceManager: DeviceManager,
    private val technology: String,
    private val data: List<DataSample>) : ViewModel() {

    enum class Status {
        RetrieveCurrentLocation,
        UploadData,
        UploadComplete,
        UploadError
    }

    private val mUploadStatus = MutableLiveData<Status>()

    /**
     * current upload status
     */
    val uploadStatus: LiveData<Status>
        get() = mUploadStatus

    init {
        viewModelScope.launch {
            mUploadStatus.postValue(Status.RetrieveCurrentLocation)
            var location = locationService.currentLocation()?.let {
                LocationData(it.latitude.toFloat(), it.longitude.toFloat(), Date(it.time))
            }
            /*if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                location = LocationData(0.0f,0.0f,Date())
            }*/
            //val location = LocationData(0.0f,0.0f,Date())
            mUploadStatus.postValue(Status.UploadData)
            try {
                val result = deviceManager.uploadNewTelemetryData(deviceId, technology, data, location)
                if (result == DeviceManager.SaveDataResult.Success) {
                    mUploadStatus.postValue(Status.UploadComplete)
                } else {
                    mUploadStatus.postValue(Status.UploadError)
                }
            }catch (e:UnknownHostException){

            }

        }
    }

    class Factory(private val deviceId: String,
                  private val locationService: LocationService,
                  private val deviceManager: DeviceManager,
                  private val technology: String,
                  private val data: List<DataSample>) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return UploaderViewModel(deviceId, locationService, deviceManager,technology, data) as T
        }
    }

}