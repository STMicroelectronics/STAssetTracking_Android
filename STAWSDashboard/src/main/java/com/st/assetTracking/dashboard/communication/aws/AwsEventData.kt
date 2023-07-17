package com.st.assetTracking.dashboard.communication.aws

import com.beust.klaxon.Json
import com.beust.klaxon.JsonObject
import com.st.assetTracking.data.*
import java.util.*

internal data class AwsEventData(
        @Json(name = "device_id")
        val device_id: String,
        @Json(name = "values")
        val values: List<EventValues>

) {

    internal fun toEventData(): ArrayList<DataSample> {
        val listSDS: ArrayList<DataSample> = ArrayList()
        values.forEach{
            when (it.v.m) {
                "orientation" -> {
                    listSDS.add(EventDataSample(Date(it.ts), null, arrayOf(AccelerationEvent.ORIENTATION), toOrientationValue(it.v.l!!)))
                }
                "tilt" -> {
                    listSDS.add(EventDataSample(Date(it.ts), null, arrayOf(AccelerationEvent.ACCELERATION_TILT_35), Orientation.UNKNOWN))
                }
                "wakeup" -> {
                    listSDS.add(EventDataSample(Date(it.ts), null, arrayOf(AccelerationEvent.ACCELERATION_WAKE_UP), Orientation.UNKNOWN))
                }
                else -> listSDS.add(EventDataSample(Date(it.ts), null, arrayOf(AccelerationEvent.ORIENTATION), Orientation.UNKNOWN))
            }
        }
        return listSDS
    }

    private fun toOrientationValue(orientation: String): Orientation {
        return when (orientation) {
            "UP_RIGHT" -> Orientation.UP_RIGHT
            "TOP" -> Orientation.TOP
            "DOWN_LEFT" -> Orientation.DOWN_LEFT
            "BOTTOM" -> Orientation.BOTTOM
            "UP_LEFT" -> Orientation.UP_LEFT
            "DOWN_RIGHT" -> Orientation.DOWN_RIGHT
            else -> Orientation.UNKNOWN
        }
    }


}

internal data class EventValues(
        @Json(name = "ts")
        val ts: Long,
        @Json(name = "t")
        val t: String,
        @Json(name = "v")
        val v: DetailEventValue

)

internal data class DetailEventValue(
        @Json(name = "m")
        val m: String? = null,
        @Json(name = "l")
        val l: String? = null
        )
