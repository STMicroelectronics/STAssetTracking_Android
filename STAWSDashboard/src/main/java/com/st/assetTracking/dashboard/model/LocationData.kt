package com.st.assetTracking.dashboard.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

data class LocationData(
        @SerializedName("latitude")
        val latitude: Float,
        @SerializedName("longitude")
        val longitude: Float,
        @SerializedName("date")
        val date: Date) : Serializable