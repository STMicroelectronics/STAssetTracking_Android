package com.st.assetTracking.dashboard.communication.aws

import com.beust.klaxon.Json
import com.beust.klaxon.JsonObject
import com.st.assetTracking.dashboard.model.LocationData
import java.util.*
import kotlin.collections.ArrayList

internal data class AwsConfigurationSignaled(
        @Json(name = "timestamp")
        val timestamp: Long? = null,
        @Json(name = "data")
        val data: DevicesLastData? = null

) {
        internal fun toLocationData(): ArrayList<LocationData> {
                val listLocationData: ArrayList<LocationData> = ArrayList()
                /*data?.forEach {
                    listLocationData.add(LocationData(it.geolocation.v.lat, it.geolocation.v.lon, Date(it.geolocation.ts)))
                }*/
                return listLocationData
        }
}

internal data class DevicesLastData(
        @Json(name = "telemetry")
        val telemetry: List<Telem>? = null,
        @Json(name = "geolocation")
        val geolocation: List<GeolocationValues>? = null,
        @Json(name = "events")
        val events: List<Even>? = null
)

internal data class GeolocationValues(
        @Json(name = "ts")
        val ts: Long? = null,
        @Json(name = "t")
        val t: String? = null,
        @Json(name = "v")
        val v: LatLonAltValues? = null
)

internal data class LatLonAltValues(
        @Json(name = "lon")
        val lon: Float? = null,
        @Json(name = "lat")
        val lat: Float? = null,
        @Json(name = "ele")
        val ele: Float? = null
)


internal data class Telem(
        @Json(name = "t")
        val t: String? = null,
        @Json(name = "v")
        val v: Any?,
        @Json(name = "ts")
        val ts: Long? = null
)

internal data class Even(
        @Json(name = "t")
        val t: String? = null,
        @Json(name = "v")
        val v: EvenDetail? = null,
        @Json(name = "ts")
        val ts: Long? = null
)

internal data class EvenDetail(
        @Json(name = "m")
        val m: String? = null,
        @Json(name = "et")
        val et: String? = null,
        @Json(name = "t")
        val t: String? = null
)
