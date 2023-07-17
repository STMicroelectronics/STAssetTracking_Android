package com.st.nfcSmarTag.v1.tagSingleShot.settings

import android.content.Context
import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import com.st.nfcSmarTag.R


internal class SingleShotPreferenceFragment : PreferenceFragmentCompat() {

    private lateinit var mSettings: SingleShotSettings

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mSettings = SingleShotSettings(context)

    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_single_shot, rootKey)
        preferenceManager.preferenceDataStore = SettingsWrapper(mSettings)
        preferenceManager.findPreference<EditTextPreference>(TIMEOUT_PREF_KEY)?.let { timeoutEditPref ->

            timeoutEditPref.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }


            timeoutEditPref.setOnPreferenceChangeListener { _, newValue ->
                val intValue = (newValue as? String)?.toIntOrNull()
                intValue != null
            }

            timeoutEditPref.setSummaryProvider { _ ->
                val value = mSettings.readingTimeOutSec
                getString(R.string.singleShotPref_timeoutSummaryFormat,value)
            }
        }

    }


    class SettingsWrapper(private val singleShotSettings: SingleShotSettings): PreferenceDataStore(){
        override fun putString(key: String?, value: String?) {
            if(key!= TIMEOUT_PREF_KEY)
                return
            val intValue = value?.toIntOrNull() ?: return

            singleShotSettings.readingTimeOutSec = intValue

        }

        override fun getString(key: String?, defValue: String?): String? {
            if(key!= TIMEOUT_PREF_KEY)
                return defValue
            return singleShotSettings.readingTimeOutSec.toString()
        }

    }

    companion object{
        const val TIMEOUT_PREF_KEY = "singleShotPref_timeOutKey"
    }

}