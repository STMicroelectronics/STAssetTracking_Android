package com.st.assetTracking.management.deviceInfo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.st.assetTracking.R
import com.st.assetTracking.atrBle1.sensorTileBox.AtrBleMainActivity
import com.st.assetTracking.atrBle1.sensorTileBoxPRO.dumpLog.BleSTBoxProDiscoveryForDumpLogActivity
import com.st.assetTracking.dashboard.model.Device
import com.st.assetTracking.management.AssetTrackingNavigationViewModel
import com.st.assetTracking.management.deviceData.DeviceHistoryData
import com.st.polaris.ble.connectivity.AstraConnectivityActivity
import com.st.nfcSmarTag.v1.NfcMainActivity
import com.st.nfcSmarTag.v2.NfcTag2MainActivity

class DeviceInfo : AppCompatActivity() {
    companion object {
        private lateinit var deviceInfo: Device
        lateinit var mNavigationViewModel: AssetTrackingNavigationViewModel

        fun startWithDevice(context: Context, device: Device, viewModel: AssetTrackingNavigationViewModel): Intent {
            deviceInfo = device
            mNavigationViewModel = viewModel
            return Intent(context, DeviceInfo::class.java)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId ==android.R.id.home) {
            super.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_info)

        title = "Device Info"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setUpUiInformation()

        /* Open Device Settings */
        findViewById<Button>(R.id.btn_device_info_settings).setOnClickListener {
            showConfigurationBaord(applicationContext, deviceInfo)
        }

        /* Open Device Data */
        findViewById<Button>(R.id.btn_device_info_data).setOnClickListener{
            val deviceDataRepository = mNavigationViewModel.deviceListRepository?.buildDeviceRepositoryFor(applicationContext, deviceInfo.id)
            if(deviceDataRepository!=null) {
                val intent = DeviceHistoryData.startWithDeviceDataRepository(context = applicationContext, deviceInfo, deviceDataRepository)
                startActivity(intent)
            }
        }
    }

    private fun setUpUiInformation(){
        setBoardImage(deviceInfo.type)
        findViewById<TextView>(R.id.tv_device_info_id).text = deviceInfo.id
        findViewById<TextView>(R.id.tv_device_info_name).text = deviceInfo.name
        if(deviceInfo.deviceProfile!=null) {
            findViewById<TextView>(R.id.tv_device_info_profile).text = deviceInfo.deviceProfile
        }else{
            findViewById<TextView>(R.id.tv_device_info_profile).text = "No Device Profile"
        }
        findViewById<TextView>(R.id.tv_device_info_last_activity).text = deviceInfo.lastActivity

        if(deviceInfo.lastTemperatureData != null){
            findViewById<TextView>(R.id.tv_device_info_temperature).text = deviceInfo.lastTemperatureData
        }
        if(deviceInfo.lastPressureData != null) {
            findViewById<TextView>(R.id.tv_device_info_pressure).text = deviceInfo.lastPressureData
        }

        if(deviceInfo.lastHumidityData != null) {
            findViewById<TextView>(R.id.tv_device_info_humidity).text = deviceInfo.lastHumidityData
        }

        /** TODO: Handle this and other types of buttons */
        if(deviceInfo.type == Device.Type.SENSORTILEBOXPRO) {
            val bleSettingsBtn = findViewById<Button>(R.id.btn_device_info_settings_ble)
            bleSettingsBtn.visibility = View.VISIBLE

            val nfcSettingsBtn = findViewById<Button>(R.id.btn_device_info_settings_nfc)
            nfcSettingsBtn.visibility = View.VISIBLE

            val localDataBtn = findViewById<Button>(R.id.btn_device_info_local_data)
            localDataBtn.visibility = View.VISIBLE

            val genericSettingsBtn = findViewById<Button>(R.id.btn_device_info_settings)
            genericSettingsBtn.visibility = View.GONE

            bleSettingsBtn.setOnClickListener {
                val intent = Intent(applicationContext, BleSTBoxProDiscoveryForDumpLogActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("pnpl", true)
                intent.putExtra("mac", deviceInfo.mac)
                intent.putExtra("id", deviceInfo.id)
                applicationContext?.startActivity(intent)
            }

            nfcSettingsBtn.setOnClickListener {
                val intent = Intent(applicationContext, NfcTag2MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("id", deviceInfo.id)
                intent.putExtra("showSamples", false)
                applicationContext?.startActivity(intent)
            }

            localDataBtn.setOnClickListener {
                val intent = Intent(applicationContext, BleSTBoxProDiscoveryForDumpLogActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("mac", deviceInfo.mac)
                intent.putExtra("id", deviceInfo.id)
                applicationContext?.startActivity(intent)
            }
        }
    }

    private fun showConfigurationBaord(context: Context?, device: Device) {
        when(device.type){
            Device.Type.ASTRA -> {
                val intent = Intent(context, AstraConnectivityActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("mac", device.mac)
                intent.putExtra("id", device.id)
                context?.startActivity(intent)
            }
            Device.Type.NFCTAG2 -> {
                val intent = Intent(context, NfcTag2MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("id", device.id)
                context?.startActivity(intent)
            }
            Device.Type.NFCTAG1 -> {
                val intent = Intent(context, NfcMainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("id", device.id)
                context?.startActivity(intent)
            }
            Device.Type.SENSORTILEBOX -> {
                val intent = Intent(context, AtrBleMainActivity::class.java)
                intent.putExtra("mac", device.mac)
                intent.putExtra("id", device.id)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context?.startActivity(intent)
            }
            Device.Type.SENSORTILEBOXPRO -> {
                val intent = Intent(context, NfcTag2MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("id", device.id)
                context?.startActivity(intent)
            }
            else -> {}
        }
    }

    private fun setBoardImage(type: Device.Type) {
        when(type){
            Device.Type.ASTRA -> {
                setBoardImageResized(R.drawable.real_board_astra)
                setupConnectivityButton()
            }
            Device.Type.NFCTAG2 -> {
                setBoardImageResized(R.drawable.board_smartag2)
            }
            Device.Type.NFCTAG1 -> {
                setBoardImageResized(R.drawable.board_smartag1)
            }
            Device.Type.SENSORTILEBOX -> {
                setBoardImageResized(R.drawable.real_board_sensortilebox)
            }
            Device.Type.SENSORTILEBOXPRO -> {
                setBoardImageResized(R.drawable.real_board_sensortilebox_pro)
            }
            else -> {}
        }
    }

    private fun setBoardImageResized(image: Int) {
        Glide
                .with(applicationContext)
                .load(image)
                .fitCenter()
                .into(findViewById(R.id.iv_device_info_image))
    }

    private fun setupConnectivityButton() {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        val connectivityButton: Button = findViewById(R.id.btn_device_info_settings)
        val drawable = ContextCompat.getDrawable(applicationContext, R.drawable.ic_multiconnectivity_daynight)
        drawable?.setTint(resources.getColor(R.color.cardview_light_background))
        connectivityButton.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        connectivityButton.text = "Connectivity"
    }
}