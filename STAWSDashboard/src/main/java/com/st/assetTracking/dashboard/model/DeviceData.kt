package com.st.assetTracking.dashboard.model

import com.st.assetTracking.data.DataSample
import com.st.assetTracking.data.GenericDataSample

data class DeviceData(val telemetryData: List<DataSample>,
                               val locationData: List<LocationData>) {

    val isEmpty: Boolean
        get() = telemetryData.isEmpty() && locationData.isEmpty()

}

data class DeviceGenericData(val genericData: List<GenericDataSample>,
                      val locationData: List<LocationData>)