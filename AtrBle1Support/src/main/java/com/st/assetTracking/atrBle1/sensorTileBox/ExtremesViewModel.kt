package com.st.assetTracking.atrBle1.sensorTileBox

import androidx.lifecycle.*
import com.st.assetTracking.atrBle1.sensorTileBox.settings.LogDataRepository
import com.st.assetTracking.data.SensorDataSample
import com.st.assetTracking.threshold.model.AcquisitionExtremes
import com.st.assetTracking.threshold.model.DataExtreme
import kotlinx.coroutines.launch
import java.util.*

internal class ExtremesViewModel(private val mRepository: LogDataRepository) : ViewModel() {

    class Factory(private val repository: LogDataRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ExtremesViewModel(repository) as T
        }
    }

    val logData = mRepository.dataSample

    lateinit var deviceId : String

    var askToSyncData: Boolean = true
        private set

    private val _dataExtreme = MutableLiveData<AcquisitionExtremes>()
    val dataExtreme: LiveData<AcquisitionExtremes>
        get() = _dataExtreme

    private fun newExtremeData(data: AcquisitionExtremes) {
        _dataExtreme.value = data
    }

    fun loadData() {
        viewModelScope.launch {
            val state = mRepository.dataSample.value
            //get device uid and start loading the data if there was and error or no data is available
            if (state == LogDataRepository.LoadingProgress.Unknown || state == LogDataRepository.LoadingProgress.LoadingFailed) {
                deviceId = mRepository.getUID()
                askToSyncData = true
                mRepository.loadData()
            } else {
                askToSyncData = false
            }
        }
    }

    fun calcMinMax(sensorData: List<SensorDataSample>) {
        viewModelScope.launch {
            val minTemp: SensorDataSample?; val maxTemp: SensorDataSample?
            val minHum: SensorDataSample?; val maxHum: SensorDataSample?
            val minPre: SensorDataSample?; val maxPre: SensorDataSample?
            val minAcc: SensorDataSample?; val maxAcc: SensorDataSample?

            var tempDE: DataExtreme? = null; var humDE: DataExtreme? = null
            var preDE: DataExtreme? = null; var accDE: DataExtreme? = null


            val minDate: Date = if(sensorData.isNotEmpty()){
                sensorData[0].date
            }else{
                Calendar.getInstance().time
            }

            if(sensorData[0].temperature != null) {
                minTemp = sensorData.reduce(Compare::minTemp)
                maxTemp = sensorData.reduce(Compare::maxTemp)
                tempDE = DataExtreme(minTemp.date, minTemp.temperature!!, maxTemp.date, maxTemp.temperature!!)
            }

            if(sensorData[0].humidity != null) {
                minHum = sensorData.reduce(Compare::minHum)
                maxHum = sensorData.reduce(Compare::maxHum)
                humDE = DataExtreme(minHum.date, minHum.humidity!!, maxHum.date, maxHum.humidity!!)
            }

            if(sensorData[0].pressure != null) {
                minPre = sensorData.reduce(Compare::minPre)
                maxPre = sensorData.reduce(Compare::maxPre)
                preDE = DataExtreme(minPre.date, minPre.pressure!!, maxPre.date, maxPre.pressure!!)
            }

            if(sensorData[0].acceleration != null) {
                minAcc = sensorData.reduce(Compare::minAcc)
                maxAcc = sensorData.reduce(Compare::maxAcc)
                accDE = DataExtreme(minAcc.date, minAcc.acceleration!!, maxAcc.date, maxAcc.acceleration!!)
            }

            val extremes = AcquisitionExtremes(minDate, tempDE, preDE, humDE, accDE)
            newExtremeData(extremes)
        }
    }

    internal object Compare {
        fun minTemp(a: SensorDataSample, b: SensorDataSample): SensorDataSample {
            return if (a.temperature!! < b.temperature!!) a else b
        }
        fun maxTemp(a: SensorDataSample, b: SensorDataSample): SensorDataSample {
            return if (a.temperature!! > b.temperature!!) a else b
        }

        fun minHum(a: SensorDataSample, b: SensorDataSample): SensorDataSample {
            return if (a.humidity!! < b.humidity!!) a else b
        }
        fun maxHum(a: SensorDataSample, b: SensorDataSample): SensorDataSample {
            return if (a.humidity!! > b.humidity!!) a else b
        }

        fun minPre(a: SensorDataSample, b: SensorDataSample): SensorDataSample {
            return if (a.pressure!! < b.pressure!!) a else b
        }
        fun maxPre(a: SensorDataSample, b: SensorDataSample): SensorDataSample {
            return if (a.pressure!! > b.pressure!!) a else b
        }

        fun minAcc(a: SensorDataSample, b: SensorDataSample): SensorDataSample {
            return if (a.acceleration!! < b.acceleration!!) a else b
        }
        fun maxAcc(a: SensorDataSample, b: SensorDataSample): SensorDataSample {
            return if (a.acceleration!! > b.acceleration!!) a else b
        }

    }
}