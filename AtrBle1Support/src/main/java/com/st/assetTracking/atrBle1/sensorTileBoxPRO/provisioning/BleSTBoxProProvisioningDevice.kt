package com.st.assetTracking.atrBle1.sensorTileBoxPRO.provisioning

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.ActivityWithNode
import com.st.assetTracking.atrBle1.R
import com.st.assetTracking.atrBle1.sensorTileBox.SettingsViewModel
import com.st.assetTracking.atrBle1.sensorTileBox.settings.LogSettingsRepository
import com.st.assetTracking.dashboard.model.Device
import com.st.assetTracking.dashboard.provisioning.ProvisioningDevice
import com.st.login.Configuration
import com.st.login.LoginManager
import com.st.login.loginprovider.LoginProviderFactory

class BleSTBoxProProvisioningDevice : ActivityWithNode() {

    companion object {
        fun startWithNode(context: Context, node: Node): Intent {
            return getStartIntent(context, BleSTBoxProProvisioningDevice::class.java, node, false)
        }
    }

    private lateinit var idBleDevice: String

    private lateinit var mainView: View
    private lateinit var ivBoard: ImageView
    private lateinit var tvInfoBLEid: TextView
    private lateinit var mCheckDeviceID : ImageView
    private lateinit var nameEditText : EditText
    private lateinit var mRegisterButton : MaterialButton

    private lateinit var yourTextWatcher: TextWatcher

    override fun onCreate(savedInstanceState: Bundle?) {

        // Hide title bar
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {}

        val node = getNodeFromIntent(intent)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_provisioning_device)

        mainView = findViewById(R.id.ble_provisioning_root_view)
        ivBoard = findViewById(R.id.board_ble_image)
        tvInfoBLEid = findViewById(R.id.tv_ble_id)
        nameEditText = findViewById(R.id.register_ble_device_name)
        mRegisterButton = findViewById(R.id.btn_BLE_registration)
        mCheckDeviceID = findViewById(R.id.check_device_id)

        setBoardImageResized(ivBoard)

        findViewById<TextView>(R.id.ble_provisioning_title).text = "Add SensorTile.Box PRO"

        val mViewModel by viewModels<SettingsViewModel> {
            SettingsViewModel.Factory(node, LogSettingsRepository(node = node))
        }

        /**
         * Check if SensorTile.Box ha ATR-BLE-1 fw
         */
        /*mViewModel.atrFirmware.observe(this, Observer { atrFw ->
            if (!atrFw) {
                Toast.makeText(applicationContext, "Incorrect firmware", Toast.LENGTH_SHORT).show()
                this@BleSTBoxProProvisioningDevice.onBackPressed()
            }
        })
         */

        /**
         * Setup UID information
         */
        mViewModel.uid.observe(this, Observer { uid ->
            idBleDevice = uid
            tvInfoBLEid.text = uid
            mCheckDeviceID.setImageResource(R.drawable.ic_check)
        })

        // Back Arrow ImageButton
        findViewById<ImageButton>(R.id.back_arrow_button).setOnClickListener{
            onBackPressed()
        }

        // Name EditText
        yourTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val textInput: String = nameEditText.text.toString().filter { !it.isWhitespace() }
                mRegisterButton.isEnabled = textInput.isNotEmpty() && !(textInput.contains(" "))
            }
        }
        nameEditText.addTextChangedListener(yourTextWatcher)

        // Registration Button
        mRegisterButton.setOnClickListener{
            provisioningDevice(this, this.activityResultRegistry, node)
            mViewModel.setCompletedProvisioning()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun setBoardImageResized(ivBoard: ImageView) {
        Glide
            .with(applicationContext)
            .load(R.drawable.real_board_sensortilebox_pro)
            .fitCenter()
            .into(ivBoard)
    }

    private fun provisioningDevice(
        bleSTBoxPROProvisioningDevice: BleSTBoxProProvisioningDevice,
        activityResultRegistry: ActivityResultRegistry,
        node: Node
    ) {
        val loginManager = LoginManager(
            activityResultRegistry,
            bleSTBoxPROProvisioningDevice,
            applicationContext,
            LoginProviderFactory.LoginProviderType.COGNITO,
            Configuration.getInstance(applicationContext, com.st.login.R.raw.auth_config_cognito)
        )

        loginManager.getAtrAuthData { authData ->
            if (authData != null) {
                ProvisioningDevice.startActivityToProvisioningFrom(authData, applicationContext, idBleDevice, nameEditText.text.toString().filter { !it.isWhitespace() }, Device.Type.SENSORTILEBOXPRO.toString(), node.device.address, null, null, 3, 1)
            }
        }
    }
}