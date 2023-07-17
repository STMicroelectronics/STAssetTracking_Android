package com.st.nfcSmarTag.v2.extremes

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.st.smartaglibrary.v2.model.SmarTag2Extremes

class Tag2ExtremesViewModel: ViewModel() {

    companion object {
        fun create(activity: FragmentActivity): Tag2ExtremesViewModel {
            return ViewModelProvider(activity).get(Tag2ExtremesViewModel::class.java)
        }
    }

    private val _dataExtreme = MutableLiveData<SmarTag2Extremes>()
    val dataExtreme: LiveData<SmarTag2Extremes>
        get() = _dataExtreme

    fun newExtremeData(data: SmarTag2Extremes) {
        _dataExtreme.value = data
    }
}