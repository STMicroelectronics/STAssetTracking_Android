package com.st.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class LoginActivityResultContract(val automaticLoginButtonClick: Boolean?) : ActivityResultContract<String, AuthData>(){

    override fun createIntent(context: Context, input: String): Intent {
        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra("PROVIDER", input)
            if(automaticLoginButtonClick != null){
                if(automaticLoginButtonClick) {
                    putExtra("automaticClickLogin", true)
                }
            }
        }
        return intent
    }

    override fun parseResult(
            resultCode: Int,
            intent: Intent?
    ): AuthData = when {
        resultCode != Activity.RESULT_OK -> AuthData("","","","")
        else -> AuthData(intent?.getBundleExtra("AuthData")!!.getString("accessKey")!!,
        intent.getBundleExtra("AuthData")!!.getString("secretKey")!!,
        intent.getBundleExtra("AuthData")!!.getString("token")!!,
        intent.getBundleExtra("AuthData")!!.getString("expiration")!!)
    }

}