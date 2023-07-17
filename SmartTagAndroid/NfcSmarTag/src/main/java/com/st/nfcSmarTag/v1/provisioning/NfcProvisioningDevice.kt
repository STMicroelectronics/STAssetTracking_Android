package com.st.nfcSmarTag.v1.provisioning

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultRegistry
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.st.assetTracking.dashboard.model.Device
import com.st.assetTracking.dashboard.provisioning.ProvisioningDevice
import com.st.login.Configuration
import com.st.login.LoginManager
import com.st.login.loginprovider.LoginProviderFactory
import com.st.nfcSmarTag.v1.NfcTagViewModel
import com.st.nfcSmarTag.R

class NfcProvisioningDevice : AppCompatActivity() {

    private lateinit var nfcTagHolder: NfcTagViewModel
    private lateinit var tvInfoNFCid: TextView
    private lateinit var nfcAdapter: NfcAdapter

    private lateinit var nfcID: String

    private var errorToast: Toast? = null
    private lateinit var mainView: View

    private lateinit var ivBoard: ImageView

    private lateinit var mCheckDeviceID : ImageView
    private lateinit var nameEditText : EditText

    private lateinit var yourTextWatcher: TextWatcher

    private lateinit var mRegisterButton: MaterialButton
    private var validDeviceName = false

    private var showSearchTag: Boolean
        get() = findViewById<View>(R.id.provisioning_progressBar).visibility == View.VISIBLE
        set(value) {
            val visibility = if (value) View.VISIBLE else View.GONE
            findViewById<View>(R.id.provisioning_progressBar).visibility = visibility
            findViewById<View>(R.id.provisioning_progressMessage).visibility = visibility
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        // Hide title bar
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {}


        super.onCreate(savedInstanceState)

        val pm: PackageManager = applicationContext.packageManager
        if (!(pm.hasSystemFeature(PackageManager.FEATURE_NFC))) {
            onBackPressed()
            Toast.makeText(this, "NFC is not supported.", Toast.LENGTH_SHORT).show()
            onBackPressed()
        }else {

            nfcAdapter = NfcAdapter.getDefaultAdapter(this)

            setContentView(R.layout.activity_nfc_provisioning_device)
            mainView = findViewById(R.id.nfc_provisioning_root_view)

            tvInfoNFCid = findViewById(R.id.tv_nfc_id)

            ivBoard = findViewById(R.id.board_nfc_image)

            setBoardImageResized(ivBoard)

            errorToast = Toast(this)

            nfcTagHolder = NfcTagViewModel.create(this)
            initializeNfcObserver()

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                } else {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
            }

            nameEditText = findViewById(R.id.register_nfc_device_name)
            mRegisterButton = findViewById(R.id.btn_NFC_registration)
            mCheckDeviceID = findViewById(R.id.check_device_id)

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
                    validDeviceName = textInput.isNotEmpty() && !(textInput.contains(" "))
                    mRegisterButton.isEnabled = validDeviceName && nfcTagHolder.nfcTagId.value?.isNotEmpty() ?: false
                }
            }
            nameEditText.addTextChangedListener(yourTextWatcher)

            // Registration Button
            findViewById<Button>(R.id.btn_NFC_registration).setOnClickListener{
                provisioningDevice(this, this.activityResultRegistry)
                nfcTagHolder.setCompletedProvisioning()
            }

            if (intent?.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
                val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                if (tag != null) {
                    nfcTagHolder.nfcTagDiscovered(tag)
                }
            }

        }
    }

    private fun setBoardImageResized(ivBoard: ImageView) {
        Glide
            .with(applicationContext)
            .load(R.drawable.board_smartag1)
            .fitCenter()
            .into(ivBoard)
    }

    private fun provisioningDevice(nfcProvisioningDevice: NfcProvisioningDevice, activityResultRegistry: ActivityResultRegistry) {
        val loginManager = LoginManager(
            activityResultRegistry,
            nfcProvisioningDevice,
            applicationContext,
            LoginProviderFactory.LoginProviderType.COGNITO,
            Configuration.getInstance(applicationContext, com.st.login.R.raw.auth_config_cognito)
        )

        loginManager.getAtrAuthData { authData ->
            if (authData != null) {
                ProvisioningDevice.startActivityToProvisioningFrom(authData,
                    applicationContext,
                    nfcID,
                    nameEditText.text.toString().filter { !it.isWhitespace() },
                    Device.Type.NFCTAG1.toString(),
                    null,
                    null,
                    null)
            }
        }
    }

    private fun initializeNfcObserver() {
        nfcTagHolder.nfcTag.observe(this, Observer {
            if (it == null) {
                showSearchTag = true
                Snackbar.make(mainView, getString(R.string.main_tag_missing), Snackbar.LENGTH_SHORT).show()
            } else {
                showSearchTag = false
                Snackbar.make(mainView, R.string.main_tag_detected, Snackbar.LENGTH_SHORT).show()
            }
            nfcTagHolder.nfcTagId.observe(this, Observer { id ->
                if(id == null){
                    tvInfoNFCid.setText(R.string.settings_tagIdUnknown)
                }else {
                    nfcID = nfcTagHolder.nfcTagId.value.toString()
                    tvInfoNFCid.text = nfcTagHolder.nfcTagId.value
                    mCheckDeviceID.setImageResource(R.drawable.ic_check)
                    mRegisterButton.isEnabled = validDeviceName && nfcTagHolder.nfcTagId.value?.isNotEmpty() ?: false
                }
            })

        })
        nfcTagHolder.ioError.observe(this, Observer { errorMsg ->
            if (errorMsg != null)
                Log.e("SmartTag", "error: $errorMsg")
        })
    }

    override fun onResume() {
        super.onResume()

        if(!nfcAdapter.isEnabled){
            Snackbar.make(mainView, R.string.main_nfc_disabled,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.main_nfc_enable_button) {
                    startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                }
                .show()
            return
        }

        nfcAdapter.enableReaderMode(this,
            { foundTag -> nfcTagHolder.nfcTagDiscovered(foundTag) },
            NfcAdapter.FLAG_READER_NFC_V, Bundle())
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableReaderMode(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}