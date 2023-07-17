package com.st.assetTracking.data

import kotlin.experimental.and
import kotlin.experimental.or

/**
 * accelerometer event
 */
enum class AccelerationEvent(private val mask: Byte) {
    ACCELERATION_WAKE_UP(0X01),
    ORIENTATION(0X02),
    SINGLE_TAP(0X04),
    DOUBLE_TAP(0X08),
    FREE_FALL(0X10),
    ACCELERATION_TILT_35(0X20);

    companion object {

        /**
         * since each bit is a separate event, this function extract all selected events
         */
        fun extractEvent(rawEvent: Byte): Array<AccelerationEvent> {
            val events = ArrayList<AccelerationEvent>()
            values().forEach {
                if (rawEvent and it.mask == it.mask)
                    events.add(it)
            }
            return events.toArray(arrayOfNulls(events.size))
        }

        /**
         * map all the events to a byte
         */
        fun packEvents(events: Array<AccelerationEvent>): Byte {
            var value: Byte = 0
            events.forEach { value = value or it.mask }
            return value
        }
    }
}
