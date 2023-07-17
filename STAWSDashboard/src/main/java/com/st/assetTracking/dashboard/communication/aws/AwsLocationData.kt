package com.st.assetTracking.dashboard.communication.aws

import com.beust.klaxon.Json
import com.st.assetTracking.dashboard.model.Device
import com.st.assetTracking.dashboard.model.LocationData
import java.util.*
import kotlin.collections.ArrayList

internal data class AwsLocationData(
        @Json(name = "device_id")
        val device_id: String,
        @Json(name = "values")
        val values: List<GeoValues>
) {
    internal fun toLocationData(): ArrayList<LocationData> {
            val listLocationData: ArrayList<LocationData> = ArrayList()
            values.forEach{
                    listLocationData.add(LocationData(it.v.lat, it.v.lon, Date(it.ts)))
            }
            return listLocationData
    }
}

internal data class GeoValues(
        @Json(name = "ts")
        val ts: Long,
        @Json(name = "t")
        val t: String,
        @Json(name = "v")
        val v: LatLonAlt
)

internal data class LatLonAlt(
        @Json(name = "lon")
        val lon: Float,
        @Json(name = "lat")
        val lat: Float,
        @Json(name = "ele")
        val ele: Float
)
