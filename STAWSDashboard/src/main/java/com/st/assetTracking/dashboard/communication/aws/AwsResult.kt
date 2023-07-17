package com.st.assetTracking.dashboard.communication.aws

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import com.beust.klaxon.Json
import com.google.gson.annotations.SerializedName

internal data class AwsResult<T>(
        @Json(name = "statusCode")
        @SerializedName("statusCode")
        val statusCode: Int,
        @Json(name = "body")
        @SerializedName("body")
        val body: T,
        @Json(name = "headers")
        @SerializedName("headers")
        val headers: Map<String, String>
)