package com.st.smartaglibrary.v2.ext

import com.st.smartaglibrary.util.leUShort

internal fun unpackSampleCounterInformation(sampleCounterRawData:ByteArray) : Int {
    return sampleCounterRawData.leUShort
}

internal fun unpackLastSamplePointerInformation(lastSamplePointerRawData:ByteArray) : Int {
    return lastSamplePointerRawData.leUShort
}
