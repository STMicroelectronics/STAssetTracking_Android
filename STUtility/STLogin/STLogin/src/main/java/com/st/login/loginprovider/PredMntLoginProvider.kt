package com.st.login.loginprovider

import android.app.Activity
import android.content.Context
import android.util.Log
import com.st.login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.openid.appauth.TokenResponse
import okio.buffer
import okio.source
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

class PredMntLoginProvider (
    val activity: Activity,
    private val ctx: Context,
    private val configuration: Configuration) : KeycloakLoginProvider(activity, ctx, configuration) {

    companion object {
        private const val TAG = "PredMntLoginProvider"
        private const val LOGIN_PROVIDER_TAG = "PREDMNT_LOGIN_PROVIDER"
    }

    override fun getLoginProviderTag() : String {
        return LOGIN_PROVIDER_TAG
    }

    override suspend fun onAuthNExchangeCompleted(tokenResponse: TokenResponse?) : LoginResult {
        return when (checkLicenseAgreement(tokenResponse!!.idToken!!)) {
            is LoginLicenseAgreed ->
                LoginSuccess(
                    AuthData(mAuthStateManager.current.accessToken!!,
                        "SecretKey",
                        mAuthStateManager.current.idToken!!,
                        mAuthStateManager.current.accessTokenExpirationTime.toString()
                    )
                )
            is LoginAuthNSuccess ->
                LoginAuthNSuccess(
                    AuthData(mAuthStateManager.current.accessToken!!,
                        "SecretKey",
                        mAuthStateManager.current.idToken!!,
                        mAuthStateManager.current.accessTokenExpirationTime.toString()
                    )
                )
            else -> {
                LoginError(Exception("login error"))
            }
        }

    }

    private fun checkLicenseAgreement(idTokenN: String): LoginResult{
        val splittedToken = idTokenN.split(".")
        val payload = splittedToken[1]

        val bytes: ByteArray = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Base64.getUrlDecoder().decode(payload)
        } else {
            android.util.Base64.decode(payload, android.util.Base64.DEFAULT)
        }

        val tokenPayloadDecoded = String(bytes, StandardCharsets.UTF_8)
        var tokenPayloadAsJson = JSONObject()
        try {
            tokenPayloadAsJson = JSONObject(tokenPayloadDecoded)
            Log.d("Token N Payload as JSON", tokenPayloadAsJson.toString())
        } catch (t: Throwable) {
            Log.e("Token N Payload as JSON", "Could not parse malformed JSON: \"$tokenPayloadDecoded\"")
        }

        return if (tokenPayloadAsJson.has("zoneinfo")) {
            if (tokenPayloadAsJson.get("zoneinfo") == "1") {
                tokenEditor.putString("email_N", tokenPayloadAsJson.get("email") as String?)
                LoginLicenseAgreed
            } else {
                LoginAuthNSuccess(
                    AuthData(mAuthStateManager.current.accessToken!!,
                        "SecretKey",
                        mAuthStateManager.current.idToken!!,
                        mAuthStateManager.current.accessTokenExpirationTime.toString()
                    )
                )
            }
        } else {
            LoginAuthNSuccess(
                AuthData(mAuthStateManager.current.accessToken!!,
                    "SecretKey",
                    mAuthStateManager.current.idToken!!,
                    mAuthStateManager.current.accessTokenExpirationTime.toString()
                )
            )
        }
    }

    /*
    override fun getAuthZTokenURL(): String {
        return mConfiguration.apiGateway.toString() + "/sts"
    }

    override fun getAuthZRequest(authzTokenEndpoint: String, idToken: String?, accessToken: String?): HttpURLConnection {
        val endpoint = "$authzTokenEndpoint?accessToken=$accessToken"
        val conn = URL(endpoint).openConnection() as HttpURLConnection
        conn.setRequestProperty("Authorization", "Bearer $idToken")

        return conn
    }

    override fun extractAuthZToken(response: String): String {
        val parsedResponse = response.replace("SecretAccessKey", "SecretKey", false)
        val jsonResponse = JSONObject(parsedResponse)
        val expectedJSONobject = jsonResponse.getJSONObject("data")
        return expectedJSONobject.toString()
    }
    */

}