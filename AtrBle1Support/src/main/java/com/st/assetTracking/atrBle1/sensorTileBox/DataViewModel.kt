package com.st.assetTracking.atrBle1.sensorTileBox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.st.assetTracking.atrBle1.sensorTileBox.settings.LogDataRepository
import com.st.assetTracking.atrBle1.sensorTileBox.settings.LogDataRepository.LoadingProgress
import kotlinx.coroutines.launch

internal class DataViewModel(private val mRepository: LogDataRepository) : ViewModel() {


    class Factory(private val repository: LogDataRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return DataViewModel(repository) as T
        }
    }

    val logData = mRepository.dataSample

    lateinit var deviceId : String

    var askToSyncData: Boolean = true
        private set

    fun loadData() {
        viewModelScope.launch {
            val state = mRepository.dataSample.value
            //get device uid and start loading the data if there was and error or no data is available
            if (state == LoadingProgress.Unknown || state == LoadingProgress.LoadingFailed) {
                deviceId = mRepository.getUID()
                askToSyncData = true
                mRepository.loadData()
            } else {
                askToSyncData = false
            }
        }
    }
}