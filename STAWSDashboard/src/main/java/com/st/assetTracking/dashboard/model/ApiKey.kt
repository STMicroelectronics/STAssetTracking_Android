package com.st.assetTracking.dashboard.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "ApiKey")
data class ApiKey(
        @ColumnInfo(name = "owner")
        val owner: String,
        @ColumnInfo(name = "create_timestamp")
        val create_timestamp: Long,
        @ColumnInfo(name = "enabled_update_timestamp")
        val enabled_update_timestamp: Long,
        @ColumnInfo(name = "label")
        val label: String,
        @PrimaryKey
        val apiKey: String,
        @ColumnInfo(name = "enabled")
        val enabled: Boolean
)