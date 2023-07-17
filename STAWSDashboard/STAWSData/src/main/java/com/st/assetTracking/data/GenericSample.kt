package com.st.assetTracking.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/**
 * common class to store a data sample it can be a sensor data or an event data
 */
sealed class GenericSample: Serializable

/**
 * class containing a virtual sensor sampling recorded on [date].
 */
data class GenericDataSample(
    val id: Int,
    val type: String,
    val date: Date?,
    val value: Double?
) : GenericSample()

fun unpackVirtualSensorId(rawData:ByteArray): Int {
    val intDataValue = rawData.leUInt.toInt()
    return (intDataValue and 0x07)
}

fun List<GenericSample>.getSensorDataSample() : List<GenericDataSample> = filterIsInstance<GenericDataSample>()

val ByteArray.leUInt: Long
    get() {return this.extractUIntFrom(0)}
/**
 * extract a UInt32 value merging 4 consecutive bytes, the msb is in [index+3] , and the lsb in [index]
 */
fun ByteArray.extractUIntFrom(index: Int=0): Long {
    return (ByteBuffer.wrap(this, index, 4).order(ByteOrder.LITTLE_ENDIAN).int)
        .toLong() and 0xFFFFFFFFL
}

sealed class GenericDSHSample: Parcelable
@Parcelize
data class GenericDSHDataSample(
    val id: Int,
    val type: String,
    val date: Date?,
    val value: Double?
) : GenericDSHSample()
fun List<GenericDSHSample>.getGenericSample() : List<GenericDSHDataSample> = filterIsInstance<GenericDSHDataSample>()
