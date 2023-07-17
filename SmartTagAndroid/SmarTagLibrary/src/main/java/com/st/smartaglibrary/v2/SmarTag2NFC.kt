package com.st.smartaglibrary.v2

import android.util.Log
import com.st.assetTracking.data.GenericDataSample
import com.st.assetTracking.data.GenericSample
import com.st.assetTracking.data.unpackVirtualSensorId
import com.st.smartaglibrary.SmarTagIO
import com.st.smartaglibrary.getNDefRecordFromOffset
import com.st.smartaglibrary.readStringFromByteOffset
import com.st.smartaglibrary.util.toLeUInt32
import com.st.smartaglibrary.v2.catalog.NFCBoardCatalogService
import com.st.smartaglibrary.v2.catalog.NFCTag2CurrentFw
import com.st.smartaglibrary.v2.catalog.NfcV2Firmware
import com.st.smartaglibrary.v2.ext.packForV2
import com.st.smartaglibrary.v2.ext.unpackDataSampleTimeStamp
import com.st.smartaglibrary.v2.ext.unpackLastSamplePointerInformation
import com.st.smartaglibrary.v2.ext.unpackMinMaxTimeStamp
import com.st.smartaglibrary.v2.ext.unpackSampleCounterInformation
import com.st.smartaglibrary.v2.model.*
import com.st.smartaglibrary.v2.model.unpackBaseSmarTag2Information
import com.st.smartaglibrary.v2.model.unpackVirtualSensor
import java.util.*
import kotlin.collections.ArrayList

class SmarTag2NFC (private val tag: SmarTagIO) {

    private lateinit var smarTag2BaseInformation: SmarTag2BaseInformation
    private var sampleCounter: Int = 0
    private var lastSamplePointer: Int = 0

    private var addressToRead: Short = FIRST_VIRTUAL_SENSOR_POSITION

    private val uriMemoryLayout by lazy {
        val tagSize = if (hasExtendedCCFile()) NFCTAG_64K_SIZE else NFCTAG_4K_SIZE
        SmarTagMemoryLayout(tagSize, findSmarTagUriRecord(tagSize))
    }

    private val memoryLayout by lazy {
        val tagSize = if (hasExtendedCCFile()) NFCTAG_64K_SIZE else NFCTAG_4K_SIZE
        SmarTagMemoryLayout(tagSize, findSmarTagRecord(tagSize))
    }

    private fun findSmarTagRecord(tagSize:Short):Short{
        var offset:Short = if (hasExtendedCCFile()) 3 else 2

        do {
            val recordHeader = tag.getNDefRecordFromOffset(offset)
            if (recordHeader.type == NDEF_EXTERNAL_TYPE) {
                // *4 since we want the byte offset not the cell offset
                val recordType = tag.readStringFromByteOffset((offset*4 + recordHeader.length).toShort(), recordHeader.typeLength.toShort())
                if (recordType == NDEF_SMARTAG_TYPE) {
                    val payloadOffset = recordHeader.typeLength + recordHeader.length + recordHeader.idLength
                    return (offset+payloadOffset.div(4)).toShort()
                }
            }
            val recordSize = (recordHeader.length + recordHeader.typeLength + recordHeader.payloadLength + recordHeader.idLength)
            offset = (offset + recordSize.div(4)).toShort()
        } while (offset<(tagSize-1) && !recordHeader.isLastRecord)

        return offset
    }

    private fun findSmarTagUriRecord(tagSize:Short):Short{
        var offset:Short = if (hasExtendedCCFile()) 3 else 2

        do {
            val recordHeader = tag.getNDefRecordFromOffset(offset)
            if (recordHeader.type == NDEF_URI_TYPE) {
                val payloadOffset = recordHeader.typeLength + recordHeader.length + recordHeader.idLength
                return (offset+payloadOffset.div(4)).toShort()
            }
            val recordSize = (recordHeader.length + recordHeader.typeLength + recordHeader.payloadLength + recordHeader.idLength)
            offset = (offset + recordSize.div(4)).toShort()
        } while (offset<(tagSize-1) && !recordHeader.isLastRecord)

        return offset
    }

    private fun hasExtendedCCFile(): Boolean {
        val cc = tag.read(0)
        //cc = 0xE2 0x40 length1 length2
        return cc[2]== EXTENDED_CC_LENGTH
    }

    //fun readTagConfiguration(): SamplingConfiguration = readTagConfiguration(true)

    fun readTag2BLEMacAddress(): String {
        tag.connect()
        val totalSize = uriMemoryLayout.ndefHeaderSize * 4
        var rawData = ""
        for (i in 0..totalSize){
            val data = uriRead(i.toShort())
            rawData += data.decodeToString()
        }
        val macAddress = rawData.substringAfter("Add=").substring(0..16)
        tag.close()
        return macAddress.uppercase()
    }

    /**
     * [0] Protocol Version (1B), Protocol Revision (1B), BoardID (1B), FwID(2B)
     * [1] RFU (1B), #VirtualSensor (1B), SampleTime (2B)
     * [2] TimeStamp (4B)
     * [3..#VirtualSensor] TH1 TH2 VSId Mod
     * [n..n+#VirtualSensor] TS Min TS Max
     * [] Sample Counter
     * [] Last Sample Pointer
     */
    fun readTag2Configuration(): SmarTag2Configuration = readTag2Configuration(true)

    private fun readTag2Configuration(openConnection:Boolean): SmarTag2Configuration {
        val catalog = NFCBoardCatalogService.getCatalog()

        if(openConnection)
            tag.connect()

        val protocolBoardFirmwareInfo = read(PROTOCOL_BOARD_FIRMWARE_INFORMATION)
        val virtualSensorSampleTimeInfo = read(VIRTUAL_SENSOR_SAMPLE_TIME)
        val timestampInfo = read(TIMESTAMP_ADDRESS)

        smarTag2BaseInformation = unpackBaseSmarTag2Information(protocolBoardFirmwareInfo, virtualSensorSampleTimeInfo, timestampInfo)

        val currentFw = findCurrentFirmwareFromCatalog(catalog, smarTag2BaseInformation.boardID, smarTag2BaseInformation.firmwareId)

        val virtualSensorsValues: ArrayList<VirtualSensorConfiguration> = ArrayList()
        var virtualSensorsMinMax: ArrayList<VirtualSensorMinMax> = ArrayList()

        if(currentFw != null) {

            for (i in FIRST_VIRTUAL_SENSOR_POSITION until (FIRST_VIRTUAL_SENSOR_POSITION + smarTag2BaseInformation.virtualSensorNumber.toString(16).toShort())) {
                val rawData = read(addressToRead)
                virtualSensorsValues.add(unpackVirtualSensor(rawData, currentFw))
                addressToRead = addressToRead.inc()
            }

            virtualSensorsMinMax = readMaxMinVirtualSensorValues(
                currentFw,
                smarTag2BaseInformation,
                virtualSensorsValues
            )
        }


        if(openConnection)
            tag.close()

        return SmarTag2Configuration(smarTag2BaseInformation, virtualSensorsValues.toList(), virtualSensorsMinMax.toList())
    }

    fun readTag2Extremes(): SmarTag2Extremes {
        val currentNfcV2Firmware = NFCTag2CurrentFw.getCurrentFw()

        tag.connect()

        val protocolBoardFirmwareInfo = read(PROTOCOL_BOARD_FIRMWARE_INFORMATION)
        val virtualSensorSampleTimeInfo = read(VIRTUAL_SENSOR_SAMPLE_TIME)
        val timestampInfo = read(TIMESTAMP_ADDRESS)

        smarTag2BaseInformation = unpackBaseSmarTag2Information(protocolBoardFirmwareInfo, virtualSensorSampleTimeInfo, timestampInfo)

        var virtualSensorsMinMax: ArrayList<VirtualSensorMinMax> = ArrayList()
        val virtualSensorsValues: ArrayList<VirtualSensorConfiguration> = ArrayList()

        for (i in FIRST_VIRTUAL_SENSOR_POSITION until (FIRST_VIRTUAL_SENSOR_POSITION + smarTag2BaseInformation.virtualSensorNumber.toString(16).toShort())) {
            val rawData = read(addressToRead)
            virtualSensorsValues.add(unpackVirtualSensor(rawData, currentNfcV2Firmware))
            addressToRead = addressToRead.inc()
        }

        virtualSensorsMinMax = readMaxMinVirtualSensorValues(
            currentNfcV2Firmware,
            smarTag2BaseInformation,
            virtualSensorsValues
        )

        tag.close()

        return SmarTag2Extremes(virtualSensorsMinMax)
    }

    private fun read(address: Short) : ByteArray{
        return tag.read((memoryLayout.ndefHeaderSize+address).toShort())
    }

    private fun uriRead(address: Short) : ByteArray{
        return tag.read((uriMemoryLayout.ndefHeaderSize+address).toShort())
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
                        ts = unpackMinMaxTimeStamp(bitLength, read(addressToRead), smarTag2BaseInformation.baseTagTimeStamp).time
                        previousLength = bitLength
                    } else if (maxMinFormat.type.contains("min") || maxMinFormat.type.contains("Min")){
                        val negativeOffset = maxMinFormat.format.offset
                        val scaleFactor = maxMinFormat.format.scaleFactor
                        min =
                            unpackVirtualSensorMinMaxSingleValue(
                                read(addressToRead),
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
                                read(addressToRead),
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

        sampleCounter = unpackSampleCounterInformation(read(addressToRead))
        addressToRead = addressToRead.inc()
        lastSamplePointer = unpackLastSamplePointerInformation(read(addressToRead))
        addressToRead = addressToRead.inc()

        FIRST_SAMPLE_POSITION = addressToRead

        sampleInfo.add(sampleCounter)
        sampleInfo.add(lastSamplePointer)

        return sampleInfo.toList()
    }

    private fun readDataSample(): GenericDataSample {
        val rawVSId = read(addressToRead)
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
                            read(addressToRead),
                            smarTag2BaseInformation.baseTagTimeStamp
                        ).time
                        previousLength = bitLength
                    } else if (sampleFormat.type.contains("sample") || sampleFormat.type.contains("Sample")) {
                        value =
                            unpackVirtualSensorSingleValue(
                                read(addressToRead),
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

        tag.connect()
        val conf = readTag2Configuration(false)
        val sampleInfo = readSampleCounterLastSamplePointer()
        onReadNumberOfSample(sampleInfo[0])
        print("\n\n\n *** SAMPLE COUNTER: ${sampleInfo[0]} - LAST SAMPLE POINTER: ${sampleInfo[1]} *** \n\n\n")
        // each sample has size 2 memory cell -> /2 go from memory address to sample index
        // -1 since we want the last sample and we have the next sample
        /*val lastSampleIndex = (FIRST_SAMPLE_POSITION - memoryLayout.firstDataSamplePtr)/2 -1
        var firstSampleIndex = if (sampleInfo[0] >= memoryLayout.maxSample) {
            lastSampleIndex + 1
        } else {
            0
        }*/
        for (i in 0 until sampleInfo[0]) {
            val sample = readDataSample()
            onReadSample(sample)
            //firstSampleIndex = (firstSampleIndex +1) % memoryLayout.maxSample
        }
        tag.close()
    }

    fun writeTagConfiguration(conf: SmarTag2Configuration) {
        tag.connect()

        val configurationToWrite = SmartTag2ConfigurationToWrite(
            conf.smarTag2BaseInformation.rfu,
            conf.smarTag2BaseInformation.virtualSensorNumber,
            conf.smarTag2BaseInformation.sampleTime,
            conf.smarTag2BaseInformation.rawTs,
            conf.virtualSensors
        )

        setVirtualSensorNumberAndSampleTime(configurationToWrite)
        setDateAndTime()
        writeVirtualSensorConfiguration(configurationToWrite)
        /*writeSamplingThreshold(conf)
        if(conf.mode!= SamplingConfiguration.Mode.SaveNextSample) {
            initializeFirstSamplePosition()
            resetTempHumMinMax()
            resetPresAccMinMax()
        }
        setNewConfigurationAvailable()*/
        tag.close()
    }

    private fun write(address:Short, data:ByteArray){
        tag.write((memoryLayout.ndefHeaderSize+address).toShort(),data)
    }

    private fun setVirtualSensorNumberAndSampleTime(conf: SmartTag2ConfigurationToWrite){
        val packData = ByteArray(4)
        val sampleTime = conf.sampleTime.toLong().toLeUInt32
        packData[0] = conf.rfu.toShort().toByte()
        packData[1] = conf.virtualSensorNumber.toShort().toByte()
        packData[2] = sampleTime[0]
        packData[3] = sampleTime[1]
        write(VIRTUAL_SENSOR_SAMPLE_TIME,packData)
    }

    private fun setDateAndTime() {
        write(TIMESTAMP_ADDRESS,GregorianCalendar().packForV2())
    }

    private fun writeVirtualSensorConfiguration(conf: SmartTag2ConfigurationToWrite) {
        val byteArray: ArrayList<ByteArray> = ArrayList()
        val currentFw = NFCTag2CurrentFw.getCurrentFw()

        conf.virtualSensors.forEach { vs ->
            val vsCatalog =
                retrieveVirtualSensorFromCatalog(
                    currentFw,
                    vs.id
                )
            if(vsCatalog != null) {
                byteArray.add(createVSCPack(vs, vsCatalog))
            }
        }

        if(byteArray.isNotEmpty()) {
            var memoryAddress = TIMESTAMP_ADDRESS.inc()
            byteArray.forEach { byte ->
                write(memoryAddress, byte)
                memoryAddress = memoryAddress.inc()
            }
        }
    }

    fun readSingleShotData(readTimeoutS:Int,beforeWait: ((Long)->Unit)?=null) : SmarTag2Configuration {
        tag.connect()

        val waitAnswerMS = if (readTimeoutS > 0){
            readTimeoutS * 1000L
        }else{
            WAIT_SINGLE_SHOT_ANSWER_MS
        }

        do {
            beforeWait?.invoke(waitAnswerMS)
            Thread.sleep(waitAnswerMS)
        }while (!singleShotDataAreReady())

        val tag2Configuration = readTag2Configuration(false)
        tag.close()
        return  tag2Configuration
    }

    private fun singleShotDataAreReady(): Boolean {
        val response = read(VIRTUAL_SENSOR_SAMPLE_TIME)
        Log.d("SINGLE SHOT BYTE READY", response[0].toInt().toString())
        return response[0].toInt() == 1
    }

    private data class SmarTagMemoryLayout(
        val totalSize:Short,
        val ndefHeaderSize: Short) {

        /**
         * position where the first data sample is
         */
        val firstDataSamplePtr: Short = (FIRST_SAMPLE_POSITION +ndefHeaderSize).toShort()
        //totalSize -1 since the last cell is used for the TLV
        val lastDataSample:Short = totalSize.dec()

        /**
         * max number of data sample that is possible to store in the tag
         */
        val maxSample:Short = ((lastDataSample- firstDataSamplePtr)/2).toShort()
    }

    companion object {

        private const val EXTENDED_CC_LENGTH = 0x00.toByte()

        private const val WAIT_SINGLE_SHOT_ANSWER_MS = 7000L

        private const val PROTOCOL_BOARD_FIRMWARE_INFORMATION = 0x00.toShort()
        private const val VIRTUAL_SENSOR_SAMPLE_TIME = 0x01.toShort()
        private const val TIMESTAMP_ADDRESS = 0x02.toShort()

        private const val FIRST_VIRTUAL_SENSOR_POSITION = 0x03.toShort()

        private var FIRST_SAMPLE_POSITION = 0x06.toShort()

        private const val NFCTAG_4K_SIZE = 0x80.toShort()
        private const val NFCTAG_64K_SIZE = 0x800.toShort()

        private const val NDEF_URI_TYPE = 0x01.toByte()
        private const val NDEF_EXTERNAL_TYPE = 0x04.toByte()
        private const val NDEF_SMARTAG_TYPE = "st.com:smartag"
    }

}