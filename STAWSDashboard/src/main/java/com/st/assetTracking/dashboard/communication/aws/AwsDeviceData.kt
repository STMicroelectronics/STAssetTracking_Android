package com.st.assetTracking.dashboard.communication.aws

import com.st.assetTracking.dashboard.model.DeviceData
import com.st.assetTracking.dashboard.model.DeviceGenericData
import com.st.assetTracking.dashboard.model.LocationData
import com.st.assetTracking.data.DataSample
import com.st.assetTracking.data.GenericDataSample

internal data class AwsDeviceData(
    val telemetryData: List<DataSample>,
    val locationData: List<LocationData>) { fun toDeviceData() = DeviceData(telemetryData, locationData) }

internal data class AwsDeviceGenericData(
    val telemetryData: List<GenericDataSample>,
    val locationData: List<LocationData>) { fun toDeviceData() = DeviceGenericData(telemetryData, locationData) }
