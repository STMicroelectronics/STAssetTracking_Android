package com.st.assetTracking.lora

import android.app.PendingIntent
import android.content.*
import android.content.pm.ActivityInfo
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
//import com.st.assetTracking.dashboard.AssetTrackerDashboard
import com.st.assetTracking.dashboard.AssetTrackingDeviceProfile
import com.st.assetTracking.dashboard.AssetTrackingUploadData
import com.st.assetTracking.lora.databinding.ActivityAtrLoraMainBinding
import com.st.login.Configuration
import com.st.login.LoginManager
import com.st.login.loginprovider.LoginProviderFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.Executors


class AtrLoraMainActivity : AppCompatActivity(), SerialInputOutputManager.Listener{

    private lateinit var binding: ActivityAtrLoraMainBinding

    private val INTENT_ACTION_GRANT_USB: String = "com.st.assetTracking.lora.GRANT_USB"
    private val WRITE_WAIT_MILLIS = 2000

    private enum class UsbPermission {
        Unknown, Requested, Granted, Denied
    }

    private var usbPermission: UsbPermission = UsbPermission.Unknown
    private var usbIoManager: SerialInputOutputManager? = null
    private var usbSerialPort: UsbSerialPort? = null
    private var connected = false

    private var portNum:Int = 0
    private lateinit var consoleLOG : TextView
    private var mainLooper: Handler? = null
    private var broadcastReceiver: BroadcastReceiver? = null

    private val readDataSync = true

    private var stages: Int = 0

    private lateinit var devEui: String
    private lateinit var fwVersion: String
    private var appEui: String = ""
    private var appKey: String = ""

    private val readerLoRaParamCollection = "DeviceProfile"

    private var skip: Boolean = false
    private var completed: Boolean = false

    override fun onResume() {
        super.onResume()

        this.registerReceiver(broadcastReceiver, IntentFilter(INTENT_ACTION_GRANT_USB))
        if(!completed){
            if (usbPermission == UsbPermission.Unknown || usbPermission == UsbPermission.Granted) mainLooper!!.post { connect() }
        }

    }
    override fun onPause() {
        if (connected) {
            Toast.makeText(applicationContext, "disconnected", Toast.LENGTH_LONG).show()
            disconnect()
        }
        this.unregisterReceiver(broadcastReceiver)
        super.onPause()
    }

    override fun onDestroy() {
        val readerLoRaparam: SharedPreferences = applicationContext.getSharedPreferences(readerLoRaParamCollection, Context.MODE_PRIVATE)
        readerLoRaparam.edit().clear().apply()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityAtrLoraMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        title = "LoRa configuration"
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        consoleLOG = findViewById(R.id.tvLOG)

        if(!completed){
            stages = 0

            /*PendingIntent.getBroadcast(this, 0, Intent(INTENT_ACTION_GRANT_USB), 0)
            val filter = IntentFilter(INTENT_ACTION_GRANT_USB)
            registerReceiver(usbReceiver, filter)

            val fbSerialConsole : FloatingActionButton = findViewById(R.id.fb_serial)
            fbSerialConsole.setOnClickListener{
                //Add serial console
            }*/

            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == INTENT_ACTION_GRANT_USB) {
                        usbPermission = if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) UsbPermission.Granted else UsbPermission.Denied
                        connect()
                    }
                }
            }
            mainLooper = Handler(Looper.getMainLooper())


            val btnCloudRegistration: Button = findViewById(R.id.btn_cloud_registration)
            btnCloudRegistration.setOnClickListener{
                skip = true
                val resultRegistry = activityResultRegistry
                val ctx = applicationContext
                CoroutineScope(Dispatchers.Main).launch {
                    val authData = LoginManager(
                        resultRegistry,
                        this@AtrLoraMainActivity,
                        ctx,
                        LoginProviderFactory.LoginProviderType.COGNITO,
                        Configuration.getInstance(
                            ctx,
                            com.st.login.R.raw.auth_config_cognito)
                    ).login()
                    if(authData != null) {
                        AssetTrackingDeviceProfile.startActivityDeviceProfileFrom(authData, applicationContext, devEui, "lora")
                    }
                }
            }

            val btnLogConsole: MaterialButton = findViewById(R.id.btnShowSerialConsole)
            btnLogConsole.setOnClickListener{
                binding.cvLogConsole.visibility = View.VISIBLE
            }

            val btnShowDeviceOnCloud: MaterialButton = findViewById(R.id.btnShowDeviceOnCloud)
            btnShowDeviceOnCloud.setOnClickListener{
                //val intent = Intent(applicationContext, AssetTrackerDashboard::class.java)
                //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                //applicationContext.startActivity(intent)
            }
        }

    }

    private fun resetLayout(){
        binding.layoutLoraSettings.visibility = View.GONE

        binding.pbRetrieveDevEUI.visibility = View.VISIBLE
        binding.ivRetryRetrieveDevEUI.visibility = View.GONE

        //ivInformationRetrival.visibility = View.GONE
        //pb_information_retrieval.visibility = View.VISIBLE
        binding.ivDeviceRegistration.visibility = View.GONE
        binding.pbDeviceRegistration.visibility = View.VISIBLE
        binding.ivConfigurationSettings.visibility = View.GONE
        binding.pbConfigurationSettings.visibility = View.VISIBLE

        binding.layoutCompleted.visibility = View.GONE
        binding.cvLogConsole.visibility = View.VISIBLE
    }

    /**
     * Layout management
     */
    private fun setupDefaultConnectedLayout(){
        binding.pbConnection.visibility = View.VISIBLE
        binding.ivConnected.visibility = View.GONE
        binding.ivRetry.visibility = View.GONE

        binding.pbConfigurationSettings.visibility = View.VISIBLE
        binding.ivConfigurationSettings.visibility = View.GONE
    }
    private fun setupNotConnectedLayout(){
        binding.pbConnection.visibility = View.GONE
        binding.ivConnected.visibility = View.GONE
        binding.ivRetry.visibility = View.VISIBLE
        binding.ivRetry.setOnClickListener{
            setupDefaultConnectedLayout()
            connect()
        }
    }
    private fun setupConnectedLayout(){
        binding.pbConnection.visibility = View.GONE
        binding.ivConnected.visibility = View.VISIBLE
        binding.ivRetry.visibility = View.GONE
        //startBoardSettingsconfiguration()
        checkFw()
    }
    private fun setupIncorrectFwLayout(){
        binding.pbCheckFwVersion.visibility = View.GONE
        binding.ivCheckFwVersion.visibility = View.GONE
        binding.iBlockCheckFwVersion.visibility = View.VISIBLE
        binding.pbRetrieveDevEUI.visibility = View.GONE

        binding.tvErrorFw.visibility = View.VISIBLE
        binding.tvErrorFw.text = " Current Fw version: $fwVersion ... Minimum version required is 2.2.0"
    }

    /**
     * Flow Execution Commands
     */
    private fun checkFw(){
        stages = 10
        send("?fwversion\n")
    }

    private fun startBoardSettingsconfiguration(){
        binding.pbCheckFwVersion.visibility = View.GONE
        binding.ivCheckFwVersion.visibility = View.VISIBLE
        binding.iBlockCheckFwVersion.visibility = View.GONE

        stages = 1
        send("?devicejoinparam\n")
    }
    private fun deviceEuiCompleted(){
        binding.layoutCloudDashboardRegistration.visibility = View.VISIBLE
        stages = 2
        binding.settingsBoardValue.text = "DeviceEUI: $devEui"

        binding.pbRetrieveDevEUI.visibility = View.GONE
        binding.ivRetrieveDevEUI.visibility = View.VISIBLE
        binding.ivRetryRetrieveDevEUI.visibility = View.GONE
    }
    private fun informationRetrievalCompleted(){
        val readerLoRaparam: SharedPreferences = applicationContext.getSharedPreferences(readerLoRaParamCollection, Context.MODE_PRIVATE)
        appEui = readerLoRaparam.getString("appEUI", "")!!
        appKey = readerLoRaparam.getString("appKEY", "")!!
        if(appEui == "" && appKey == ""){
            Snackbar.make(this.window.decorView, "Something went wrong. Check if device is already registered on cloud dashboard.", Snackbar.LENGTH_SHORT).show()
        }else{
            binding.layoutLoraSettings.visibility = View.VISIBLE
            binding.pbDeviceRegistration.visibility = View.GONE
            binding.ivDeviceRegistration.visibility = View.VISIBLE
            stages = 3
            send("!deviceeui-$devEui\n")
        }
    }
    private fun setUpDeviceEUICompleted(){
        stages = 4
        send("!joineui-$appEui\n")
    }
    private fun setUpJoinEUICompleted(){
        stages = 5
        send("!appkey-$appKey\n")
    }
    private fun setUpAppKeyCompleted(){
        stages = 6
        send("!ntwkkey-$appKey\n")
    }
    private fun setUpNtwKeyCompleted(){
        binding.pbConfigurationSettings.visibility = View.GONE
        binding.ivConfigurationSettings.visibility = View.VISIBLE
        stages = 0

        binding.layoutCompleted.visibility = View.VISIBLE

        completed = true
        send("!sysreset\n")
    }

    /**
     * Serial Console Management
     */
    override fun onNewData(data: ByteArray?) {
        mainLooper!!.post { receive(data!!) }
    }

    override fun onRunError(e: Exception) {
        mainLooper!!.post {
            Toast.makeText(applicationContext, "connection lost: " + e.message, Toast.LENGTH_LONG).show()
            disconnect()
        }
    }

    fun status(str: String) {
        val spn = SpannableStringBuilder(str+'\n')
        consoleLOG.append(spn)
    }

    private fun disconnect() {
        connected = false

        usbIoManager?.stop()
        usbIoManager = null
        try {
            usbSerialPort?.close()
        } catch (ignored: IOException) {}
        usbSerialPort = null
    }

    private fun connect() {

        var device: UsbDevice? = null

        val usbManager = this.getSystemService(Context.USB_SERVICE) as UsbManager

        val usbDefaultProber = UsbSerialProber.getDefaultProber()
        val usbCustomProber = CustomProber.customProber

        for (v in usbManager.deviceList.values){
            device = v
        }
        if (device == null) {
            status("connection failed: device not found")
            setupNotConnectedLayout()
            return
        }
        var driver = usbDefaultProber.probeDevice(device)
        if (driver == null) {
            driver = usbCustomProber.probeDevice(device)
        }
        if (driver == null) {
            status("connection failed: no driver for device")
            setupNotConnectedLayout()
            return
        }
        if (driver.ports.size < portNum) {
            status("connection failed: not enough ports at device")
            setupNotConnectedLayout()
            return
        }

        usbSerialPort = driver.ports[portNum]

        val usbConnection = usbManager.openDevice(device)
        if (usbConnection == null && usbPermission == UsbPermission.Unknown && !usbManager.hasPermission(device)){
            usbPermission = UsbPermission.Requested
            val usbPermissionIntent = PendingIntent.getBroadcast(this, 0, Intent(INTENT_ACTION_GRANT_USB), 0)
            usbManager.requestPermission(device, usbPermissionIntent)
            return
        }

        if (usbConnection == null) {
            if (!usbManager.hasPermission(device)){
                status("connection failed: permission denied")
                setupNotConnectedLayout()
            }else{
                status("connection failed: open failed")
                setupNotConnectedLayout()
            }
            return
        }

        try {
            usbSerialPort!!.open(usbConnection)
            usbSerialPort!!.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            if (readDataSync) {
                usbIoManager = SerialInputOutputManager(usbSerialPort, this)
                Executors.newSingleThreadExecutor().submit(usbIoManager)
            }

            status("connected\n")

            connected = true

            usbSerialPort!!.rts = true
            usbSerialPort!!.dtr = true

            if(skip){
                informationRetrievalCompleted()
            }else{
                setupConnectedLayout()
            }

        } catch (e: java.lang.Exception) {
            status("connection failed: " + e.message)
            Toast.makeText(applicationContext, "connection failed: " + e.message, Toast.LENGTH_LONG).show()
            setupNotConnectedLayout()
            disconnect()
        }
    }

    private fun send(str: String) {
        if (!connected) {
            Toast.makeText(applicationContext, "not connected", Toast.LENGTH_LONG).show()
            setupNotConnectedLayout()
            return
        }
        try {
            val data = str.toByteArray()
            val spn = SpannableStringBuilder()
            spn.append("send ${data.size} bytes")
            consoleLOG.append("SEND --> ${String(data)}")

            usbSerialPort!!.write(data, WRITE_WAIT_MILLIS)

        } catch (e: java.lang.Exception) {
            onRunError(e)
            Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun receive(data: ByteArray) {
        if (data.isEmpty()){
            Toast.makeText(applicationContext, "EMPTY", Toast.LENGTH_LONG).show()
        }
        if(stages==0){
            consoleLOG.append(String(data))
        }else{
            setupStages(data)
        }
    }

    private fun setupStages(data: ByteArray){
        val strData = String(data)

        if(stages==10){
            val pattern = "LoRa IOT tracker"
            if(strData.contains(pattern, ignoreCase = true)){
                fwVersion = strData.substringAfter(":")
                consoleLOG.append("RESP <-- ")
                consoleLOG.append("FW Version $fwVersion\n\n")
                if(fwVersion.contains("2.2.0", ignoreCase = true)){
                    startBoardSettingsconfiguration()
                }else{
                    Toast.makeText(applicationContext, "Current Version: $fwVersion \nMinimum version required is 2.2.0", Toast.LENGTH_LONG).show()
                    setupIncorrectFwLayout()
                }
            }
        }
        if(stages==1){
            val pattern = "DevEui(FromMcuId)"
            if(strData.contains(pattern, ignoreCase = true)){
                val devEuiFromMCUID = strData.substringAfter("=")
                devEui = devEuiFromMCUID.replace("\\s".toRegex(), "")
                consoleLOG.append("RESP <-- ")
                consoleLOG.append("Get DevEUI $devEui\n\n")
                deviceEuiCompleted()
            }
        }else if(stages==3){
            val pattern = "Command complete"
            if(strData.contains(pattern, ignoreCase = true)){
                consoleLOG.append("RESP <-- ")
                consoleLOG.append("Set deviceEUI $strData\n")
                setUpDeviceEUICompleted()
            }
        }else if(stages==4){
            val pattern = "Command complete"
            if(strData.contains(pattern, ignoreCase = true)){
                consoleLOG.append("RESP <-- ")
                consoleLOG.append("Set joinEUI $strData\n")
                setUpJoinEUICompleted()
            }
        }else if(stages==5){
            val pattern = "Command complete"
            if(strData.contains(pattern, ignoreCase = true)){
                consoleLOG.append("RESP <-- ")
                consoleLOG.append("Set appKey $strData\n")
                setUpAppKeyCompleted()
            }
        }else if(stages==6){
            val pattern = "Command complete"
            if(strData.contains(pattern, ignoreCase = true)){
                consoleLOG.append("RESP <-- ")
                consoleLOG.append("Set ntwKey $strData\n")
                setUpNtwKeyCompleted()
            }
        }

    }

}