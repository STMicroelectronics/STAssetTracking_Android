package com.st.assetTracking.dashboard.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "DeviceProfile")
data class DeviceProfile(
        @ColumnInfo(name = "owner")
        val owner: String,
        @ColumnInfo(name = "create_timestamp")
        val create_timestamp: Long,
        @ColumnInfo(name = "technology")
        val technology: String,
        @PrimaryKey
        val id: String,
        @ColumnInfo(name = "context")
        val context: DeviceProfileDetails,
        @ColumnInfo(name = "converter")
        val converter: String?
)

@Entity(tableName = "DeviceProfileDetails")
data class DeviceProfileDetails(
        @ColumnInfo(name = "access_key")
        val access_key: String?,
        @ColumnInfo(name = "secret")
        val secret: String?,
        @ColumnInfo(name = "region")
        val region: String?,
        @ColumnInfo(name = "application_id")
        val application_id: String?,
        @ColumnInfo(name = "application_eui")
        val application_eui: String?,
        @ColumnInfo(name = "application_key")
        val application_key: String?,
        @PrimaryKey
        val apiKey: String?
)