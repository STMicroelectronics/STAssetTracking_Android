/*
 *  Copyright (c) 2019  STMicroelectronics – All rights reserved
 *  The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice, this list of conditions
 *    and the following disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright notice, this list of
 *    conditions and the following disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 *  - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *    STMicroelectronics company nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 *  - All of the icons, pictures, logos and other images that are provided with the source code
 *    in a directory whose title begins with st_images may only be used for internal purposes and
 *    shall not be redistributed to any third party or modified in any way.
 *
 *  - Any redistributions in binary form shall not include the capability to display any of the
 *    icons, pictures, logos and other images that are provided with the source code in a directory
 *    whose title begins with st_images.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 *  AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 *  OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 *  OF SUCH DAMAGE.
 */

package com.st.assetTracking.threshold.comunication

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.st.BlueSTSDK.Debug
import com.st.BlueSTSDK.Utils.NumberConversion
import com.st.assetTracking.threshold.model.SamplingSettings
import com.st.assetTracking.threshold.model.ThresholdComparison
import com.st.assetTracking.threshold.model.ThresholdSensorType


internal class CloudTrackerNucleoConsole(private val console: Debug) : CloudTrackerConsole {

    /**
     * Console listener used to store the settings into a node, check that [nByteToSent] bytes are sent
     * or call [CloudTrackerConsole.SaveCallback.onError()] if the timeout is fired
     */
    private inner class SaveCommandListener(private val nByteToSent: Int,
                                            private val userCallback: CloudTrackerConsole.SaveCallback) :
            Debug.DebugOutputListener {


        private val onTimeoutIsFired = Runnable {
            this@CloudTrackerNucleoConsole.console.removeDebugOutputListener(this)
            userCallback.onError()
        }

        private fun resetTimeout() {
            removeTimeout()
            this@CloudTrackerNucleoConsole.mTimeout.postDelayed(onTimeoutIsFired, COMMAND_TIMEOUT_MS)
        }

        private fun removeTimeout() {
            this@CloudTrackerNucleoConsole.mTimeout.removeCallbacks(onTimeoutIsFired)
        }

        private var nByteSent = 0

        override fun onStdOutReceived(debug: Debug, message: String) {
            Log.d("CloudTracker", "resp $message")
        }

        override fun onStdErrReceived(debug: Debug, message: String) {

        }

        override fun onStdInSent(debug: Debug, message: String, writeResult: Boolean) {
            Log.d("CloudTracker", "send $message")
            resetTimeout()
            nByteSent += Debug.stringToByte(message).size
            if (nByteSent == nByteToSent) {
                removeTimeout()
                debug.removeDebugOutputListener(this)
                userCallback.onSuccess()
            }
        }
    }

    /**
     *  Console listener used to read the current board settings when the reading ended it call the
     *  callback into the [userCallback] object
     */
    private inner class LoadCommandListener(
            private val userCallback: CloudTrackerConsole.LoadCallback) :
            Debug.DebugOutputListener {


        private val onTimeoutIsFired = Runnable {
            this@CloudTrackerNucleoConsole.console.removeDebugOutputListener(this)
            val settings = buffer?.toByteArray()?.toSamplingSettings()
            if (settings == null) {
                userCallback.onError()
            } else {
                userCallback.onSuccess(settings)
            }
        }

        private fun resetTimeout() {
            removeTimeout()
            this@CloudTrackerNucleoConsole.mTimeout.postDelayed(onTimeoutIsFired, COMMAND_TIMEOUT_MS)
        }

        private fun removeTimeout() {
            this@CloudTrackerNucleoConsole.mTimeout.removeCallbacks(onTimeoutIsFired)
        }

        private var buffer: MutableList<Byte>? = null
        private var nByteToRead = 0

        override fun onStdOutReceived(debug: Debug, message: String) {
            Log.d("CloudTracker", "resp $message")
            removeTimeout()
            val readByte = Debug.stringToByte(message)
            if (buffer == null) {
                nByteToRead = readByte.getSampleSettingsLength()
                buffer = ArrayList(nByteToRead)

            }
            buffer?.addAll(readByte.asIterable())

            if (buffer?.size == nByteToRead) { // message is complete
                onTimeoutIsFired.run()
            } else {
                resetTimeout()
            }
        }

        override fun onStdErrReceived(debug: Debug, message: String) {
        }

        override fun onStdInSent(debug: Debug, message: String, writeResult: Boolean) {
            //when the read command is sent, initialize the variable where store the received bytes
            Log.d("CloudTracker", "send $message")
            resetTimeout()
            buffer = null
            nByteToRead = 0
        }
    }

    private val mTimeout = Handler(Looper.getMainLooper())

    private fun prepareSetCommand(settings: SamplingSettings): ByteArray {
        return SET_COMMAND + settings.toByteArray()
    }

    override fun save(settings: SamplingSettings, callback: CloudTrackerConsole.SaveCallback) {
        val settingsData = prepareSetCommand(settings)
        console.addDebugOutputListener(SaveCommandListener(settingsData.size, callback))
        console.write(settingsData)
    }

    override fun load(callback: CloudTrackerConsole.LoadCallback) {
        console.addDebugOutputListener(LoadCommandListener(callback))
        console.write(GET_COMMAND)
    }

    companion object {
        private val SET_COMMAND = "setTracking".toByteArray()
        private val GET_COMMAND = "getTracking\n".toByteArray()
        private const val COMMAND_TIMEOUT_MS = 1000L
    }

}

private fun SamplingSettings.toByteArray(): ByteArray {

    val nThreshold = threshold.size.toByte()
    //the number of threshold is encoded as byte, so we can take only the first nThreshold values
    val thresholdDataList = threshold.subList(0, nThreshold.toInt())
            .map { it.toByteArray() } // convert to byte
    val nByteForThreshold = thresholdDataList.fold(0) { length, data -> length + data.size }
    val settingsData = ByteArray(nByteForThreshold + 3)
    settingsData[0] = this.cloudSyncInterval.toByte()
    settingsData[1] = this.samplingInterval.toByte()
    settingsData[2] = nThreshold
    var offset = 3
    thresholdDataList.forEach {
        System.arraycopy(it, 0, settingsData, offset, it.size)
        offset += it.size
    }

    return settingsData
}


private const val SAMPLING_INTERVAL_OFFSET = 0
private const val CLOUD_SYNC_OFFSET = 1
private const val N_THRESHOLD_OFFSET = 2
private const val HEADER_SIZE = 3
private const val N_SENSOR_THRESHOLD_SIZE = 4

internal fun ByteArray.getSampleSettingsLength(): Int {
    return HEADER_SIZE + this[N_THRESHOLD_OFFSET] * N_SENSOR_THRESHOLD_SIZE
}

internal fun ByteArray.toSamplingSettings(): SamplingSettings? {
    try {
        val cloudSyncInterval = NumberConversion.byteToUInt8(this, CLOUD_SYNC_OFFSET)
        val samplingInterval = NumberConversion.byteToUInt8(this, SAMPLING_INTERVAL_OFFSET)
        val nThreshold = NumberConversion.byteToUInt8(this, N_THRESHOLD_OFFSET)
        val thresholdList = List(nThreshold.toInt()) { index ->
            val thOffset = HEADER_SIZE + index * N_SENSOR_THRESHOLD_SIZE
            toSensorThreshold(thOffset)
                    ?: throw IllegalAccessException("Impossible parse a Sensor threshold: index: $thOffset")
        }
        return SamplingSettings(cloudSyncInterval, samplingInterval, thresholdList)
    } catch (ex: Exception) {
        when (ex) {
            is IllegalAccessException, is IndexOutOfBoundsException -> {
                return null
            }
            else -> throw ex
        }
    }//try-catch
}


private fun ThresholdSensorType.toByte(): Byte {
    return when (this) {
        ThresholdSensorType.Temperature -> 0x01
        ThresholdSensorType.Pressure -> 0x02
        ThresholdSensorType.Humidity -> 0x03
        ThresholdSensorType.WakeUp -> 0x04
        ThresholdSensorType.Tilt -> 0x05
        ThresholdSensorType.Orientation -> 0x06
    }
}

private fun Byte.toThresholdSensorType(): ThresholdSensorType? {
    return when (this) {
        0x01.toByte() -> ThresholdSensorType.Temperature
        0x02.toByte() -> ThresholdSensorType.Pressure
        0x03.toByte() -> ThresholdSensorType.Humidity
        0x04.toByte() -> ThresholdSensorType.WakeUp
        0x05.toByte() -> ThresholdSensorType.Tilt
        0x06.toByte() -> ThresholdSensorType.Orientation
        else ->
            null
    }
}

private fun ThresholdComparison.toByte(): Byte {
    return when (this) {
        ThresholdComparison.Less -> -1
        ThresholdComparison.Equal -> 0
        ThresholdComparison.BiggerOrEqual -> 1
    }
}

private fun Byte.toThresholdComparison(): ThresholdComparison? {
    return when (this) {
        (-1).toByte() -> ThresholdComparison.Less
        0.toByte() -> ThresholdComparison.Equal
        1.toByte() -> ThresholdComparison.BiggerOrEqual
        else ->
            null
    }
}

private fun encodeThreshold(sensor: ThresholdSensorType, value: Float): ByteArray {
    return when (sensor) {
        ThresholdSensorType.Temperature -> {
            val intValue = (value * 10).toInt().toShort()
            NumberConversion.LittleEndian.int16ToBytes(intValue)
        }
        ThresholdSensorType.Pressure -> {
            val intValue = (value * 10).toInt()
            NumberConversion.LittleEndian.uint16ToBytes(intValue)
        }
        ThresholdSensorType.Humidity -> {
            val intValue = (value * 10).toInt()
            NumberConversion.LittleEndian.uint16ToBytes(intValue)
        }
        ThresholdSensorType.WakeUp -> {
            val intValue = (value).toInt()
            NumberConversion.LittleEndian.uint16ToBytes(intValue)
        }
        ThresholdSensorType.Tilt -> {
            val intValue = (value).toInt()
            NumberConversion.LittleEndian.uint16ToBytes(intValue)
        }
        ThresholdSensorType.Orientation -> {
            val intValue = (value).toInt()
            NumberConversion.LittleEndian.uint16ToBytes(intValue)
        }
    }
}

private fun ByteArray.decodeThresholdFromOffset(offset: Int, sensor: ThresholdSensorType): Float {
    return when (sensor) {
        ThresholdSensorType.Temperature -> {
            val intValue = NumberConversion.LittleEndian.bytesToInt16(this, offset)
            intValue / 10.0f
        }
        ThresholdSensorType.Pressure -> {
            val intValue = NumberConversion.LittleEndian.bytesToUInt16(this, offset)
            intValue / 10.0f
        }
        ThresholdSensorType.Humidity -> {
            val intValue = NumberConversion.LittleEndian.bytesToUInt16(this, offset)
            intValue / 10.0f
        }
        ThresholdSensorType.WakeUp -> {
            val intValue = NumberConversion.LittleEndian.bytesToUInt16(this, offset)
            intValue.toFloat()
        }
        ThresholdSensorType.Tilt -> {
            val intValue = NumberConversion.LittleEndian.bytesToUInt16(this, offset)
            intValue.toFloat()
        }
        ThresholdSensorType.Orientation -> {
            val intValue = NumberConversion.LittleEndian.bytesToUInt16(this, offset)
            intValue.toFloat()
        }
    }
}

private fun com.st.assetTracking.threshold.model.SensorThreshold.toByteArray(): ByteArray {
    return byteArrayOf(sensor.toByte(), comparison.toByte()) + encodeThreshold(sensor, threshold)
}

private fun ByteArray.toSensorThreshold(offset: Int = 0): com.st.assetTracking.threshold.model.SensorThreshold? {
    val sensor = get(offset).toThresholdSensorType()
    val comparison = get(offset + 1).toThresholdComparison()
    return if (sensor != null && comparison != null) {
        com.st.assetTracking.threshold.model.SensorThreshold(sensor, comparison, decodeThresholdFromOffset(offset + 2, sensor))
    } else
        null
}