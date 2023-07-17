package com.st.assetTracking.dashboard.communication

import com.google.gson.JsonArray
import com.st.assetTracking.dashboard.model.ApiKey
import com.st.assetTracking.dashboard.model.DeviceData
import com.st.assetTracking.dashboard.model.DeviceGenericData
import com.st.assetTracking.dashboard.model.LocationData
import com.st.assetTracking.data.DataSample
import com.st.assetTracking.data.GenericDSHSample
import retrofit2.Response
import java.util.*

interface DeviceManager {

    sealed class DeviceDataResult {
        data class Success(val data: DeviceData) : DeviceDataResult()
        object IOError : DeviceDataResult()
        object UnknownError : DeviceDataResult()

        val isSuccess: Boolean
            get() = this is Success
    }

    sealed class DeviceGenericDataResult {
        data class Success(val data: DeviceGenericData) : DeviceGenericDataResult()
        object IOError : DeviceGenericDataResult()
        object UnknownError : DeviceGenericDataResult()

        val isSuccess: Boolean
            get() = this is Success
    }

    enum class SaveDataResult {
        Success,
        InvalidData,
        IOError,
        UnknownError
    }

    suspend fun getAllDeviceDataFor(geolocationData: JsonArray?, telemetryData: JsonArray?, eventData : JsonArray?): DeviceDataResult

    suspend fun getGeolocationDataFor(deviceId: String, range: ClosedRange<Date>): Response<JsonArray>
    suspend fun getTelemetryDataFor(deviceId: String, range: ClosedRange<Date>): Response<JsonArray>
    suspend fun getEventDataFor(deviceId: String, range: ClosedRange<Date>): Response<JsonArray>

    suspend fun uploadNewTelemetryData(deviceId: String, technology: String, data: List<DataSample>, currentLocation: LocationData? = null): SaveDataResult

    suspend fun getAllDeviceGenericDataFor(boardID: Int?, firmwareID: Int?, geolocationData: JsonArray?, telemetryData: JsonArray?): DeviceGenericDataResult
    suspend fun uploadNewGenericData(apiKey: ApiKey, deviceId: String, technology: String, data: List<GenericDSHSample>, currentLocation: LocationData? = null): SaveDataResult
}