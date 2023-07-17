package com.st.smartaglibrary.v2.catalog

import com.google.gson.annotations.SerializedName
import java.util.*

data class NfcV2BoardCatalog(
    @SerializedName("nfc_v2")
    var nfcV2FirmwareList: List<NfcV2Firmware>,
    @SerializedName("date")
    val date: Date,
    @SerializedName("version")
    val version: String,
    @SerializedName("checksum")
    val checksum: String
)

data class NfcV2Firmware (
    @SerializedName("nfc_dev_id")
    val nfcDevID: String,
    @SerializedName("nfc_fw_id")
    val nfcFwID: String,
    @SerializedName("brd_name")
    val brdName: String,
    @SerializedName("fw_name")
    val fwName: String,
    @SerializedName("fw_version")
    val fwVersion: String,
    @SerializedName("bit_length_virtual_sensors_id")
    val bitLengthVirtualSensorsId: String,
    @SerializedName("virtual_sensors")
    val virtualSensors: List<VirtualSensor>
): java.io.Serializable

data class VirtualSensor (
    @SerializedName("sensor_name")
    val sensorName: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("display_name")
    val displayName: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("incompatibility")
    val incompatibility: List<Incompatibility>,
    @SerializedName("plottable")
    val plottable: Boolean,
    @SerializedName("threshold")
    val threshold: Threshold,
    @SerializedName("max_min_format")
    val maxMinFormat: List<MaxMinFormat>? = null,
    @SerializedName("sample_format")
    val sampleFormat: List<SampleFormat>? = null
)

data class Incompatibility (
    @SerializedName("id")
    val id: Int
)

data class MaxMinFormat (
    @SerializedName("type")
    val type: String,
    @SerializedName("format")
    val format: FormatClass
)

data class FormatClass (
    @SerializedName("display_name")
    val displayName: String? = null,
    @SerializedName("format_type")
    val formatType: String? = null,
    @SerializedName("bit_length")
    val bitLength: Long,
    @SerializedName("comment")
    val comment: String? = null,
    @SerializedName("offset")
    val offset: Long? = null,
    @SerializedName("scale_factor")
    val scaleFactor: Double? = null,
    @SerializedName("unit")
    val unit: String? = null
)

data class SampleFormat (
    @SerializedName("type")
    val type: String,
    @SerializedName("format")
    val format: ThresholdDetail? = null
)

data class Threshold (
    @SerializedName("bit_length_id")
    val bitLengthID: Long,
    @SerializedName("bit_length_mod")
    val bitLengthMod: Long,
    @SerializedName("offset")
    val offset: Long? = null,
    @SerializedName("scale_factor")
    val scaleFactor: Double? = null,
    @SerializedName("min")
    val min: Long? = null,
    @SerializedName("max")
    val max: Long? = null,
    @SerializedName("unit")
    val unit: String? = null,
    @SerializedName("th_low")
    val thLow: ThresholdDetail,
    @SerializedName("th_high")
    val thHigh: ThresholdDetail? = null
)

data class ThresholdDetail (
    @SerializedName("display_name")
    val displayName: String? = null,
    @SerializedName("format")
    val format: String? = null,
    @SerializedName("bit_length")
    val bitLength: Long? = null,
    @SerializedName("comment")
    val comment: String? = null,
    @SerializedName("string_values")
    val enumStringValues: List<EnumStringValues>? = null
)

data class EnumStringValues (
    @SerializedName("display_name")
    val displayName: String? = null,
    @SerializedName("value")
    val value: Int
) {
    override fun toString(): String {
        return displayName ?: ""
    }
}