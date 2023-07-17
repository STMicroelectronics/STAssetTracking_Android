package com.st.assetTracking.dashboard.communication.aws

import android.util.Log
import com.babbel.mobile.android.commons.okhttpawssigner.OkHttpAwsV4Signer
import com.st.assetTracking.dashboard.BuildConfig.AWS_HOST
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AwsSigingInterceptor(signer: OkHttpAwsV4Signer) : Interceptor {

   private val signer: OkHttpAwsV4Signer
   private val dateFormat: ThreadLocal<SimpleDateFormat>
   private var accessKey: String? = null
   private var secretKey: String? = null
   private var sessionToken: String? = null

   @Throws(IOException::class)
   override fun intercept(chain: Interceptor.Chain): Response {
       Log.i("TEST", "INTERCEPTED!")
       val originalRequest = chain.request()
       val newRequest = originalRequest.newBuilder()
               .addHeader("host", AWS_HOST)
               .addHeader("x-amz-date", dateFormat.get()!!.format(Date()))
               .addHeader("X-Amz-Security-Token", sessionToken!!)
               .build()
       val signedRequest = signer.sign(newRequest, accessKey, secretKey)
       return chain.proceed(signedRequest)
   }

   fun setCredentials(accessKey: String?, secretKey: String?, sessionToken: String?) {
       Log.i("TEST", "CREDENTIALS SET!")
       this.accessKey = accessKey
       this.secretKey = secretKey
       this.sessionToken = sessionToken
   }

   init {
       Log.i("TEST", "INTERCEPTOR!")
       dateFormat = object : ThreadLocal<SimpleDateFormat>() {
           override fun initialValue(): SimpleDateFormat {
               val localFormat: SimpleDateFormat
               localFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
               localFormat.timeZone = TimeZone.getTimeZone("UTC")
               return localFormat
           }
       }
       this.signer = signer
   }

}