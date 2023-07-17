package com.st.assetTracking.management.deviceData

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.st.assetTracking.dashboard.R
import com.st.assetTracking.dashboard.model.LocationData


internal class SingleDeviceLocationFragment : Fragment() {
    private lateinit var data: List<LocationData>
    lateinit var leafletWebView: WebView

    companion object {
        private const val DEVICE_LOCATION_DATA = "Device Location"

        internal fun newInstance(locationData: List<LocationData>): SingleDeviceLocationFragment {
            val fragment = SingleDeviceLocationFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(DEVICE_LOCATION_DATA, ArrayList(locationData))
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        data = requireArguments().getSerializable(DEVICE_LOCATION_DATA) as List<LocationData>
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_webview_device_location, container, false)

        leafletWebView = rootView.findViewById(R.id.leafletWebView)
        leafletWebView.addJavascriptInterface(LocationSender(data), "Android")
        leafletWebView.webChromeClient = WebChromeClient()
        leafletWebView.settings.javaScriptEnabled = true
        leafletWebView.loadUrl(("file:///android_asset/www/index.html"))

        return rootView
    }

    private class LocationSender(data: List<LocationData>) {

        private val jsonLocationStr:String = Gson().toJson(data.sortedByDescending { it.date })

        @JavascriptInterface
        fun getLocations(): String {
            return jsonLocationStr
        }
    }
}