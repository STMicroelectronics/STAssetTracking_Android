package com.st.nfcSmarTag.v2

import android.content.SharedPreferences
import android.nfc.Tag
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.st.smartaglibrary.stringId
import com.st.smartaglibrary.v2.catalog.NFCBoardCatalogService
import com.st.smartaglibrary.v2.catalog.NfcV2BoardCatalog
import com.st.smartaglibrary.v2.catalog.NfcV2Firmware
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.Serializable

class NfcTag2ViewModel : ViewModel() {

    /** SmarTag provisioning completed */
    val completeProvisioning: LiveData<Boolean>
        get() = mcompleteProvisioning
    private val mcompleteProvisioning = MutableLiveData(false)

    /** Last nfc tag detected, if null no tag are detected, or an error happen */
    val nfcTag: LiveData<Tag>
        get() = _nfcTag
    private val _nfcTag = MutableLiveData<Tag>()

    val nfcTagId: LiveData<String?>
        get() = _nfcTagId
    private val _nfcTagId = MutableLiveData<String?>()

    private val _ioError = MutableLiveData<String>()

    /** last error description or null if not error */
    val ioError: LiveData<String>
        get() = _ioError

    /** NFC Catalog */
    private val _nfcCatalog = MutableLiveData<NfcV2BoardCatalog?>()
    val nfcCatalog: LiveData<NfcV2BoardCatalog?>
        get() = _nfcCatalog

    /** Setup Complete Provisioning Device */
    fun setCompletedProvisioning() {
        mcompleteProvisioning.postValue(true)
    }

    /** Call when a new [tag] is available, it resets the error key */
    fun nfcTagDiscovered(tag: Tag) {
        if (_nfcTag.value != tag) {
            _nfcTag.postValue(tag)
            _nfcTagId.postValue(tag.stringId)
        }
        //remove old error
        _ioError.postValue(null)
    }

    /** Call when the [nfcTag] is lost/disconnected */
    fun nfcTagLost() {
        _nfcTag.postValue(null)
        _nfcTagId.postValue(null)
    }

    /** Call when an [error] happen during the IO to using the [nfcTag] */
    fun nfcTagError(error: String) {
        nfcTagLost()
        _ioError.postValue(error)
    }

    /** Retrieve NFC Catalog */
    fun retrieveNfcCatalog() {
        CoroutineScope(Dispatchers.Main).launch {
            val nfcCatalog = NFCBoardCatalogService().getNfcCatalog()
            _nfcCatalog.postValue(nfcCatalog)
        }
    }

    fun getCustomFwEntry(): NfcV2Firmware? {
        return sharedPreferences.getSerializable("NfcV2Firmware", NfcV2Firmware::class.java)
    }

    fun putCustomFwEntry(o: NfcV2Firmware) {
        sharedPreferences.edit().putSerializable("NfcV2Firmware", o).apply()
    }

    @Throws(JsonIOException::class)
    fun Serializable.toJson(): String {
        return Gson().toJson(this)
    }

    @Throws(JsonSyntaxException::class)
    fun <T> String.to(type: Class<T>): T where T : Serializable {
        return Gson().fromJson(this, type)
    }

    @Throws(JsonIOException::class)
    fun SharedPreferences.Editor.putSerializable(key: String, o: Serializable?) = apply {
        putString(key, o?.toJson())
    }

    @Throws(JsonSyntaxException::class)
    fun <T> SharedPreferences.getSerializable(key: String, type: Class<T>): T? where T : Serializable {
        return getString(key, null)?.to(type)
    }

    companion object {
        lateinit var sharedPreferences: SharedPreferences
        /** helper function to create a viewmodel attach to the [activity] */
        fun create(activity: FragmentActivity): NfcTag2ViewModel {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            return ViewModelProvider(activity).get(NfcTag2ViewModel::class.java)
        }
    }

}