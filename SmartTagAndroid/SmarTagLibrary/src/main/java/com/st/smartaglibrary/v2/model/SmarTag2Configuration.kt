package com.st.smartaglibrary.v2.model

import com.st.smartaglibrary.v2.catalog.VirtualSensor
import com.st.smartaglibrary.util.toLeUInt32
import java.io.Serializable
import java.util.*

data class SmarTag2BaseInformation(
    val protocolVersion: Int,
    val protocolRevision: Int,
    val boardID: Int,
    val firmwareId: Int,
    val rfu: Int,
    var virtualSensorNumber: Int,
    var sampleTime: Int,
    val rawTs: Int,
    val baseTagTimeStamp: GregorianCalendar
): Serializable

data class SmarTag2Configuration(
    val smarTag2BaseInformation: SmarTag2BaseInformation,
    var virtualSensors: List<VirtualSensorConfiguration>,
    val virtualSensorsMinMax: List<VirtualSensorMinMax>
): Serializable

data class SmarTag2Extremes(
    val virtualSensorsMinMax: List<VirtualSensorMinMax>
): Serializable

data class SmartTag2ConfigurationToWrite(
    val rfu: Int,
    val virtualSensorNumber: Int,
    val sampleTime: Int,
    val timeStamp: Int,
    val virtualSensors: List<VirtualSensorConfiguration>,
)

/**
 * encode the sampling configuration into a  nfc memory cell.
 */
fun createVSCPack(vs: VirtualSensorConfiguration, vsCatalog: VirtualSensor): ByteArray {
    var bitLength = 0

    // ID
    var pckDataTemp = (vs.id and 0x07)
    bitLength += vsCatalog.threshold.bitLengthID.toInt()

    // MOD
    if(vs.thUsageType != null) {
        pckDataTemp =
            pckDataTemp or ((vs.thUsageType!! and calculateBitMask(vsCatalog.threshold.bitLengthMod.toInt())) shl (bitLength)) //MOD
        bitLength += vsCatalog.threshold.bitLengthMod.toInt()
    }

    // Threshold Min
    if(vs.thMin != null){
        val min = calculateValueToPack(vs.thMin!!.toFloat(), vsCatalog.threshold.offset?.toFloat(), vsCatalog.threshold.scaleFactor?.toFloat())
        //val min = vs.thMin.toInt()
        if(vsCatalog.threshold.thLow.bitLength != null){
            pckDataTemp =
                pckDataTemp or ((min and calculateBitMask(vsCatalog.threshold.thLow.bitLength.toInt())) shl (bitLength)) //MIN
            bitLength += vsCatalog.threshold.thLow.bitLength.toInt()
        }
    }

    // Threshold Max
    if(vs.thMax != null){
        val max = calculateValueToPack(vs.thMax!!.toFloat(), vsCatalog.threshold.offset?.toFloat(), vsCatalog.threshold.scaleFactor?.toFloat())
        //val max =vs.thMax.toInt()
        if(vsCatalog.threshold.thHigh != null) {
            if (vsCatalog.threshold.thHigh.bitLength != null) {
                pckDataTemp =
                    pckDataTemp or ((max and calculateBitMask(vsCatalog.threshold.thHigh.bitLength.toInt())) shl (bitLength)) //MIN
                bitLength += vsCatalog.threshold.thHigh.bitLength.toInt()
            }
        }
    }

    return pckDataTemp.toLong().toLeUInt32
}

/** Function that sum NEGATIVE OFFSET and divide for SCALE FACTOR */
private fun calculateValueToPack(v: Float, negativeOffset: Float?, scaleFactor: Float?): Int {
    // Example --> ((23.5f+10)*5).toInt()
    return if(negativeOffset != null && scaleFactor != null){
        ((v-negativeOffset)/scaleFactor).toInt()
    } else {
        v.toInt()
    }
}

private fun calculateBitMask(length: Int): Int{
    return (1 shl length) -1
}