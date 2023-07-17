package com.st.assetTracking.threshold.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * configure the board do something when the [sensor] value  is [comparison] [threshold]
 * */
class SensorThreshold(val sensor: ThresholdSensorType,
                      val comparison: ThresholdComparison,
                      threshold: Float) : Parcelable {

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
        fun orientationThreshold(orientation: Orientation): SensorThreshold {
            return SensorThreshold(ThresholdSensorType.Orientation,
                    ThresholdComparison.Equal,
                    orientation.rawValue.toFloat())
        }

        /**
         * create a threshold that will be fired when the an acceleration bigger than [th] is detected
         */
        fun wakeUpThreshold(th: Float): SensorThreshold {
            return SensorThreshold(ThresholdSensorType.WakeUp, ThresholdComparison.BiggerOrEqual, th)
        }

        /**
         * create a tilt threshold
         */
        fun tiltThreshold(): SensorThreshold {
            return SensorThreshold(ThresholdSensorType.Tilt, ThresholdComparison.Equal, 1.0f)
        }
    }
}