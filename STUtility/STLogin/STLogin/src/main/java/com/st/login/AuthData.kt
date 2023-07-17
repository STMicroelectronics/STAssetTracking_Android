package com.st.login

import androidx.lifecycle.MutableLiveData
import com.st.login.loginprovider.ILoginProvider

//internal data class AuthData(val token: String) : LoginProvider.AuthData

data class AuthData(override val accessKey: String, override val secretKey: String, override val token: String, override val expiration: String) : ILoginProvider.AuthData

sealed class AuthDataLoading {
    object Requesting : AuthDataLoading()
    data class Loaded(val authenticationData: AuthData) : AuthDataLoading()
    object UnknownError : AuthDataLoading()
}

val mAuthProcess = MutableLiveData<AuthDataLoading>()
val authProcess: MutableLiveData<AuthDataLoading>
    get() = mAuthProcess