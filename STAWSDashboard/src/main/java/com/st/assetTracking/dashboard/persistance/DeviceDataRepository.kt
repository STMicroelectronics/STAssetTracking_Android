package com.st.assetTracking.dashboard.persistance

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.st.assetTracking.dashboard.communication.DeviceManager
import com.st.assetTracking.dashboard.model.DeviceData
import com.st.assetTracking.dashboard.model.DeviceGenericData
import com.st.assetTracking.data.GenericDataSample
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.*

class DeviceDataRepository(private val remoteDeviceDataDao: DeviceManager, val deviceId: String) {

    sealed class Result {
        object Loading : Result()
        data class Partial(val data: DeviceData) : Result()
        data class Complete(val data: DeviceData) : Result()
        data class Error(val partialResult: DeviceData?) : Result()
    }

    sealed class GenericResult {
        object Loading : GenericResult()
        data class Partial(val data: DeviceGenericData) : GenericResult()
        data class Complete(val data: DeviceGenericData) : GenericResult()
        data class Error(val partialResult: DeviceGenericData?) : GenericResult()
    }

    private suspend fun askAndStoreRemoteDataInRange(range: ClosedRange<Date>): DeviceManager.DeviceDataResult {

        val geolocationData = remoteDeviceDataDao.getGeolocationDataFor(deviceId, range)
        val telemetryData = remoteDeviceDataDao.getTelemetryDataFor(deviceId, range)
        val eventData = remoteDeviceDataDao.getEventDataFor(deviceId, range)

        return remoteDeviceDataDao.getAllDeviceDataFor(
            geolocationData.body(),
            telemetryData.body(),
            eventData.body()
        )
    }

    fun getDataSince(date: Date): LiveData<Result> = liveData {

        emit(Result.Loading)

        coroutineScope {
            val requestDataRange = date..Date()
            //load remote data
            val remoteResult = async { askAndStoreRemoteDataInRange(requestDataRange.start..requestDataRange.endInclusive) }
            //wait remote data
            when (val remoteData = remoteResult.await()) {
                is DeviceManager.DeviceDataResult.Success -> {
                    val a = remoteData.data
                    emit(Result.Complete(a))
                }
                else -> { emit(Result.Error(null)) }
            }
        }
    }

    private suspend fun askAndStoreRemoteGenericDataInRange(range: ClosedRange<Date>, boardID: Int?, firmwareID: Int?): DeviceManager.DeviceGenericDataResult {

        val geolocationData = remoteDeviceDataDao.getGeolocationDataFor(deviceId, range)
        val telemetryData = remoteDeviceDataDao.getTelemetryDataFor(deviceId, range)

        return remoteDeviceDataDao.getAllDeviceGenericDataFor(
            boardID,
            firmwareID,
            geolocationData.body(),
            telemetryData.body()
        )
    }

    fun getGenericDataSince(date: Date, boardID: Int?, firmwareID: Int?): LiveData<GenericResult> = liveData {

        emit(GenericResult.Loading)

        coroutineScope {
            val requestDataRange = date..Date()
            //load remote data
            val remoteResult = async { askAndStoreRemoteGenericDataInRange(requestDataRange.start..requestDataRange.endInclusive, boardID, firmwareID) }
            //wait remote data
            when (val remoteData = remoteResult.await()) {
                is DeviceManager.DeviceGenericDataResult.Success -> {
                    val a = remoteData.data
                    emit(GenericResult.Complete(a))
                }
                else -> { emit(GenericResult.Error(null)) }
            }
        }
    }
}