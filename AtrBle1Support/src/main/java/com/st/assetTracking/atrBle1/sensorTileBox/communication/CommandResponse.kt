package com.st.assetTracking.atrBle1.sensorTileBox.communication

import java.lang.IllegalArgumentException
import java.util.Locale
import java.util.regex.Pattern

class CommandResponse {

    enum class Status {
        Done,
        Warning,
        Error
    }

    val status: Status
    val payload: String

    @Throws(IllegalArgumentException::class)
    constructor(message: String) {
        val match = RESPONSE_REGEXP.matcher(message)
        if (match.find()) {
            status = match.group(1)!!.toStatus()
            payload = match.group(2)!!
        } else {
            throw IllegalArgumentException("$message is not a valid response")
        }
    }

    val isNotError: Boolean
        get() = status != Status.Error

    companion object {
        val RESPONSE_REGEXP: Pattern = Pattern.compile("(.*) - (.*)")
    }
}

private fun String.toStatus(): CommandResponse.Status {
    return when (this.toLowerCase(Locale.ROOT)) {
        "done" -> CommandResponse.Status.Done
        "warning" -> CommandResponse.Status.Warning
        "error" -> CommandResponse.Status.Error
        else -> throw IllegalArgumentException("$this is not a valid response status")
    }
}
