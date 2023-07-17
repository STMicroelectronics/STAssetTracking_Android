package com.st.assetTracking.lora

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.UsbSerialProber

/**
 * add devices here, that are not known to DefaultProber
 *
 * if the App should auto start for these devices, also
 * add IDs to app/src/main/res/xml/device_filter.xml
 */
internal object CustomProber {
    val customProber: UsbSerialProber
        get() {
            val customTable = ProbeTable()
            customTable.addProduct(0x16d0, 0x087e, CdcAcmSerialDriver::class.java) // e.g. Digispark CDC
            customTable.addProduct(0x0483, 0x5740, CdcAcmSerialDriver::class.java) //ST-LINK
            customTable.addProduct(0x0483, 0x374B, CdcAcmSerialDriver::class.java) //IoT01A1
            return UsbSerialProber(customTable)
        }
}