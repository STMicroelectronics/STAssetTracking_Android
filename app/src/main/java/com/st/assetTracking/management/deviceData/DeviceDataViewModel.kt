package com.st.assetTracking.management.deviceData

import androidx.lifecycle.*
import com.st.assetTracking.dashboard.persistance.DeviceDataRepository
import java.util.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.days
import kotlin.time.hours

class DeviceDataViewModel(private val deviceDataRepository: DeviceDataRepository, private val boardID: Int?, private val firmwareID: Int?) : ViewModel() {

    private var mRepositoryLiveData: LiveData<DeviceDataRepository.Result>? = null

    private val mDeviceData = MediatorLiveData<DeviceDataRepository.Result>()
    val deviceDataStatus: LiveData<DeviceDataRepository.Result>
        get() = mDeviceData

    private var mRepositoryGenericLiveData: LiveData<DeviceDataRepository.GenericResult>? = null

    private val mDeviceGenericData = MediatorLiveData<DeviceDataRepository.GenericResult>()
    val deviceGenericDataStatus: LiveData<DeviceDataRepository.GenericResult>
        get() = mDeviceGenericData

    private fun getDeviceData(since: Date) {
        mRepositoryLiveData?.let {
            mDeviceData.removeSource(it)
        }
        mRepositoryLiveData = deviceDataRepository.getDataSince(since)
        mDeviceData.addSource(mRepositoryLiveData!!) { result ->
            val currentData = mDeviceData.value
            if (result is DeviceDataRepository.Result.Complete && currentData is DeviceDataRepository.Result.Partial) {
                if (result.data != currentData.data) {
                    //update the data only if the deviceData are different to avoid ui flikering
                    mDeviceData.postValue(result)
                }
            } else {
                mDeviceData.postValue(result)
            }

        }
    }

    private fun getGenericDeviceData(since: Date, boardID: Int?, firmwareID: Int?) {
        mRepositoryGenericLiveData?.let {
            mDeviceGenericData.removeSource(it)
        }
        mRepositoryGenericLiveData = deviceDataRepository.getGenericDataSince(since, boardID, firmwareID)
        mDeviceGenericData.addSource(mRepositoryGenericLiveData!!) { result ->
            val currentData = mDeviceGenericData.value
            if (result is DeviceDataRepository.GenericResult.Complete && currentData is DeviceDataRepository.GenericResult.Partial) {
                if (result.data != currentData.data) {
                    //update the data only if the deviceData are different to avoid ui flikering
                    mDeviceGenericData.postValue(result)
                }
            } else {
                mDeviceGenericData.postValue(result)
            }

        }
    }

    @ExperimentalTime
    fun getDataFromLast3Hours(generic: Boolean) {
        if(!generic) {
            val time = System.currentTimeMillis() - THREE_HOURS.toLongMilliseconds()
            getDeviceData(Date(time))
        } else {
            val time = System.currentTimeMillis() - THREE_HOURS.toLongMilliseconds()
            getGenericDeviceData(Date(time), boardID, firmwareID)
        }
    }

    @ExperimentalTime
    fun getDataFromLast6Hours(generic: Boolean) {
        if(!generic) {
            val time = System.currentTimeMillis() - SIX_HOURS.toLongMilliseconds()
            getDeviceData(Date(time))
        } else {
            val time = System.currentTimeMillis() - THREE_HOURS.toLongMilliseconds()
            getGenericDeviceData(Date(time), boardID, firmwareID)
        }
    }

    @ExperimentalTime
    fun getDataFromLast24Hours(generic: Boolean) {
        if(!generic) {
            val time = System.currentTimeMillis() - ONE_DAY.toLongMilliseconds()
            getDeviceData(Date(time))
        } else {
            val time = System.currentTimeMillis() - THREE_HOURS.toLongMilliseconds()
            getGenericDeviceData(Date(time), boardID, firmwareID)
        }
    }

    @ExperimentalTime
    fun getDataFromLast48Hours(generic: Boolean) {
        if(!generic) {
            val time = System.currentTimeMillis() - TWO_DAYS.toLongMilliseconds()
            getDeviceData(Date(time))
        } else {
            val time = System.currentTimeMillis() - THREE_HOURS.toLongMilliseconds()
            getGenericDeviceData(Date(time), boardID, firmwareID)
        }
    }

    @ExperimentalTime
    fun getDataFromLastWeek(generic: Boolean) {
        if (!generic) {
            val time = System.currentTimeMillis() - SEVEN_DAYS.toLongMilliseconds()
            getDeviceData(Date(time))
        } else {
            val time = System.currentTimeMillis() - THREE_HOURS.toLongMilliseconds()
            getGenericDeviceData(Date(time), boardID, firmwareID)
        }
    }


    class Factory(private val deviceManager: DeviceDataRepository, private val boardID: Int?, private val firmwareID: Int?) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DeviceDataViewModel(deviceManager, boardID, firmwareID) as T
        }
    }

    @ExperimentalTime
    companion object {
        private val THREE_HOURS: Duration = 3.hours
        private val SIX_HOURS: Duration = 6.hours
        private val ONE_DAY: Duration = 1.days
        private val TWO_DAYS: Duration = 2.days
        private val SEVEN_DAYS: Duration = 7.days
    }
}