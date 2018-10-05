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

package com.st.assetTracking.sigfox.model

import android.os.Parcel
import android.os.Parcelable

internal enum class ThresholdComparison{
    Less,Equal,BiggerOrEqual
}

/**
 * Valid range for the different type of sensor
 */
internal enum class ThresholdSensorType(val range:ClosedRange<Float>){
    Temperature(-20.0f..100.0f),
    Pressure(500.0f..1260.0f),
    Humidity(0.0f..100.0f),
    WakeUp(1.0f..16.0f),
    Tilt(0.0f..1.0f),
    Orientation(0.0f..6.0f)
}

/**
 * configure the board do something when the [sensor] value  is [comparison] [threshold]
 * */
internal class SensorThreshold(val sensor: ThresholdSensorType,
                           val comparison: ThresholdComparison,
                           threshold: Float) :Parcelable{

    val threshold = threshold.coerceIn(sensor.range)

    constructor(parcel: Parcel) : this(
            parcel.readSerializable() as ThresholdSensorType,
            parcel.readSerializable() as ThresholdComparison,
            parcel.readFloat())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeSerializable(sensor)
        parcel.writeSerializable(comparison)
        parcel.writeFloat(threshold)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "SensorThreshold(sensor=$sensor, comparison=$comparison, threshold=$threshold)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SensorThreshold

        if (sensor != other.sensor) return false
        if (comparison != other.comparison) return false
        if (threshold != other.threshold) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sensor.hashCode()
        result = 31 * result + comparison.hashCode()
        result = 31 * result + threshold.hashCode()
        return result
    }


    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<SensorThreshold> {
            override fun createFromParcel(parcel: Parcel): SensorThreshold {
                return SensorThreshold(parcel)
            }

            override fun newArray(size: Int): Array<SensorThreshold?> {
                return arrayOfNulls(size)
            }
        }

        /**
         * create a threshold that will be fired when the specific orientation is detected
         */
        fun orientationThreshold(orientation: Orientation) : SensorThreshold {
            return SensorThreshold(ThresholdSensorType.Orientation,
                    ThresholdComparison.Equal,
                    orientation.rawValue.toFloat())
        }

        /**
         * create a threshold that will be fired when the an acceleration bigger than [th] is detected
         */
        fun wakeUpThreshold(th:Float): SensorThreshold {
            return SensorThreshold(ThresholdSensorType.WakeUp, ThresholdComparison.BiggerOrEqual, th)
        }

        /**
         * create a tilt threshold
         */
        fun tiltThreshold():SensorThreshold{
            return SensorThreshold(ThresholdSensorType.Tilt,ThresholdComparison.Equal, 1.0f)
        }
    }
}

internal data class SamplingSettings(
        val samplingInterval:Short,
        val cloudSyncInterval:Short,
        val threshold:List<SensorThreshold>
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt().toShort(),
            parcel.readInt().toShort(),
            parcel.createTypedArrayList(SensorThreshold.CREATOR)!!)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(samplingInterval.toInt())
        parcel.writeInt(cloudSyncInterval.toInt())
        parcel.writeTypedList(threshold)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SamplingSettings> {
        override fun createFromParcel(parcel: Parcel): SamplingSettings {
            return SamplingSettings(parcel)
        }

        override fun newArray(size: Int): Array<SamplingSettings?> {
            return arrayOfNulls(size)
        }
    }


}
