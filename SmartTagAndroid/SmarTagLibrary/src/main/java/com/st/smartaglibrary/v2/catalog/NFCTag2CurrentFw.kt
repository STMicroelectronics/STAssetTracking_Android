package com.st.smartaglibrary.v2.catalog

class NFCTag2CurrentFw {
    companion object {
        private lateinit var currentFw: NfcV2Firmware
        fun saveCurrentFw(fw: NfcV2Firmware){
            currentFw = fw
        }
        fun getCurrentFw(): NfcV2Firmware {
            return currentFw
        }
    }
}