package com.st.assetTracking.dashboard.communication.aws

import com.github.marlonlom.utilities.timeago.TimeAgoMessages.Builder
import android.os.Build
import androidx.annotation.RequiresApi
import com.beust.klaxon.Json
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.github.marlonlom.utilities.timeago.TimeAgoMessages
import com.st.assetTracking.dashboard.model.*
import java.util.*


internal data class AwsDevice(
        @Json(name = "id")
        val id: String,
        @Json(name = "attributes")
        val attributes: Attributes,
        @Json(name = "configuration")
        val configuration: String? = null,
        @Json(name = "configuration_signaled")
        val configuration_signaled: AwsConfigurationSignaled? = null,
        @Json(name = "certificate")
        var certificate: String?=null,
        @Json(name = "selfSigned")
        var selfSigned: Boolean=false,
        @Json(name = "type")
        val type: String? = null
) {
        @RequiresApi(Build.VERSION_CODES.N)
        fun toDevice(): Device {
                return Device(this.id, attributes.label, Device.Type.strToDeviceType(attributes.technology) ?: Device.Type.UNKNOWN, toHumanDate(this.configuration_signaled?.timestamp), this.configuration, this.configuration_signaled.toString(), this.certificate, this.selfSigned, this.attributes.device_profile, this.attributes.device_mac, null, toLastTelemetryData("tem", this.configuration_signaled?.data?.telemetry), toLastTelemetryData("pre", this.configuration_signaled?.data?.telemetry), toLastTelemetryData("hum", this.configuration_signaled?.data?.telemetry),attributes.boardID?.toInt(), attributes.firmwareID?.toInt())
        }

        @RequiresApi(Build.VERSION_CODES.N)
        fun toHumanDate(ts: Long?) : String {
                var timeStampText = "Never Seen"
                if(ts!=null) {
                        val languageToSet: Locale = Locale.forLanguageTag("en")
                        val messages: TimeAgoMessages = Builder().withLocale(languageToSet).build()
                        timeStampText = TimeAgo.using(ts, messages)
                }
                return timeStampText
        }

        private fun toLastTelemetryData(measure: String, lastTelemetryData: List<Telem>?) : String? {
                var value: String? = null
                lastTelemetryData?.forEach { telemetry ->
                        if(telemetry.t == measure) { value = telemetry.v.toString() }
                }
                return value
        }

}

internal data class Attributes(
        @Json(name = "application")
        val application: String,
        @Json(name = "board_id")
        val boardID: String? = null,
        @Json(name = "firmware_id")
        val firmwareID: String? = null,
        @Json(name = "converter")
        val converter: String? = null,
        @Json(name = "converter_owner")
        val converter_owner: String? = null,
        @Json(name = "device_profile")
        val device_profile: String? = null,
        @Json(name = "device_profile_owner")
        val device_profile_owner: String? = null,
        @Json(name = "device_mac")
        val device_mac: String? = null,
        @Json(name = "label")
        val label: String?,
        @Json(name = "owner")
        val owner: String? = null,
        @Json(name = "technology")
        val technology: String
)