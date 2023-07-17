package com.st.nfcSmarTag.v2.settings

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.st.smartaglibrary.v2.model.SmarTag2Configuration

class Tag2SettingsViewModel : ViewModel() {
    var currentSettings: MutableLiveData<SmarTag2Configuration> = MutableLiveData()
    var desiredSettings: MutableLiveData<SmarTag2Configuration> = MutableLiveData()
    var readSettings: MutableLiveData<Boolean> = MutableLiveData()

    companion object {
        fun create(activity: FragmentActivity): Tag2SettingsViewModel {
            return ViewModelProvider(activity).get(Tag2SettingsViewModel::class.java)
        }
    }

    fun updateSettings(newConf: SmarTag2Configuration) {
        //todo: use postValue?
        readSettings.value = false
        desiredSettings.value = newConf
    }

    fun onSettingsWrote() {
        newConfiguration(desiredSettings.value)
        readSettings.value = true
    }

    /**
     * retrieved new SmarTag2NFC configuration
     */
    fun newConfiguration(conf: SmarTag2Configuration?) {
        currentSettings.value = conf
        desiredSettings.value = null
    }

}