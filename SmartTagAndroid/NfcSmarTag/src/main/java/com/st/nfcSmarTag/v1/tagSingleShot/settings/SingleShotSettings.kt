package com.st.nfcSmarTag.v1.tagSingleShot.settings

import android.content.Context
import androidx.core.content.edit

internal class SingleShotSettings(context: Context){

    private val settings = context.getSharedPreferences(SETTINGS_NAME,Context.MODE_PRIVATE)

    var readingTimeOutSec:Int
        get() = settings.getInt(TIMEOUT_KEY, TIMEOUT_DEFAULT_S)
        set(value) {
            settings.edit {
                putInt(TIMEOUT_KEY,value)
            }
        }

    companion object{
        private val SETTINGS_NAME = SingleShotSettings::class.java.name
        private val TIMEOUT_KEY = SETTINGS_NAME +".TIMEOUT"
        private val TIMEOUT_DEFAULT_S = 7
    }

}