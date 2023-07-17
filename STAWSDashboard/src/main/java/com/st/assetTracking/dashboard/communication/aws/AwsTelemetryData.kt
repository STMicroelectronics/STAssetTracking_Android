package com.st.assetTracking.dashboard.communication.aws

import com.beust.klaxon.Json
import com.st.assetTracking.data.DataSample
import com.st.assetTracking.data.GenericDataSample
import com.st.assetTracking.data.SensorDataSample
import com.st.smartaglibrary.v2.catalog.NFCBoardCatalogService
import java.util.*


internal data class AwsTelemetryData(
        @Json(name = "device_id")
        val device_id: String,
        @Json(name = "values")
        val values: List<TelemetryValues>

) {

    internal fun toTelemetryData(): ArrayList<DataSample> {
        val listSDS: ArrayList<DataSample> = ArrayList()
        values.forEach {
            when (it.t) {
                "tem" -> {
                    listSDS.add(SensorDataSample(Date(it.ts), it.v.toString().toFloat(), null, null, null, null))
                }
                "pre" -> {
                    listSDS.add(SensorDataSample(Date(it.ts), null, it.v.toString().toFloat(), null, null, null))
                }
                "hum" -> {
                    listSDS.add(SensorDataSample(Date(it.ts), null, null, it.v.toString().toFloat(), null, null))
                }
                "acc" -> {
                    //listSDS.add(SensorDataSample(Date(it.ts), null, null, null, it.v))
                }
                else -> listSDS.add(SensorDataSample(Date(it.ts), null, null, null, null, null))
            }
        }
        return listSDS
    }

    internal fun toGenericData(boardID: Int, firmwareID: Int): ArrayList<GenericDataSample> {
        val currentFw = NFCBoardCatalogService.getCurrentFirmware(boardID,firmwareID)
        var thID: Int? = null
        val listGenericSamples: ArrayList<GenericDataSample> = ArrayList()
        values.forEach {
            if(currentFw != null) {
                thID = NFCBoardCatalogService.getCurrentThresholdIdFromName(currentFw, it.t)
                if(thID!=null){
                    listGenericSamples.add(GenericDataSample(thID!!, it.t, Date(it.ts), it.v.toString().toDouble()))
                } else {
                    listGenericSamples.add(GenericDataSample(0, it.t, Date(it.ts), it.v.toString().toDouble()))
                }
            }

        }
        return listGenericSamples
    }

}

internal data class TelemetryValues(
        @Json(name = "ts")
        val ts: Long,
        @Json(name = "t")
        val t: String,
        @Json(name = "v")
        val v: Any
)

data class AccelerometerValues(
        @Json(name = "x")
        val x: Float,
        @Json(name = "y")
        val y: Float,
        @Json(name = "z")
        val z: Float
)