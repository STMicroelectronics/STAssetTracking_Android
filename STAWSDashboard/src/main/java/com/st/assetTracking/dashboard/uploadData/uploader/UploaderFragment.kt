package com.st.assetTracking.dashboard.uploadData.uploader

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.st.assetTracking.dashboard.R
import com.st.assetTracking.dashboard.communication.DeviceManager
import com.st.assetTracking.dashboard.uploadData.UploadDataNavigatorViewModel
import com.st.assetTracking.dashboard.uploadData.uploader.UploaderViewModel.Factory
import com.st.assetTracking.dashboard.uploadData.uploader.UploaderViewModel.Status
import com.st.assetTracking.dashboard.util.LocationService
import com.st.assetTracking.data.DataSample

/**
 * fragment that is reading the current position and uploading the data to the cloud
 * @param locationService object used to read the mobile position
 * @param deviceManager object used to upload the data
 * @param data data to upload
 */
//todo: data can be passed as fragment args?
//todo request permission for location, now it works becouse we need the location permission for the BLE
internal class UploaderFragment(deviceId: String, locationService: LocationService,
                                deviceManager: DeviceManager, technology: String, data: List<DataSample>) :
        Fragment(R.layout.fragment_upload_data) {

    //this will crash if navigationViewModel is not already instantiated
    private val mNavigationViewModel by activityViewModels<UploadDataNavigatorViewModel>()

    private val mUploaderViewModel by viewModels<UploaderViewModel> {
        Factory(deviceId, locationService, deviceManager, technology, data)
    }

    @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val text = view.findViewById<TextView>(R.id.dataUploader_statusText)
        mUploaderViewModel.uploadStatus.observe(viewLifecycleOwner, Observer { newStatus ->
            val textId = when (newStatus) {
                Status.RetrieveCurrentLocation -> R.string.dataUpload_location
                Status.UploadData -> R.string.dataUpload_uploadData
                Status.UploadComplete -> R.string.dataUpload_uploadDataComplete
                Status.UploadError -> R.string.dataUpload_uploadDataError
            }
            text.setText(textId)
            if (newStatus == Status.UploadComplete) {
                mNavigationViewModel.onUploadCompleted()
            }
        })
    }

}