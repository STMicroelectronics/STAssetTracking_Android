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

package com.st.assetTracking.threshold.view.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.st.assetTracking.threshold.model.ThresholdSensorType.*
import com.st.assetTracking.threshold.view.resourceImage
import com.st.assetTracking.threshold.view.resourceString

sealed class SensorThresholdViewData(
        @DrawableRes val iconId: Int,
        @StringRes val sensorId: Int) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SensorThresholdViewData

        if (iconId != other.iconId) return false
        if (sensorId != other.sensorId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = iconId
        result = 31 * result + sensorId
        return result
    }
}

internal class EnvironmentalThresholdViewData(iconId: Int, sensorId: Int,
                                              val compareSymbolStr: String,
                                              val valueStr: String,
                                              @StringRes val unitStrId: Int) :
        SensorThresholdViewData(iconId, sensorId) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as EnvironmentalThresholdViewData

        if (compareSymbolStr != other.compareSymbolStr) return false
        if (valueStr != other.valueStr) return false
        if (unitStrId != other.unitStrId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + compareSymbolStr.hashCode()
        result = 31 * result + valueStr.hashCode()
        result = 31 * result + unitStrId
        return result
    }
}

internal class OrientationThresholdViewData(iconId: Int, sensorId: Int,
                                            @DrawableRes val orientationIcon: Int,
                                            @StringRes val orientationString: Int) :
        SensorThresholdViewData(iconId, sensorId) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as OrientationThresholdViewData

        if (orientationIcon != other.orientationIcon) return false
        if (orientationString != other.orientationString) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + orientationIcon
        result = 31 * result + orientationString
        return result
    }
}

internal class TiltThresholdViewData() :
        SensorThresholdViewData(Tilt.resourceImage,
                Tilt.resourceString)