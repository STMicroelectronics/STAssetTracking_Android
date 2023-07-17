package com.st.assetTracking.atrBle1.communication

import com.st.assetTracking.atrBle1.sensorTileBox.communication.CommandResponse
import com.st.assetTracking.atrBle1.sensorTileBox.communication.CommandResponse.Status
import org.junit.Assert.*
import org.junit.Test
import java.lang.IllegalArgumentException

class CommandResponseTest {

    @Test
    fun doneResponseIsParseCorrectly() {
        val payload = "message"
        val response = CommandResponse("Done - $payload")
        assertEquals(Status.Done, response.status)
        assertEquals(payload, response.payload)
    }

    @Test
    fun errorResponseIsParseCorrectly() {
        val payload = "message"
        val response = CommandResponse("Error - $payload")
        assertEquals(Status.Error, response.status)
        assertEquals(payload, response.payload)
    }

    @Test
    fun warningResponseIsParseCorrectly() {
        val payload = "message"
        val response = CommandResponse("Warning - $payload")
        assertEquals(Status.Warning, response.status)
        assertEquals(payload, response.payload)
    }

    @Test(expected = IllegalArgumentException::class)
    fun emptyStringIsInvalid() {
        CommandResponse("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun stringWithoutTheDashIsInvalid() {
        CommandResponse("Done message")
    }

    @Test(expected = IllegalArgumentException::class)
    fun wrongStateIsInvalid() {
        CommandResponse("Almost Done - message")
    }

}