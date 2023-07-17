package com.st.nfcSmarTag.v2.singleshot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.st.smartaglibrary.v2.catalog.NFCBoardCatalogService
import com.st.smartaglibrary.v2.catalog.NfcV2BoardCatalog
import com.st.smartaglibrary.v2.model.SmarTag2Configuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Tag2SingleShotViewModel : ViewModel(){

    /**
     * NFC Catalog retrieved
     */
    private val _nfcCatalog = MutableLiveData<NfcV2BoardCatalog?>()
    val nfcCatalog: LiveData<NfcV2BoardCatalog?>
        get() = _nfcCatalog

    private val _tag2Configuration = MutableLiveData<SmarTag2Configuration>()
    val tag2Configuration: LiveData<SmarTag2Configuration>
        get() = _tag2Configuration

    private val _waitingAnswer = MutableLiveData<Long>()
    val waitingAnswer: LiveData<Long>
        get() = _waitingAnswer

    private val _singleShotReadFail = MutableLiveData<Boolean>()
    val singleShotReadFail: LiveData<Boolean>
        get() = _singleShotReadFail

    fun newTag2Configuration(configuration: SmarTag2Configuration) {
        _tag2Configuration.value = configuration
        _waitingAnswer.value=null
        _singleShotReadFail.value=false
    }

    fun readFail(){
        _tag2Configuration.value=null
        _singleShotReadFail.value=true
    }

    fun startWaitingAnswer(timeout:Long){
        _waitingAnswer.value=timeout
    }

    /**
     * retrieve NFC Catalog
     */
    fun retrieveNfcCatalog() {
        CoroutineScope(Dispatchers.Main).launch {
            val nfcCatalog = NFCBoardCatalogService().getNfcCatalog()
            _nfcCatalog.postValue(nfcCatalog)
        }
    }

}
