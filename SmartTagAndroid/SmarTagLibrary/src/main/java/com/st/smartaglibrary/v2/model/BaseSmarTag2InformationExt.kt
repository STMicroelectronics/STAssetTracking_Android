package com.st.smartaglibrary.v2.model

import com.st.smartaglibrary.util.leUInt
import com.st.smartaglibrary.util.leUShort
import com.st.smartaglibrary.v2.ext.unpackTimeStamp

internal fun unpackBaseSmarTag2Information(
    protocolBoardFirmwareInfo:ByteArray,
    virtualSensorSampleTimeInfo:ByteArray,
    timestampInfo:ByteArray): SmarTag2BaseInformation {

    return SmarTag2BaseInformation(
        protocolBoardFirmwareInfo[0].toInt(),
        protocolBoardFirmwareInfo[1].toInt(),
        protocolBoardFirmwareInfo[2].toInt(),
        protocolBoardFirmwareInfo[3].toInt(),
        virtualSensorSampleTimeInfo[0].toInt(),
        virtualSensorSampleTimeInfo[1].toInt(),
        virtualSensorSampleTimeInfo.slice(IntRange(2,3)).toByteArray().leUShort,
        timestampInfo.leUInt.toInt(),
        unpackTimeStamp(timestampInfo)
    )

}