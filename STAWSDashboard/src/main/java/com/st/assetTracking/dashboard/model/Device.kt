package com.st.assetTracking.dashboard.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "Device")
data class Device(
        @PrimaryKey
        val id: String,
        @ColumnInfo(name = "Name")
        val name: String?,
        @ColumnInfo(name = "Type")
        val type: Type,
        @ColumnInfo(name = "lastActivity")
        val lastActivity: String?,
        @ColumnInfo(name = "Configuration")
        val configuration: String?,
        @ColumnInfo(name = "configurationSignaled")
        val configurationSignaled: String?,
        @ColumnInfo(name = "certificate")
        var certificate: String?,
        @ColumnInfo(name = "selfSigned")
        var selfSigned: Boolean,
        @ColumnInfo(name = "deviceProfile")
        val deviceProfile: String?,
        @ColumnInfo(name = "mac")
        val mac: String?,
        @ColumnInfo(name = "devEui")
        val devEui: String?,
        @ColumnInfo(name = "lastTemperatureData")
        val lastTemperatureData: String?,
        @ColumnInfo(name = "lastPressureData")
        val lastPressureData: String?,
        @ColumnInfo(name = "lastHumidityData")
        val lastHumidityData: String?,
        @ColumnInfo(name = "boardID")
        val boardID: Int?,
        @ColumnInfo(name = "firmwareID")
        val firmwareID: Int?
) {
        /*enum class Type {nfc, lora, sigfox, ble, unknown, Sigfox, lte, wifi, Lora_ttn, http, polaris}*/
        enum class Type : Serializable {
                ASTRA, NFCTAG2, NFCTAG1, SENSORTILEBOX, SENSORTILEBOXPRO, LORA_TTN, UNKNOWN;

                override fun toString(): String {
                        return when (this) {
                                ASTRA -> "steval-astra1b:mcu:fp-atr-astra1:1"
                                NFCTAG2 -> "smartag2:mcu:fp-sns-smartag2:1"
                                NFCTAG1 -> "smartag1:mcu:fp-sns-smartag1:1"
                                SENSORTILEBOX -> "sensortile-box:mcu:fp-atr-ble1:1"
                                SENSORTILEBOXPRO -> "sensortile-box-pro:mcu:fp-atr-ble1:1"
                                LORA_TTN -> "Lora_ttn"
                                UNKNOWN -> "unknown"
                        }
                }

                companion object {
                        fun strToDeviceType(value: String): Type? {
                                return if (value.equals(ASTRA.toString(), ignoreCase = true))
                                        ASTRA
                                else if (value.equals(NFCTAG2.toString(), ignoreCase = true))
                                        NFCTAG2
                                else if (value.equals(NFCTAG1.toString(), ignoreCase = true))
                                        NFCTAG1
                                else if (value.equals(SENSORTILEBOX.toString(), ignoreCase = true))
                                        SENSORTILEBOX
                                else if (value.equals(SENSORTILEBOXPRO.toString(), ignoreCase = true))
                                        SENSORTILEBOXPRO
                                else if (value.equals(LORA_TTN.toString(), ignoreCase = true))
                                        LORA_TTN
                                else if (value.equals(UNKNOWN.toString(), ignoreCase = true))
                                        UNKNOWN
                                else null
                        }
                }
        }
}