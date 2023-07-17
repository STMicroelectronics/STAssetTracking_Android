/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *    and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *    conditions and the following disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *    STMicroelectronics company nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *    in a directory whose title begins with st_images may only be used for internal purposes and
 *    shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *    icons, pictures, logos and other images that are provided with the source code in a directory
 *    whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package com.st.nfcSmarTag.v1.tagPlotData

import androidx.fragment.app.FragmentActivity
import android.util.Log
import androidx.lifecycle.*
import com.st.assetTracking.data.*
import com.st.smartaglibrary.v1.model.*

class TagDataViewModel : ViewModel() {

    private val _numberSample = MutableLiveData<Int>()
    /**
     * number of sensor and event sample read from the tag
     */
    val numberSample: LiveData<Int>
        get() = _numberSample


    private val _isUpdating = MutableLiveData<Boolean>()

    val isUpdating : LiveData<Boolean>
        get() = _isUpdating

    private val _allSampleList = MutableLiveData<MutableList<NFCDataSample>>()
    private val _allSampleListAWS = MutableLiveData<MutableList<DataSample>>()

    /**
     * list of [numberSample] sample read from the tag
     */
    val allSampleList: LiveData<MutableList<NFCDataSample>>
        get() = _allSampleList
    val allSampleListAWS: LiveData<MutableList<DataSample>>
        get() = _allSampleListAWS

    private val _sensorSampleList = MutableLiveData<MutableList<NFCSensorDataSample>>()
    private val _sensorSampleListAWS = MutableLiveData<MutableList<SensorDataSample>>()

    /**
     * list with all the sensor data read from the tag
     * @note the event samples with an acceleration will be mapped as a sensor sample with only the acceleration data
     */
    val sensorSampleList: LiveData<MutableList<NFCSensorDataSample>>
        get() = _sensorSampleList

    private val _lastSensorSample = MutableLiveData<NFCSensorDataSample>()

    val sensorSampleListAWS: LiveData<MutableList<SensorDataSample>>
        get() = _sensorSampleListAWS

    private val _lastSensorSampleAWS = MutableLiveData<SensorDataSample>()

    /**
     * last sensor sample read from the board
     */
    val lastSensorSample: LiveData<NFCSensorDataSample>
        get() = _lastSensorSample

    private val _eventSampleList = MutableLiveData<MutableList<NFCEventDataSample>>()

    val lastSensorSampleAWS: LiveData<SensorDataSample>
        get() = _lastSensorSampleAWS

    private val _eventSampleListAWS = MutableLiveData<MutableList<EventDataSample>>()

    /**
     * list with all the event data sample read from the tag
     */
    val eventSampleList: LiveData<MutableList<NFCEventDataSample>>
        get() = _eventSampleList

    private val _lastEventSample = MutableLiveData<NFCEventDataSample>()

    val eventSampleListAWS: LiveData<MutableList<EventDataSample>>
        get() = _eventSampleListAWS

    private val _lastEventSampleAWS = MutableLiveData<EventDataSample>()

    /**
     * last event sensor read from the board
     */
    val lastEventSample: LiveData<NFCEventDataSample>
        get() = _lastEventSample
    val lastEventSampleAWS: LiveData<EventDataSample>
        get() = _lastEventSampleAWS


    private fun appendSensorSample( sensorSample: NFCSensorDataSample){
        _lastSensorSample.value = sensorSample

        val ssAWS = SensorDataSample(sensorSample.date,sensorSample.temperature,sensorSample.pressure,sensorSample.humidity,sensorSample.acceleration,null)
        _lastSensorSampleAWS.value = ssAWS

        _sensorSampleList.value?.add(sensorSample)
        _sensorSampleListAWS.value?.add(ssAWS)

        _allSampleListAWS.value?.add(ssAWS)
    }

    /**
     * append the event data and create a new sensor sample if the acceleration data is available
     */
    private fun appendEventDataSample(eventSample: NFCEventDataSample){

        _eventSampleList.value?.add(eventSample)
        _lastEventSample.value = eventSample

        val eventsAWS : ArrayList<AccelerationEvent> = ArrayList()
        val orientationAWS : Orientation

        eventSample.events.forEach {
            when (it) {
                NFCAccelerationEvent.ACCELERATION_WAKE_UP -> {
                    eventsAWS.add(AccelerationEvent.ACCELERATION_WAKE_UP)
                }
                NFCAccelerationEvent.ORIENTATION -> {
                    eventsAWS.add(AccelerationEvent.ORIENTATION)
                }
                NFCAccelerationEvent.SINGLE_TAP -> {
                    eventsAWS.add(AccelerationEvent.SINGLE_TAP)
                }
                NFCAccelerationEvent.DOUBLE_TAP -> {
                    eventsAWS.add(AccelerationEvent.DOUBLE_TAP)
                }
                NFCAccelerationEvent.FREE_FALL -> {
                    eventsAWS.add(AccelerationEvent.FREE_FALL)
                }
                NFCAccelerationEvent.ACCELERATION_TILT_35 -> {
                    eventsAWS.add(AccelerationEvent.ACCELERATION_TILT_35)
                }
            }
        }

        when (eventSample.currentOrientation) {
            NFCOrientation.UNKNOWN -> {
                orientationAWS = Orientation.UNKNOWN
            }
            NFCOrientation.UP_RIGHT -> {
                orientationAWS = Orientation.UP_RIGHT
            }
            NFCOrientation.TOP -> {
                orientationAWS = Orientation.TOP
            }
            NFCOrientation.DOWN_LEFT -> {
                orientationAWS = Orientation.DOWN_LEFT
            }
            NFCOrientation.BOTTOM -> {
                orientationAWS = Orientation.BOTTOM
            }
            NFCOrientation.UP_LEFT -> {
                orientationAWS = Orientation.UP_LEFT
            }
            NFCOrientation.DOWN_RIGHT -> {
                orientationAWS = Orientation.DOWN_RIGHT
            }
            //if it has the acceleration data, create also a sensor sample
        }

        val esAWS = EventDataSample(eventSample.date,eventSample.acceleration, eventsAWS.toArray(arrayOfNulls(eventsAWS.size)), orientationAWS)

        _eventSampleListAWS.value?.add(esAWS)
        _lastEventSampleAWS.value = esAWS
        //if it has the acceleration data, create also a sensor sample
        if(eventSample.acceleration!=null) {
            val accelerationSample = NFCSensorDataSample(eventSample.date,
                    null, null, null, eventSample.acceleration?.toFloat())
            appendSensorSample(accelerationSample)
        }

        _allSampleListAWS.value?.add(esAWS)

    }

    fun appendSample(sensorSample: NFCDataSample) {
        _allSampleList.value?.add(sensorSample)
        _isUpdating.value = _allSampleList.value?.size != _numberSample.value
        Log.d("VM",""+_allSampleList.value?.size +"->"+_numberSample.value)

        when(sensorSample){
            is NFCSensorDataSample -> {
                appendSensorSample(sensorSample)
            }
            is NFCEventDataSample -> {
                appendEventDataSample(sensorSample)
            }
        }
    }

    /**
     * set the new sample number
     * this method will also reset all the sensor list
     */
    fun setNumberSample(num: Int) {
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
    }

    companion object {
        fun create(activity: FragmentActivity): TagDataViewModel {
            return ViewModelProvider(activity).get(TagDataViewModel::class.java)
        }
    }
}
