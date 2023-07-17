/*
 * Copyright (c) 2018  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *    and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *    conditions and the following disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *    STMicroelectronics company nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *    in a directory whose title begins with st_images may only be used for internal purposes and
 *    shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *    icons, pictures, logos and other images that are provided with the source code in a directory
 *    whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package com.st.smartaglibrary.v1.model

import com.st.smartaglibrary.v1.SmarTag
import kotlin.experimental.and

/**
 * Class containing the min and max for the temperature ( [tempMax] and [tempMin]) and the humidity
 * ( [humMax] and [humMin])
 */
internal data class MaxMinTempHumCell(val tempMax:Float, val tempMin:Float,
                             val humMax:Float, val humMin:Float)


/**
 * encode the min/max information to a nfc memory cell
 */
internal fun MaxMinTempHumCell.pack():ByteArray{
    val packData = ByteArray(4)
    val maxTemp = tempMax - SmarTag.TEMPERATURE_RANGE_C.start
    val minTemp = tempMin - SmarTag.TEMPERATURE_RANGE_C.start
    packData[0] = maxTemp.toInt().toByte() and 0x7F
    packData[1] = minTemp.toInt().toByte() and 0x7F

    packData[2] = humMax.toInt().toByte() and 0x7F
    packData[3] = humMin.toInt().toByte() and 0x7F
    return  packData
}

/**
 * decode a nfc memory cell into a the min/max information
 */
internal fun unpackMaxMinTempHumCell(rawData:ByteArray): MaxMinTempHumCell {
    val tempMax = (rawData[0] and 0x7F.toByte()) + SmarTag.TEMPERATURE_RANGE_C.start
    val tempMin = (rawData[1] and 0x7F.toByte()) + SmarTag.TEMPERATURE_RANGE_C.start
    val humMax = (rawData[2] and 0x7F).toFloat()
    val humMin = (rawData[3] and 0x7F).toFloat()
    return MaxMinTempHumCell(tempMax, tempMin, humMax, humMin)
}