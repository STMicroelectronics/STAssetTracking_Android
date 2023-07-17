package com.st.polaris.ble

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Feature.FeatureListener
import com.st.BlueSTSDK.Features.*
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.ActivityWithNode
import com.st.assetTracking.dashboard.communication.aws.AwsAssetTrackingService
import com.st.assetTracking.dashboard.model.Device
import com.st.assetTracking.dashboard.model.LocationData
import com.st.assetTracking.dashboard.persistance.DeviceListRepository
import com.st.assetTracking.dashboard.provisioning.ProvisioningDevice
import com.st.assetTracking.dashboard.uploadData.uploader.UploaderGenericDataActivity
import com.st.assetTracking.dashboard.util.LocationService
import com.st.assetTracking.data.DataSample
import com.st.assetTracking.data.GyroDataClass
import com.st.assetTracking.data.SensorDataSample
import com.st.login.loginprovider.LoginProviderFactory
import com.st.polaris.R
import com.st.demos.polaris.ExtConfig.ExtConfigurationActivity
import com.st.login.*
import com.st.login.loginprovider.CognitoLoginProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue
import kotlin.math.sqrt

class BlePolarisConfiguration : ActivityWithNode() {

    private var networkConnection: Boolean = false

    /* Graphics elements */
    private lateinit var mTemperatureText: TextView
    private lateinit var mPressureText: TextView
    private lateinit var mHumidityText: TextView
    private lateinit var mGNSSText: TextView
    private lateinit var mAccText: TextView
    private lateinit var mGyroText: TextView
    private lateinit var samplingTime : EditText
    private lateinit var mLocateOnMapButton: FloatingActionButton
    private lateinit var mWebView: WebView

    private lateinit var mCloudSync: MaterialButton

    private var mEnableButtonOnlyOneTime= true

    private var arrSensorDataSample: ArrayList<DataSample> = ArrayList()
    private var mHandler: Handler = Handler()
    private var mRunnable : Runnable? = null

    //private lateinit var backArrow: ImageButton
    //private lateinit var extConfigurationButton: ImageButton

    /* List of features */
    private lateinit var mTemperature: List<FeatureTemperature>
    private lateinit var mHumidity: List<FeatureHumidity>
    private lateinit var mPressure: List<FeaturePressure>

    private var temperatureDataSample: DataSample? = null
    private var humidityDataSample: DataSample? = null
    private var pressureDataSample: DataSample? = null
    private var accelerationDataSample: DataSample? = null
    private var gyroscopeDataSample: DataSample? = null

    private var mAcc: FeatureAcceleration? =null
    private var mGyro: FeatureGyroscope? =null

    private var mGNSS : FeatureGNSS? =null

    /* DSH variable*/
    private lateinit var authenticationData: AuthData
    private lateinit var deviceListRepository: DeviceListRepository

    /* Other variables */
    private var mCurrentPosition: LocationData?=null
    private var syncTime: Long = 0

    //private var arrAccelerometer = ArrayList<AwsAssetTrackingService.AccForPolaris>()
    //private var arrGyrosope = ArrayList<AwsAssetTrackingService.GyroForPolaris>()

    companion object {
        var deviceID: String = ""
        fun startWithNode(context: Context, node: Node, id: String): Intent {
            deviceID = id
            return getStartIntent(
                context,
                BlePolarisConfiguration::class.java,
                node,
                true
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun startUpLayout(){
        // Check Phone Connection
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.let {
            try{
                it.registerDefaultNetworkCallback(@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        //take action when network connection is gained
                        networkConnection = true
                    }
                    override fun onLost(network: Network) {
                        //take action when network connection is lost
                        networkConnection = false
                    }
                })
            }catch (e: NoSuchMethodError){ networkConnection = true }
        }

        mTemperatureText = findViewById(R.id.thermometerText)
        mPressureText = findViewById(R.id.barometerText)
        mHumidityText = findViewById(R.id.humidityText)
        mGNSSText = findViewById(R.id.gnssText)
        mAccText = findViewById(R.id.accText)
        mGyroText = findViewById(R.id.gyroText)
        samplingTime = findViewById(R.id.register_ble_device_name)

        mCloudSync = findViewById(R.id.btn_polaris_cloud_sync)

        mLocateOnMapButton = findViewById(R.id.gnss_locate_on_map)
        mWebView = findViewById(R.id.gnss_webview)
        mWebView.webChromeClient = WebChromeClient()
        mWebView.settings.javaScriptEnabled = true

        //backArrow = findViewById(R.id.back_arrow_button)
        //extConfigurationButton = findViewById(R.id.ext_configuration_button)

        mCloudSync.setOnClickListener {
            if(networkConnection) {
                if(samplingTime.text.toString().toInt() in 5..60){
                    if (mCloudSync.text == "START SYNC") {
                        mCloudSync.text = "STOP SYNC"
                        node?.let { n -> enableNotification(n) }
                        val samplingTime = samplingTime.text.toString().trim()
                        syncTime = samplingTime.toLong() * 1000
                        print("SAMPLING TIME: $syncTime \n")
                        sendData()
                    } else {
                        resetLayout()
                    }
                }else{
                    showSnackbar(window.decorView.rootView, "Please, express Sampling Time in seconds. Valid interval: [5..60](s).")
                }
            } else {
                showSnackbar(window.decorView.rootView, "Offline. Check your Internet connection.")
            }
        }

        mLocateOnMapButton.setOnClickListener { setPositionOnMaps() }
    }

    private fun resetLayout(){
        if(mRunnable!=null) {
            mHandler.removeCallbacks(mRunnable!!)
            mHandler.removeCallbacksAndMessages(mRunnable!!)
        }
        disableNotification(node!!)

        mLocateOnMapButton.isEnabled = false

        mCloudSync.text = "START SYNC"
        mTemperatureText.text = "--- No data Available ---"
        mPressureText.text = "--- No data Available ---"
        mHumidityText.text = "--- No data Available ---"
        mGNSSText.text = "--- No data Available ---"
        mAccText.text = "--- No data Available ---"
        mGyroText.text = "--- No data Available ---"
    }

    @SuppressLint("AddJavascriptInterface")
    private fun setPositionOnMaps() {
        if(mCurrentPosition!=null) {
            mWebView.visibility = View.VISIBLE
            mWebView.loadUrl("file:///android_asset/gnss_leaflat.html")
            mWebView.addJavascriptInterface(SetLocation(mCurrentPosition!!), "Android")
        }
    }
    private class SetLocation(data: LocationData) {

        private val jsonLocationStr:String = Gson().toJson(data)

        @JavascriptInterface
        fun setLocation(): String {
            return jsonLocationStr
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        title = "Real Time Sync"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_polaris_configuration)

        startUpLayout()

        /*backArrow.setOnClickListener{
            setResult(1, Intent())
            onBackPressed()
        }
        extConfigurationButton.setOnClickListener{
            if(mRunnable!=null) {
                mHandler.removeCallbacks(mRunnable!!)
                mHandler.removeCallbacksAndMessages(mRunnable!!)
            }
            disableNotification(node!!)
            resetLayout()
            val intent = ExtConfigurationActivity.startWithNode(this, node!!)
            startActivity(intent)
        }*/

        mTemperature = listOfNotNull()
        mHumidity = listOfNotNull()
        mPressure = listOfNotNull()

        val loginManager = LoginManager(
            this@BlePolarisConfiguration.activityResultRegistry,
            this@BlePolarisConfiguration,
            applicationContext,
            LoginProviderFactory.LoginProviderType.COGNITO,
            Configuration.getInstance(applicationContext, com.st.login.R.raw.auth_config_cognito)
        )

        loginManager.getAtrAuthData { authData ->
            if (authData != null) {
                authenticationData = authData
                val deviceListRemote = AwsAssetTrackingService(authenticationData, applicationContext)
                deviceListRepository = DeviceListRepository(authenticationData, deviceListRemote)

                CoroutineScope(Dispatchers.IO).launch {
                    val apiKey = deviceListRepository.registerApiKey()
                    val sharePrefEditor: SharedPreferences.Editor =
                        applicationContext.getSharedPreferences(
                            "TokenCollection",
                            Context.MODE_PRIVATE
                        ).edit()
                    sharePrefEditor.putString("ApiKey", apiKey.apiKey)
                    sharePrefEditor.putString("Owner", apiKey.owner)
                    sharePrefEditor.apply()
                }
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            super.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSnackbar(view: View, message: String) {
        Snackbar.make(view,
                message,
                Snackbar.LENGTH_SHORT)
                .show()
    }

    override fun onDestroy() {
        if(mRunnable!=null) {
            mHandler.removeCallbacks(mRunnable!!)
            mHandler.removeCallbacksAndMessages(mRunnable!!)
        }
        disableNotification(node!!)
        super.onDestroy()
    }

    override fun onBackPressed() {
        if(mRunnable!=null) {
            mHandler.removeCallbacks(mRunnable!!)
            mHandler.removeCallbacksAndMessages(mRunnable!!)
        }
        disableNotification(node!!)
        val intent = Intent()
        setResult(1, intent)
        super.onBackPressed()
    }

    /**
     * Enable all BLE notifications.
     */
    private fun enableNotification(node: Node){

        /*Enable BLE Temperature Notification*/
        mTemperature = node.getFeatures(FeatureTemperature::class.java)
        if (mTemperature.isNotEmpty()) {
            for (f in mTemperature) {
                f.addFeatureListener(mTemperatureListener)
                node.enableNotification(f)
            }
        }

        /*Enable BLE Pressure Notification*/
        mPressure = node.getFeatures(FeaturePressure::class.java)
        if (mPressure.isNotEmpty()) {
            for (f in mPressure) {
                f.addFeatureListener(mPressureListener)
                node.enableNotification(f)
            }
        }

        /*Enable BLE Humidity Notification*/
        mHumidity = node.getFeatures(FeatureHumidity::class.java)
        if (mHumidity.isNotEmpty()) {
            for (f in mHumidity) {
                f.addFeatureListener(mHumidityListener)
                node.enableNotification(f)
            }
        }

        /*Enable BLE GNSS Notification*/
        mGNSS = node.getFeature(FeatureGNSS::class.java)
        mGNSS?.apply {
            addFeatureListener(mGNSSListener)
            enableNotification()
        }

        /*Enable BLE Accelerometer Notification*/
        mAcc = node.getFeature(FeatureAcceleration::class.java)
        mAcc?.apply {
            addFeatureListener(mAccListner)
            enableNotification()
        }

        /*Enable BLE Gyroscope Notification*/
        mGyro = node.getFeature(FeatureGyroscope::class.java)
        mGyro?.apply {
            addFeatureListener(mGyroListner)
            enableNotification()
        }

    }

    /**
     * Disable all BLE notifications.
     */
    private fun disableNotification(node: Node){
        /*Disable BLE Temperature Notification*/
        if (mTemperature.isNotEmpty()) {
            for (f in mTemperature) {
                f.removeFeatureListener(mTemperatureListener)
                node.disableNotification(f)
            }
        }

        /*Disable BLE Pressure Notification*/
        if (mPressure.isNotEmpty()) {
            for (f in mPressure) {
                f.removeFeatureListener(mPressureListener)
                node.disableNotification(f)
            }
        }

        /*Disable BLE Humidity Notification*/
        if (mHumidity.isNotEmpty()) {
            for (f in mHumidity) {
                f.removeFeatureListener(mHumidityListener)
                node.disableNotification(f)
            }
        }

        /*Disable BLE GNSS Notification*/
        node.getFeature(FeatureGNSS::class.java)?.apply {
            removeFeatureListener(mGNSSListener)
            disableNotification()
        }
        mGNSS = null

        /*Disable BLE Accelerometer Notification*/
        node.getFeature(FeatureAcceleration::class.java)?.apply {
            removeFeatureListener(mAccListner)
            disableNotification()
        }
        mAcc = null

        /*Disable BLE Gyroscope Notification*/
        node.getFeature(FeatureGyroscope::class.java)?.apply {
            removeFeatureListener(mGyroListner)
            disableNotification()
        }
        mGyro = null

    }

    /**
     * Send data to cloud DSH.
     */
    private fun sendData(){

        mHandler.postDelayed(Runnable {
            mHandler.postDelayed(mRunnable!!, syncTime)

            if(temperatureDataSample!=null && humidityDataSample!=null && pressureDataSample!=null) {
                arrSensorDataSample.add(temperatureDataSample!!)
                arrSensorDataSample.add(pressureDataSample!!)
                arrSensorDataSample.add(humidityDataSample!!)
                if(accelerationDataSample!=null && gyroscopeDataSample!=null){
                    arrSensorDataSample.add(accelerationDataSample!!)
                    arrSensorDataSample.add(gyroscopeDataSample!!)
                }
            }
            println("DATA SAMPLES: ${arrSensorDataSample.toList()}")
            CoroutineScope(Dispatchers.IO).launch {

                LocationService(applicationContext)
                println("DATA SAMPLES LIST: ${arrSensorDataSample.toList()}")
                AwsAssetTrackingService(authenticationData, applicationContext).uploadNewTelemetryData(
                    deviceID, "ble", arrSensorDataSample.toList(), mCurrentPosition)

                arrSensorDataSample.clear()

                /** TODO: REMOVE IT ONLY FOR DEMO
                AwsAssetTrackingService(authenticationData, applicationContext).uploadTelemetryAccForPolaris(
                    deviceID, arrAccelerometer.toList(), arrGyrosope.toList())

                arrAccelerometer.clear()
                arrGyrosope.clear()*/
            }

        }.also { mRunnable = it }, syncTime)

    }

    /**
     * get the string to show containing a value for each line
     *
     * @param format format used for print the value and the unit
     * @param unit   unit to use
     * @param values array with the value to print
     * @return string showing all the value, one for each line
     */
    private fun getDisplayString(
        format: String, unit: String,
        values: FloatArray, offset: Float
    ): String? {
        val sb = StringBuilder()
        for (i in 0 until values.size - 1) {
            //Add Offset to all the Values
            values[i] += offset
            sb.append(String.format(format, values[i], unit))
            sb.append('\n')
        } //for
        values[values.size - 1] += offset
        sb.append(String.format(format, values[values.size - 1], unit))
        return sb.toString()
    }

    private interface ExtractDataFunction {
        fun getData(s: Feature.Sample?): Float
    }

    /**
     * get a sample for each feature and extract the float data from it
     *
     * @param features            list of feature
     * @param extractDataFunction object that will extract the data from the sample
     * @return list of values inside the feature
     */
    private fun extractData(
        features: List<Feature?>,
        extractDataFunction: ExtractDataFunction
    ): FloatArray? {
        val nFeature = features.size
        val data = FloatArray(nFeature)
        for (i in 0 until nFeature) {
            data[i] = extractDataFunction.getData(features[i]!!.sample)
        } //for
        return data
    } //extractData

    /**
     * ////////////
     * TEMPERATURE
     * ///////////
     */
    private val TEMP_FORMAT = "%.1f [%s]"

    /**
     * object that extract the temperature from a feature sample
     */
    private val sExtractDataTemp: ExtractDataFunction? =
        object : ExtractDataFunction {
            override fun getData(s: Feature.Sample?): Float {
                return FeatureTemperature.getTemperature(
                    s
                )
            }
        }

    private val mTemperatureListener =
        Feature.FeatureListener { _, _ ->
            val data: FloatArray = sExtractDataTemp?.let {
                extractData(
                    mTemperature,
                    it
                )
            }!!

            val unit: String? = mTemperature[0].fieldsDesc[0].unit

            val dataString: String = unit?.let {
                getDisplayString(
                    TEMP_FORMAT,
                    it,
                    data,
                    0.0f
                )
            }!!

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    mTemperatureText.text = dataString
                    //val temps = dataString.split("\n")
                    //arrSensorDataSample.add(SensorDataSample(getDate()!!, data[0].toFloat(), null, null, null))
                    temperatureDataSample = SensorDataSample(getDate()!!, data[0], null, null, null, null)
                } catch (e: NullPointerException) {
                    //this exception can happen when the task is run after the fragment is
                    // destroyed
                }
            }
        }

    /**
     * ////////////
     * PRESSURE
     * ///////////
     */
    private val PRES_FORMAT = "%.2f [%s]"

    private val sExtractDataPres: ExtractDataFunction? =
        object : ExtractDataFunction {
            override fun getData(s: Feature.Sample?): Float {
                return FeaturePressure.getPressure(
                    s
                )
            }
        }

    private val mPressureListener =
        FeatureListener { _, _ ->
            val unit = mPressure[0].fieldsDesc[0].unit
            val data: FloatArray = sExtractDataPres?.let {
                extractData(
                    mPressure,
                    it
                )
            }!!

            val dataString: String = unit?.let {
                getDisplayString(
                    PRES_FORMAT,
                    unit,
                    data,
                    0.0f
                )
            }!!

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    //dataPressure.add(dataString)
                        pressureDataSample = SensorDataSample(getDate()!!, null, data[0], null, null, null)
                    //arrSensorDataSample.add(SensorDataSample(getDate()!!, null, data[0].toFloat(), null, null))
                    mPressureText.text = dataString
                } catch (e: java.lang.NullPointerException) {
                    //this exception can happen when the task is run after the fragment is
                    // destroyed
                } //try-catch
            }
        }

    /**
     * ////////////
     * HUMIDITY
     * ///////////
     */
    private val HUM_FORMAT = "%.1f [%s]"

    private val sExtractDataHum: ExtractDataFunction? =
        object : ExtractDataFunction {
            override fun getData(s: Feature.Sample?): Float {
                return FeatureHumidity.getHumidity(
                    s
                )
            }
        }

    private val mHumidityListener =
        FeatureListener { _, _ ->
            val unit = mHumidity[0].fieldsDesc[0].unit
            val data: FloatArray = sExtractDataHum?.let {
                extractData(
                    mHumidity,
                    it
                )
            }!!

            val dataString: String = unit?.let {
                getDisplayString(
                    HUM_FORMAT,
                    unit,
                    data,
                    0.0f
                )
            }!!

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    humidityDataSample = SensorDataSample(getDate()!!, null, null, data[0].toFloat(), null, null)
                    //arrSensorDataSample.add(SensorDataSample(getDate()!!, null, null, data[0].toFloat(), null))
                    //dataHumidity.add(dataString)
                    mHumidityText.text = dataString
                } catch (e: java.lang.NullPointerException) {
                    //this exception can happen when the task is run after the fragment is
                    // destroyed
                }
            }
        } //on update


    /**
     * ////////////
     * GNSS
     * ///////////
     */
    private val mGNSSListener = Feature.FeatureListener { _, sample ->
        val latitude = FeatureGNSS.getLatitudeValue(sample)
        val longitude = FeatureGNSS.getLongitudeValue(sample)
        val altitude = FeatureGNSS.getAltitudeValue(sample)
        val numSatellites = FeatureGNSS.getNSatValue(sample)
        val sigQuality = FeatureGNSS.getSigQualityValue(sample)

        if((latitude!=null) && (longitude!=null)) {
            val date = getDate()
            if(date != null){
                mCurrentPosition = LocationData(latitude,longitude, date)
                if(mLocateOnMapButton!=null) {
                    // Enable the Button
                    CoroutineScope(Dispatchers.Main).launch {
                        mLocateOnMapButton.isEnabled = mEnableButtonOnlyOneTime
                    }
                }
            }
        }

        CoroutineScope(Dispatchers.Main).launch {

            var coordinates = ""

            if(latitude!=null) {
                coordinates += "Latitude: ${latitude.absoluteValue}"
                coordinates += if(latitude>=0) {
                    "N"
                } else {
                    "S"
                }
            }

            if(longitude!=null) {
                coordinates += "\nLongitude: ${longitude.absoluteValue}"
                coordinates += if(longitude>=0) {
                    "E"
                } else {
                    "W"
                }
            }

            if(altitude!=null) {
                coordinates += "\nAltitude: $altitude m"
            }

            if(numSatellites!=null) {
                coordinates += "\nSatellites: $numSatellites"
            }

            if(sigQuality!=null) {
                coordinates += " Signal Quality: $sigQuality [db-Hz]"
            }

            mGNSSText.text = coordinates
        }
    }

    /**
     * ////////////
     * ACCELEROMETER
     * ///////////
     */
    private val mAccListner = Feature.FeatureListener { _, sample ->

        val x = FeatureAcceleration.getAccX(sample)
        val y = FeatureAcceleration.getAccY(sample)
        val z = FeatureAcceleration.getAccZ(sample)

        val accValToShow = "[x: $x] - [y: $y] - [z: $z]"
        val accValue = sqrt(x*x + y*y + z*z)
        accelerationDataSample = SensorDataSample(Date(), null, null, null, accValue, null)
        CoroutineScope(Dispatchers.Main).launch {
            mAccText.text = accValToShow
        }
    }

    /**
     * ////////////
     * GYROSCOPE
     * ///////////
     */
    private val mGyroListner = Feature.FeatureListener { _, sample ->

        val x = FeatureGyroscope.getGyroX(sample)
        val y = FeatureGyroscope.getGyroY(sample)
        val z = FeatureGyroscope.getGyroZ(sample)

        val gyroValToShow = "[x: $x] - [y: $y] - [z: $z]"
        gyroscopeDataSample = SensorDataSample(Date(), null, null, null, null, GyroDataClass(x, y, z))
        CoroutineScope(Dispatchers.Main).launch {
            mGyroText.text = gyroValToShow
        }
    }

    private fun getDate() : Date?{
        val localFormat = SimpleDateFormat("dd/MM/yy HH:mm:ss.SSS", Locale.ROOT)
        return try {
            val date = Date()
            val dateTime: String = localFormat.format(date)
            localFormat.parse(dateTime)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }
}