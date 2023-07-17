package com.st.smartaglibrary.v2

import android.util.Log
import com.st.assetTracking.data.GenericDataSample
import com.st.assetTracking.data.GenericSample
import com.st.assetTracking.data.unpackVirtualSensorId
import com.st.smartaglibrary.util.toLeUInt32
import com.st.smartaglibrary.v2.catalog.NFCBoardCatalogService
import com.st.smartaglibrary.v2.catalog.NFCTag2CurrentFw
import com.st.smartaglibrary.v2.catalog.NfcV2Firmware
import com.st.smartaglibrary.v2.ext.unpackDataSampleTimeStamp
import com.st.smartaglibrary.v2.ext.unpackLastSamplePointerInformation
import com.st.smartaglibrary.v2.ext.unpackMinMaxTimeStamp
import com.st.smartaglibrary.v2.ext.unpackSampleCounterInformation
import com.st.smartaglibrary.v2.model.*
import com.st.smartaglibrary.v2.model.unpackBaseSmarTag2Information
import com.st.smartaglibrary.v2.model.unpackVirtualSensor
import java.util.*
import kotlin.collections.ArrayList

class SmarTag2BLE (private val tagContent: LongArray) {

    private lateinit var baseInformation: SmarTag2BaseInformation
    private lateinit var configuration: SmarTag2Configuration
    private var sampleCounter: Int = 0
    private var lastSamplePointer: Int = 0

    private var addressToRead: Int = FIRST_VIRTUAL_SENSOR_POSITION

    /**
     * [0] Protocol Version (1B), Protocol Revision (1B), BoardID (1B), FwID(2B)
     * [1] RFU (1B), #VirtualSensor (1B), SampleTime (2B)
     * [2] TimeStamp (4B)
     * [3..#VirtualSensor] TH1 TH2 VSId Mod
     * [n..n+#VirtualSensor] TS Min TS Max
     * [] Sample Counter
     * [] Last Sample Pointer
     */
    private fun readTag2Configuration(baseInformation: SmarTag2BaseInformation): SmarTag2Configuration {
        val catalog = NFCBoardCatalogService.getCatalog()

        val currentFw = findCurrentFirmwareFromCatalog(catalog, baseInformation.boardID, baseInformation.firmwareId)

        val virtualSensorsValues: ArrayList<VirtualSensorConfiguration> = ArrayList()
        var virtualSensorsMinMax: ArrayList<VirtualSensorMinMax> = ArrayList()

        if(currentFw != null) {

            for (i in FIRST_VIRTUAL_SENSOR_POSITION until (FIRST_VIRTUAL_SENSOR_POSITION + baseInformation.virtualSensorNumber.toString(16).toShort())) {
                val rawData = tagContent[addressToRead]
                virtualSensorsValues.add(unpackVirtualSensor(rawData.toLeUInt32, currentFw))
                addressToRead = addressToRead.inc()
            }

            virtualSensorsMinMax = readMaxMinVirtualSensorValues(
                currentFw,
                baseInformation,
                virtualSensorsValues
            )
        }

        return SmarTag2Configuration(baseInformation, virtualSensorsValues.toList(), virtualSensorsMinMax.toList())
    }


    private fun readMaxMinVirtualSensorValues(catalog: NfcV2Firmware, smarTag2BaseInformation: SmarTag2BaseInformation, virtualSensorsValues: ArrayList<VirtualSensorConfiguration>) : ArrayList<VirtualSensorMinMax>{
        val virtualSensorsMinMax: ArrayList<VirtualSensorMinMax> = ArrayList()

        for (vsNumber in 0 until smarTag2BaseInformation.virtualSensorNumber) {
            val virtualSensorCatalog =
                retrieveVirtualSensorFromCatalog(
                    catalog,
                    virtualSensorsValues[vsNumber].id
                )

            var bitCounter = 0
            var previousLength: Int? = null

            var ts: Date? = null
            var min: Int? = null
            var max: Int? = null
            val minMaxVS = VirtualSensorMinMax(
                virtualSensorCatalog?.id,
                virtualSensorCatalog?.sensorName,
                virtualSensorCatalog?.displayName,
                null,
                null
            )

            virtualSensorCatalog?.maxMinFormat?.forEach { maxMinFormat ->
                val bitLength = maxMinFormat.format.bitLength.toInt()
                if(bitLength <= 32){
                    if (maxMinFormat.type.contains("time")){
                        ts = unpackMinMaxTimeStamp(bitLength, tagContent[addressToRead].toLeUInt32, smarTag2BaseInformation.baseTagTimeStamp).time
                        previousLength = bitLength
                    } else if (maxMinFormat.type.contains("min") || maxMinFormat.type.contains("Min")){
                        val negativeOffset = maxMinFormat.format.offset
                        val scaleFactor = maxMinFormat.format.scaleFactor
                        min =
                            unpackVirtualSensorMinMaxSingleValue(
                                tagContent[addressToRead].toLeUInt32,
                                bitLength,
                                previousLength
                            )
                        previousLength = bitLength
                        if(min!=null) {
                            minMaxVS.minValue =
                                MinVirtualSensorValue(
                                    ts,
                                    th1MinValueWithOffsetAndScaleFactor(
                                        min!!,
                                        negativeOffset,
                                        scaleFactor
                                    )
                                )
                        }
                    } else if (maxMinFormat.type.contains("max") || maxMinFormat.type.contains("Max")){
                        val negativeOffset = maxMinFormat.format.offset
                        val scaleFactor = maxMinFormat.format.scaleFactor
                        max =
                            unpackVirtualSensorMinMaxSingleValue(
                                tagContent[addressToRead].toLeUInt32,
                                bitLength,
                                previousLength
                            )
                        previousLength = bitLength
                        if(max!=null) {
                            minMaxVS.maxValue =
                                MaxVirtualSensorValue(
                                    ts,
                                    th2MaxValueWithOffsetAndScaleFactor(
                                        max!!,
                                        negativeOffset,
                                        scaleFactor
                                    )
                                )
                        }
                    }
                }
                bitCounter += bitLength

                if(bitCounter>=32){
                    addressToRead = addressToRead.inc()
                    bitCounter -= 32
                    previousLength = null
                }
            }
            virtualSensorsMinMax.add(minMaxVS)
            if(min != null || max != null) {
                addressToRead = addressToRead.inc()
            }
        }

        return virtualSensorsMinMax
    }

    /**
     * [0] Sample Counter (4B)
     * [1] Last Sample Pointer (4B)
     */
    private fun readSampleCounterLastSamplePointer() : List<Int> {
        val sampleInfo: ArrayList<Int> = ArrayList()

        sampleCounter = unpackSampleCounterInformation(tagContent[addressToRead].toLeUInt32)
        addressToRead = addressToRead.inc()
        lastSamplePointer = unpackLastSamplePointerInformation(tagContent[addressToRead].toLeUInt32)
        addressToRead = addressToRead.inc()

        FIRST_SAMPLE_POSITION = addressToRead

        sampleInfo.add(sampleCounter)
        sampleInfo.add(lastSamplePointer)

        return sampleInfo.toList()
    }


    private fun readDataSample(): GenericDataSample {
        val rawVSId = tagContent[addressToRead].toLeUInt32
        val vsId = unpackVirtualSensorId(rawVSId)
        val currentFw = NFCTag2CurrentFw.getCurrentFw()
        val virtualSensorCatalog =
            retrieveVirtualSensorFromCatalog(
                currentFw,
                vsId
            )

        var bitCounter = 0
        var previousLength: Int? = null

        var ts: Date? = null
        var value: Double? = null
        var measure: String = virtualSensorCatalog?.type ?: ""

        var idBitLength = 0

        virtualSensorCatalog?.sampleFormat?.forEach { sampleFormat ->
            val bitLength = sampleFormat.format?.bitLength?.toInt()
            if (bitLength != null) {
                if (bitLength <= 32) {
                    if (sampleFormat.type.contains("id")) {
                        bitCounter += sampleFormat.format.bitLength.toInt()
                        idBitLength = sampleFormat.format.bitLength.toInt()
                    }
                    if (sampleFormat.type.contains("time")) {
                        //ts = unpackVirtualSensorMinMaxSingleValue(read(addressToRead), bitLength, previousLength)
                        ts = unpackDataSampleTimeStamp(
                            idBitLength,
                            tagContent[addressToRead].toLeUInt32,
                            baseInformation.baseTagTimeStamp
                        ).time
                        previousLength = bitLength
                    } else if (sampleFormat.type.contains("sample") || sampleFormat.type.contains("Sample")) {
                        value =
                            unpackVirtualSensorSingleValue(
                                tagContent[addressToRead].toLeUInt32,
                                bitLength,
                                previousLength,
                                virtualSensorCatalog
                            )
                        previousLength = bitLength
                    }
                }
                bitCounter += bitLength
            }

            if(bitCounter>=32){
                addressToRead = addressToRead.inc()
                bitCounter -= 32
                previousLength = null
            }
        }

        addressToRead = addressToRead.inc()

        return GenericDataSample(vsId, measure, ts, value)
    }

    fun readDataSample(onReadNumberOfSample:(Int?)->Unit,onReadSample:(GenericSample)->Unit) {
        val protocolBoardFirmwareInfo = tagContent[PROTOCOL_BOARD_FIRMWARE_INFORMATION]
        val virtualSensorSampleTimeInfo = tagContent[VIRTUAL_SENSOR_SAMPLE_TIME]
        val timestampInfo = tagContent[TIMESTAMP_ADDRESS]

        baseInformation = unpackBaseSmarTag2Information(protocolBoardFirmwareInfo.toLeUInt32, virtualSensorSampleTimeInfo.toLeUInt32, timestampInfo.toLeUInt32)
        configuration = readTag2Configuration(baseInformation)

        val sampleInfo = readSampleCounterLastSamplePointer()
        onReadNumberOfSample(sampleInfo[0])
        print("\n\n\n *** SAMPLE COUNTER: ${sampleInfo[0]} - LAST SAMPLE POINTER: ${sampleInfo[1]} *** \n\n\n")

        for (i in 0 until sampleInfo[0]) {
            val sample = readDataSample()
            Log.d("SAMPLE", sample.toString())
            onReadSample(sample)
        }

    }


    companion object {
        private const val PROTOCOL_BOARD_FIRMWARE_INFORMATION = 0x00
        private const val VIRTUAL_SENSOR_SAMPLE_TIME = 0x01
        private const val TIMESTAMP_ADDRESS = 0x02

        private const val FIRST_VIRTUAL_SENSOR_POSITION = 0x03

        private var FIRST_SAMPLE_POSITION = 0x06
    }

}