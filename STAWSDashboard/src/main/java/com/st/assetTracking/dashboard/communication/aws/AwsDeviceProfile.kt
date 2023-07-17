package com.st.assetTracking.dashboard.communication.aws

import com.beust.klaxon.Json
import com.st.assetTracking.dashboard.model.DeviceProfile
import com.st.assetTracking.dashboard.model.DeviceProfileDetails

internal data class AwsDeviceProfile(
        @Json(name = "owner")
        val owner: String,
        @Json(name = "create_timestamp")
        val create_timestamp: Long,
        @Json(name = "technology")
        val technology: String,
        @Json(name = "id")
        val id: String,
        @Json(name = "context")
        val context: AwsDeviceProfileDetails,
        @Json(name = "converter")
        val converter: String

) {
    fun toDeviceProfile(): DeviceProfile {
        return DeviceProfile(owner,create_timestamp,technology,id,context.toDeviceProfileDetails(),converter)
    }

}

internal data class AwsDeviceProfileDetails(
        @Json(name = "access_key")
        val access_key: String? = null,
        @Json(name = "secret")
        val secret: String? = null,
        @Json(name = "region")
        val region: String? = null,
        @Json(name = "application_id")
        val application_id: String? = null,
        @Json(name = "application_eui")
        val application_eui: String? = null,
        @Json(name = "apikey")
        val apikey: String? = null,
        @Json(name = "application_key")
        val application_key: String? = null
        ) {
    fun toDeviceProfileDetails(): DeviceProfileDetails {
        return DeviceProfileDetails(access_key, secret, region,application_id,application_eui,application_key,apikey)
    }

}