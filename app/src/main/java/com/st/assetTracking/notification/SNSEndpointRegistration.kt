package com.st.assetTracking.notification

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.amazonaws.services.sns.model.*
import com.babbel.mobile.android.commons.okhttpawssigner.OkHttpAwsV4Signer
import com.st.assetTracking.dashboard.BuildConfig
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.google.gson.*
import com.st.assetTracking.dashboard.communication.aws.AwsSigingInterceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody


internal interface TokenRegistrationUtilityApi {

    @POST("1/device-token/")
    @Headers("Content-Type: application/json")
    suspend fun uploadToken(@Body body: RequestBody): Response<ResponseBody>
}


class TokenRegistrationUtility(private val context: Context) {
    private val tokenRegistrationApi : TokenRegistrationUtilityApi
    private val Auth_config_token = "TokenCollection"
    private val signer: OkHttpAwsV4Signer = OkHttpAwsV4Signer(BuildConfig.AWS_REGION, "execute-api")
    private val signingInterceptor = AwsSigingInterceptor(signer)
    private val tokenReader: SharedPreferences = context.getSharedPreferences(Auth_config_token, Context.MODE_PRIVATE)

    private var accessKeyZ : String = ""
    private var secretKeyZ : String = ""
    private var sessionTokenZ : String = ""

    init {
        accessKeyZ = tokenReader.getString("accessKey_Z", "")!!
        secretKeyZ = tokenReader.getString("secretKey_Z", "")!!
        sessionTokenZ = tokenReader.getString("sessionToken_Z", "")!!

        signingInterceptor.setCredentials(accessKeyZ, secretKeyZ, sessionTokenZ)

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY


        val httpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                            .build()
                    return@addInterceptor chain.proceed(request)
                }
                .addInterceptor(signingInterceptor)
                .addInterceptor(loggingInterceptor)

        /*val gson: Gson = GsonBuilder()
                .setLenient()
                .create()*/
        tokenRegistrationApi = Retrofit.Builder()
                .baseUrl(BuildConfig.AWS_ENDPOINT)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TokenRegistrationUtilityApi::class.java)
    }

    suspend fun registerWithSNS(deviceToken: String) {
        var endpointArn = retrieveEndpointArn()
        var updateNeeded = false
        var createNeeded = null == endpointArn
        //Log.d("RegisterSNS",client.endpoint)
        if (createNeeded) {
            // No platform endpoint ARN is stored; need to call createEndpoint.
            endpointArn = createEndpoint(deviceToken)
            createNeeded = false
        }
        Log.d("RegisterSNS","Retrieving platform endpoint data...")
    }

    /**
     * @return never null
     */
    private suspend fun createEndpoint(token: String): String? {
        var endpointArn: String? = null
        try {
            Log.d("RegisterSNS","Creating platform endpoint with token $token")
            val jsonRequest = JsonObject()
            jsonRequest.addProperty("token", token)

            // Call REST API to register device token
            val bodyRequest: RequestBody = jsonRequest.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            tokenRegistrationApi.uploadToken(bodyRequest)

        } catch (ipe: InvalidParameterException) {
            val message = ipe.errorMessage
            Log.d("RegisterSNS","Exception message: $message")
            val p = Pattern
                    .compile(".*Endpoint (arn:aws:sns[^ ]+) already exists " +
                            "with the same [Tt]oken.*")
            val m = p.matcher(message)
            endpointArn = if (m.matches()) {
                // The platform endpoint already exists for this token, but with
                // additional custom data that
                // createEndpoint doesn't want to overwrite. Just use the
                // existing platform endpoint.
                m.group(1)
            } else {
                // Rethrow the exception, the input is actually bad.
                throw ipe
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
        if(endpointArn!=null)
            storeEndpointArn(endpointArn)
        return endpointArn
    }

    /**
     * @return the ARN the app was registered under previously, or null if no
     * platform endpoint ARN is stored.
     */
    private fun retrieveEndpointArn(): String? {
        val storage = context.getSharedPreferences(TokenRegistrationUtility::class.java.name,Context.MODE_PRIVATE)
        return storage.getString(ENDPOINT_ARN_KEY,null)
    }

    /**
     * Stores the platform endpoint ARN in permanent storage for lookup next time.
     */
    private fun storeEndpointArn(endpointArn: String) {
        val storage = context.getSharedPreferences(TokenRegistrationUtility::class.java.name,Context.MODE_PRIVATE)
        storage.edit()
                .putString(ENDPOINT_ARN_KEY,endpointArn)
                .apply()
    }

    companion object{
        val ENDPOINT_ARN_KEY =  TokenRegistrationUtility::class.java.name+".ENDPOINT_ARN_KEY"
        private const val ENABLE_KEY = "Enabled"
        private const val TOKEN_KEY = "Token"
    }
}

private suspend fun <T> Future<T>.await():T = suspendCoroutine { continuation ->
    val value = get()
    continuation.resume(value)
}