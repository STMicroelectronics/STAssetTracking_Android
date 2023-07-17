package com.st.assetTracking.management.deviceData

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.st.assetTracking.R
import com.st.assetTracking.dashboard.model.LastDeviceLocations

class LastDevicesLocationActivity : AppCompatActivity() {
    private lateinit var leafletWebView: WebView

    companion object {
        private const val DEVICE_LOCATION_DATA = "Last Device Location"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide title bar
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {}

        val lastLocationList = intent.getSerializableExtra(DEVICE_LOCATION_DATA) as List<LastDeviceLocations>

        println("LAST LOCATION LIST : $lastLocationList")

        setContentView(R.layout.activity_last_devices_location)

        findViewById<ImageButton>(R.id.back_to_device_list).setOnClickListener{
            onBackPressed()
        }

        leafletWebView = findViewById(R.id.lastLocationLeafletWebView)
        leafletWebView.addJavascriptInterface(LocationSender(lastLocationList), "Android")
        leafletWebView.webChromeClient = WebChromeClient()
        leafletWebView.settings.javaScriptEnabled = true
        leafletWebView.loadUrl(("file:///android_asset/www/indexLastLocations.html"))

    }

    private class LocationSender(data: List<LastDeviceLocations>) {

        private val jsonLocationStr:String = Gson().toJson(data.sortedByDescending { it.date })

        @JavascriptInterface
        fun getLocations(): String {
            return jsonLocationStr
        }
    }
}