package com.st.login

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.appcompat.app.AppCompatActivity
import com.st.login.loginprovider.CognitoLoginProvider
import com.st.login.loginprovider.ILoginProvider
import com.st.login.loginprovider.LoginProviderFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LoginManager(
    private val resultRegistry: ActivityResultRegistry,
    private val activity: AppCompatActivity,
    private val ctx: Context,
    private val loginProviderType: LoginProviderFactory.LoginProviderType,
    private val configuration: Configuration): ILoginManager {

    private val loginProvider = LoginProviderFactory.getLoginProvider(activity, ctx, loginProviderType, configuration)

    private var auth: AuthData? = null

    fun getAtrAuthData(callback: (AuthData?) -> Unit) {
        if (loginProvider.isLogged()) {
            (loginProvider as CognitoLoginProvider).getAtrAuthData { authData ->
                if(authData != null) {
                    callback(authData)
                }
            }
        }
    }

    override suspend fun login() :AuthData? {

        var authData : AuthData? = null

        authData = if (loginProvider.isLogged()) {
            //doLogin(autoClickButton = true)
            loginProvider.getAuthData()
        } else {
            doLogin(autoClickButton = false)
        }

        return authData
    }

    override suspend fun forceFreshLogin(): AuthData? {
        return doLogin(autoClickButton = false)
    }

    override suspend fun logout() {
        loginProvider.logout()
    }

    override suspend fun isLogged(loginProvider: ILoginProvider) : Boolean {
        return loginProvider.isLogged()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun doLogin(autoClickButton: Boolean) : AuthData? {
        val def = CompletableDeferred<AuthData?>()

        val job = GlobalScope.launch {
            withContext(coroutineContext){
                val loginActivityLauncher: ActivityResultLauncher<String> = resultRegistry.register("key", LoginActivityResultContract(automaticLoginButtonClick = autoClickButton)) { result ->
                    if(result!=null){
                        auth = result
                        def.complete(auth!!)
                    }else{
                        def.complete(null)
                    }
                }
                loginActivityLauncher.launch(loginProviderType.toString())
                def.await()
            }
        }
        job.join()

        return auth
    }

}