package com.st.smartaglibrary.v2.model

import com.st.smartaglibrary.util.leUInt
import com.st.smartaglibrary.v2.catalog.NFCTag2CurrentFw
import com.st.smartaglibrary.v2.catalog.NfcV2BoardCatalog
import com.st.smartaglibrary.v2.catalog.NfcV2Firmware
import com.st.smartaglibrary.v2.catalog.VirtualSensor
import java.io.Serializable
import java.util.*

/**
 * content of a cell containing a Virtual Sensor Value
 */
data class VirtualSensorConfiguration(
    val id:Int,
    var enabled: Boolean = false,
    val sensorName: String?,
    val thresholdName: String?,
    var thUsageType: Int?,
    var thMin: Double?,
    var thMax: Double?
): Serializable

data class VirtualSensorMinMax(
    val id:Int?,
    val sensorName: String?,
    val thresholdName: String?,
    var minValue: MinVirtualSensorValue? = null,
    var maxValue: MaxVirtualSensorValue? = null
): Serializable

data class MinVirtualSensorValue(
    val timestamp: Date?,
    val value: Double?
): Serializable

data class MaxVirtualSensorValue(
    val timestamp: Date?,
    val value: Double?
): Serializable

data class IncompatibleVirtualSensors (
    val id1: Int,
    val id2: Int
): Serializable

/**
 * extract the Virtual Sensor data from a memory cell read from the nfc tag
 */
internal fun unpackVirtualSensor(rawData:ByteArray, catalog: NfcV2Firmware): VirtualSensorConfiguration {

    val intDataValue = rawData.leUInt.toInt()

    var virtualSensorId: Int? = null
    var thUsageType: Int? = null
    var th1: Double? = null
    var th2: Double? = null

    virtualSensorId = (intDataValue and 0x07) //TODO: Modify Catalog in order to add parameter instead of 0x07 hardcoded value
    val virtualSensorCatalog =
       retrieveVirtualSensorFromCatalog(
            catalog,
            virtualSensorId
        )

    if(virtualSensorCatalog != null){
        val idLength = virtualSensorCatalog.threshold.bitLengthID.toInt()
        val modLength = virtualSensorCatalog.threshold.bitLengthMod.toInt()
        val thLowLength = virtualSensorCatalog.threshold.thLow.bitLength?.toInt()
        val thHighLength = virtualSensorCatalog.threshold.thHigh?.bitLength?.toInt()

        thUsageType = ((intDataValue ushr idLength) and calculateBitMask(
            modLength
        ))

        when (thUsageType) {
            0 -> {
                if(thLowLength != null && thHighLength != null) {
                    val thLow = ((intDataValue ushr idLength + modLength) and calculateBitMask(
                        thLowLength
                    ))
                    th1 = th1ValueWithOffsetAndScaleFactor(thLow, virtualSensorCatalog)
                    val thHigh = ((intDataValue ushr idLength + modLength + thLowLength) and calculateBitMask(
                        thHighLength
                    ))
                    th2 = th2ValueWithOffsetAndScaleFactor(thHigh, virtualSensorCatalog)
                }
            }
            1 -> {
                if(thLowLength != null && thHighLength != null) {
                    val thLow = ((intDataValue ushr idLength + modLength) and calculateBitMask(
                        thLowLength
                    ))
                    th1 = th1ValueWithOffsetAndScaleFactor(thLow, virtualSensorCatalog)
                    val thHigh = ((intDataValue ushr idLength + modLength + thLowLength) and calculateBitMask(
                        thHighLength
                    ))
                    th2 = th2ValueWithOffsetAndScaleFactor(thHigh, virtualSensorCatalog)
                }
            }
            2 -> {
                if(thLowLength != null) {
                    val thLow = ((intDataValue ushr idLength + modLength) and calculateBitMask(
                        thLowLength
                    ))
                    th1 = th1ValueWithOffsetAndScaleFactor(thLow, virtualSensorCatalog)
                    th2 = null
                }
                if(thHighLength != null) {
                    val thHigh = ((intDataValue ushr idLength + modLength) and calculateBitMask(
                        thHighLength
                    ))
                    th1 = th2ValueWithOffsetAndScaleFactor(thHigh, virtualSensorCatalog)
                    th2 = null
                }
            }
            3 -> {
                if(thLowLength != null) {
                    val thLow = ((intDataValue ushr idLength + modLength) and calculateBitMask(
                        thLowLength
                    ))
                    th1 = th1ValueWithOffsetAndScaleFactor(thLow, virtualSensorCatalog)
                    th2 = null
                }
                if(thHighLength != null) {
                    val thHigh = ((intDataValue ushr idLength + modLength) and calculateBitMask(
                        thHighLength
                    ))
                    th1 = th2ValueWithOffsetAndScaleFactor(thHigh, virtualSensorCatalog)
                    th2 = null
                }
            }
        }
    }

    return VirtualSensorConfiguration(
        virtualSensorId,
        true,
        virtualSensorCatalog?.sensorName,
        virtualSensorCatalog?.displayName,
        thUsageType,
        th1,
        th2
    )
}

internal fun unpackVirtualSensorMinMaxSingleValue(rawData: ByteArray, length: Int, previousLength: Int?): Int {
    val intDataValue = rawData.leUInt.toInt()

    return if(previousLength == null) {
        (intDataValue and calculateBitMask(length))
    } else {
        ((intDataValue ushr previousLength) and calculateBitMask(
            length
        ))
    }
}

internal fun unpackVirtualSensorSingleValue(rawData: ByteArray, length: Int, previousLength: Int?, vsc: VirtualSensor): Double {
    val intDataValue = rawData.leUInt.toInt()
    return if(previousLength == null) {
        val v = (intDataValue and calculateBitMask(length))
        th1ValueWithOffsetAndScaleFactor(v, vsc)
    } else {
        val v = ((intDataValue ushr previousLength) and calculateBitMask(length))
        th2ValueWithOffsetAndScaleFactor(v, vsc)
    }
}

private fun calculateBitMask(length: Int): Int{
    return (1 shl length) -1
}

fun findCurrentFirmwareFromCatalog(catalog: NfcV2BoardCatalog, boardID: Int, fwID: Int): NfcV2Firmware? {
    catalog.nfcV2FirmwareList.forEach { fw ->
        if(java.lang.Long.decode(fw.nfcDevID).toInt()== boardID && java.lang.Long.decode(fw.nfcFwID).toInt() == fwID){
            NFCTag2CurrentFw.saveCurrentFw(fw)
            return fw
        }
    }
    return null
}

fun retrieveVirtualSensorFromCatalog(currentFw: NfcV2Firmware, virtualSensorId: Int): VirtualSensor?{
    currentFw.virtualSensors.forEach { vSc ->
        if(vSc.id == virtualSensorId){
            return vSc
        }
    }
    return null
}

fun th1ValueWithOffsetAndScaleFactor(value: Int, vsc: VirtualSensor): Double{
    val negativeOffset = vsc.threshold.offset
    val scaleFactor =  vsc.threshold.scaleFactor
    return if(negativeOffset != null && scaleFactor != null) {
        (value * scaleFactor) + negativeOffset
    } else {
        value.toDouble()
    }
}

fun th2ValueWithOffsetAndScaleFactor(value: Int, vsc: VirtualSensor): Double{
    val negativeOffset = vsc.threshold.offset
    val scaleFactor =  vsc.threshold.scaleFactor
    return if(negativeOffset != null && scaleFactor != null) {
        (value * scaleFactor) + negativeOffset
    } else {
        value.toDouble()
    }
}

fun th1MinValueWithOffsetAndScaleFactor(value: Int, negativeOffset: Long?, scaleFactor: Double?): Double{
    return if(negativeOffset != null && scaleFactor != null) {
        (value * scaleFactor) + negativeOffset
    } else {
        value.toDouble()
    }
}

fun th2MaxValueWithOffsetAndScaleFactor(value: Int, negativeOffset: Long?, scaleFactor: Double?): Double{
    return if(negativeOffset != null && scaleFactor != null) {
        (value * scaleFactor) + negativeOffset
    } else {
        value.toDouble()
    }
}