package com.st.assetTracking.dashboard.certManager.manager

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


internal class CertManagerPageViewModel(val context: Context, val deviceId: String, val deviceType : String, val certificate: String?,val selfSigned: Boolean) : ViewModel() {

    class Factory(private val context: Context, private val deviceId: String, private val deviceType: String, private val certificate: String?,private val selfSigned: Boolean) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CertManagerPageViewModel(context, deviceId,deviceType,certificate,selfSigned) as T
        }
    }
}