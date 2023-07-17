package com.st.assetTracking.dashboard.communication.aws

import com.beust.klaxon.Json
import com.st.assetTracking.dashboard.model.ApiKey


internal data class AwsApiKey(
        @Json(name = "owner")
        val owner: String,
        @Json(name = "create_timestamp")
        val create_timestamp: Long,
        @Json(name = "enabled_update_timestamp")
        val enabled_update_timestamp: Long,
        @Json(name = "label")
        val label: String,
        @Json(name = "apikey")
        val apikey: String,
        @Json(name = "enabled")
        val enabled: Boolean
) {
    fun toApiKey(): ApiKey {
        return ApiKey(owner,create_timestamp,enabled_update_timestamp,label,apikey,enabled)
    }

}

