package com.st.nfcSmarTag.v2.samples

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.st.assetTracking.data.GenericSample

class Tag2SamplesViewModel: ViewModel() {

    companion object {
        fun create(activity: FragmentActivity): Tag2SamplesViewModel {
            return ViewModelProvider(activity).get(Tag2SamplesViewModel::class.java)
        }
    }

    /**
     * number of sensor and event sample read from the tag
     */
    private val _numberSample = MutableLiveData<Int>()
    val numberSample: LiveData<Int>
        get() = _numberSample

    private val _isUpdating = MutableLiveData<Boolean>()
    val isUpdating : LiveData<Boolean>
        get() = _isUpdating

    val allSampleList: LiveData<MutableList<GenericSample>>
        get() = _allSampleList
    private val _allSampleList = MutableLiveData<MutableList<GenericSample>>()

    /**
     * last sensor sample read from the board
     */
    val lastSensorSample: LiveData<GenericSample?>
        get() = _lastSensorSample
    private val _lastSensorSample = MutableLiveData<GenericSample?>()


    /**
     * set the new sample number
     * this method will also reset all the sensor list
     */
    /*fun setNumberSample(num: Int) {
        _numberSample.value = num
        cleanSampleData()
    }

    private fun cleanSampleData() {
        _sensorSampleList.value = mutableListOf()
        _eventSampleList.value = mutableListOf()
        _allSampleList.value = mutableListOf()
        _lastSensorSample.value = null
        _lastEventSample.value = null
        _isUpdating.value=false

        _sensorSampleListAWS.value = mutableListOf()
        _eventSampleListAWS.value = mutableListOf()
        _allSampleListAWS.value = mutableListOf()
        _lastSensorSampleAWS.value = null
        _lastEventSampleAWS.value = null
    }*/

    /**
     * set the new sample number
     * this method will also reset all the sensor list
     */
    fun setNumberSample(num: Int) {
        _numberSample.value = num
        cleanSampleData()
    }

    private fun cleanSampleData() {
        _allSampleList.value = mutableListOf()
        _isUpdating.value=false
        _lastSensorSample.value = null
    }

    /**
     * used to append new GenericSample retrieved
     */
    fun appendSample(sensorSample: GenericSample) {
        _allSampleList.value?.add(sensorSample)
        _isUpdating.value = _allSampleList.value?.size != _numberSample.value
        Log.d("VM",""+_allSampleList.value?.size +"->"+_numberSample.value)
        _lastSensorSample.value = sensorSample
    }
}