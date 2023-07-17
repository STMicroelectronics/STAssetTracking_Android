package com.st.assetTracking.data

/**
 * possible board orientation
 */
enum class Orientation(val raw: Byte) {

    UNKNOWN(0x00),
    UP_RIGHT(0X01),
    TOP(0x02),
    DOWN_LEFT(0x03),
    BOTTOM(0x04),
    UP_LEFT(0x05),
    DOWN_RIGHT(0x06);


    companion object {
        /**
         * create an orientation or return [Orientation.UNKNOWN] if it is not a valid value
         */
        fun valueOf(raw: Byte): Orientation {
            return values().find { it.raw == raw } ?: UNKNOWN
        }
    }
}