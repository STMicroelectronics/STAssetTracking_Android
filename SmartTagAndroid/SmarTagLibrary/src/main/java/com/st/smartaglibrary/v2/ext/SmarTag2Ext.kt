package com.st.smartaglibrary.v2.ext

import android.nfc.Tag
import com.st.smartaglibrary.ST25DVTag
import com.st.smartaglibrary.v2.SmarTag2NFC

/**
 * Utility function for buld a SmarTag2NFC object form an android [tag] object
 */
fun SmarTag2NFC.Companion.get(tag: Tag): SmarTag2NFC?{
    val tagIO = ST25DVTag.get(tag)
    return if(tagIO!=null){
        SmarTag2NFC(tagIO)
    }else {
        null
    }
}
