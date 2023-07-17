package com.st.smartaglibrary.v2.ext

import com.st.smartaglibrary.util.leUInt
import com.st.smartaglibrary.util.toLeUInt32
import java.util.*

/**
 * convert a date in the format compatible with the nfc v2 firmware (used in write)
*/
internal fun GregorianCalendar.packForV2():ByteArray{
    return (timeInMillis / 1000).toLeUInt32
}

/**
 * unpack 4B Complete Timestamp stored in tag at TIMESTAMP_ADDRESS = 0x02
 */
internal fun unpackTimeStamp(rawData: ByteArray): GregorianCalendar {
    val longDate = rawData.leUInt
    val gregorianDate = GregorianCalendar()
    gregorianDate.timeInMillis = longDate * 1000
    return gregorianDate
}

/**
 * unpack DeltaTime and SUM to Base Tag Timestamp (Used for Min Max Timestamps)
 */
internal fun unpackMinMaxTimeStamp(bitLength: Int, rawData: ByteArray, baseTagTimestamp: GregorianCalendar): GregorianCalendar {
    val rawDeltaTime =rawData.leUInt
    val shortDeltaTime = (rawDeltaTime and calculateBitMask(bitLength).toLong())
    val gregorianDate = GregorianCalendar()
    gregorianDate.timeInMillis = (shortDeltaTime * 60000) + baseTagTimestamp.timeInMillis
    return gregorianDate
}
private fun calculateBitMask(length: Int): Int{
    return (1 shl length) -1
}

/**
 * extract the data from a nfc v2 memory cell (Used for Data Sample Timestamps)
 */
internal fun unpackDataSampleTimeStamp(bitLength: Int, rawData: ByteArray, baseTagTimestamp: GregorianCalendar): GregorianCalendar {
    val rawDeltaTime =rawData.leUInt
    val deltaTime = rawDeltaTime.ushr(bitLength).toInt()
    val gregorianDate = GregorianCalendar()
    gregorianDate.timeInMillis = (deltaTime * 1000) + baseTagTimestamp.timeInMillis
    return gregorianDate
}