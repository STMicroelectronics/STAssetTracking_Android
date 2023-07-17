package com.st.demos.polaris.ExtConfig

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.InputFilter.LengthFilter
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.st.BlueSTSDK.Features.ExtConfiguration.CustomCommand
import com.st.BlueSTSDK.Features.ExtConfiguration.FeatureExtConfiguration
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.WifSettings
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.ActivityWithNode
import com.st.assetTracking.dashboard.AssetTrackingCertManager
import com.st.demos.R


class ExtConfigurationActivity : ActivityWithNode(), CustomCommandAdapter.OnItemClickListener {

    companion object {

        fun startWithNode(context: Context, node: Node): Intent {
            return getStartIntent(
                context,
                ExtConfigurationActivity::class.java,
                node,
                true
            )
        }
        val  REGISTER_DIALOG_TAG = ExtConfigurationActivity::class.java.name+".REGISTER_DIALOG_TAG"
    }

    private lateinit var viewModel: ExtConfigurationViewModel

    private lateinit var mTextReadCert: TextView
    private lateinit var mTextReadUid: TextView
    private lateinit var mTextReadVFw: TextView
    private lateinit var mTextReadVInfo: TextView
    private lateinit var mTextReadVHelp: TextView
    private lateinit var mTextReadVPowerStatus: TextView
    private lateinit var mTextChangePin: TextView
    private lateinit var mTextClearDB: TextView
    private lateinit var mTextDFU: TextView
    private lateinit var mTextPowerOff: TextView
    private lateinit var mTextSetCert: TextView
    private lateinit var mTextSetName: TextView
    private lateinit var mTextSetTime: TextView
    private lateinit var mTextSetDate: TextView
    private lateinit var mTextSetWiFi: TextView
    private lateinit var mTextSetSensors: TextView
    private lateinit var mTextCustomCommands: TextView
    private lateinit var mProgressLayoutBar: LinearLayout

    private lateinit var mTextCardInfo: TextView
    private lateinit var mTextCardSecurity: TextView
    private lateinit var mTextCardControl: TextView
    private lateinit var mTextCardSetting: TextView

    private lateinit var mCardCustomCommands: CardView
    private val adapterCustomCommand = CustomCommandAdapter(this)
    private lateinit var recyclerViewCustomCommand: RecyclerView
    private lateinit var  mTextCardCustom : TextView

    private lateinit var mLinearLayoutInfo: LinearLayout
    private lateinit var mLinearLayoutSecurity: LinearLayout
    private lateinit var mLinearLayoutControl: LinearLayout
    private lateinit var mLinearLayoutSetting: LinearLayout

    private var showMcuIdDialog = true;

    private var customCommand = listOf<CustomCommand>()

    private lateinit var view: View

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            super.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fragment_extended_settings)
        title = "Configuration"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        view = window.decorView.findViewById(android.R.id.content)

        // Get the ViewModel
        viewModel = ViewModelProvider(this).get(ExtConfigurationViewModel::class.java)
        viewModel.context = applicationContext

        //Find the ProgressBar
        mProgressLayoutBar = findViewById(R.id.ExSettProgress)

        mProgressLayoutBar.visibility = View.VISIBLE

        // find all the LinearLayout
        mLinearLayoutInfo = findViewById(R.id.ExtSettInfoLinearLayout)
        mLinearLayoutSecurity = findViewById(R.id.ExtSettSecurityLinearLayout)
        mLinearLayoutControl = findViewById(R.id.ExtSettControlLinearLayout)
        mLinearLayoutSetting = findViewById(R.id.ExtSettSettingLinearLayout)

        // Find all TextView
        mTextCardInfo = findViewById(R.id.ExtSettInfoText)
        mTextCardSecurity = findViewById(R.id.ExtSettSecurityText)
        mTextCardControl = findViewById(R.id.ExtSettControlText)
        mTextCardSetting = findViewById(R.id.ExtSettSettingText)

        mTextReadUid = findViewById(R.id.ExtSettReadUidText)
        mTextReadVFw = findViewById(R.id.ExtSettReadVFwText)
        mTextReadVInfo = findViewById(R.id.ExtSettReadVInfoText)
        mTextReadVHelp = findViewById(R.id.ExtSettReadVHelpText)
        mTextReadVPowerStatus = findViewById(R.id.ExtSettReadVPowerStatusText)

        mTextReadCert = findViewById(R.id.ExtSettReadCertText)
        mTextSetCert = findViewById(R.id.ExtSettSetCertText)
        mTextChangePin = findViewById(R.id.ExtSettChangePinText)
        mTextClearDB = findViewById(R.id.ExtSettClearDBText)

        mTextDFU = findViewById(R.id.ExtSettDFUText)
        mTextPowerOff = findViewById(R.id.ExtSettPowerOffText)

        mTextCustomCommands = findViewById(R.id.ExtSettCustomCommands)
        mTextSetName = findViewById(R.id.ExtSettSetNameText)
        mTextSetTime = findViewById(R.id.ExtSettSetTimeText)
        mTextSetDate = findViewById(R.id.ExtSettSetDateText)
        mTextSetWiFi = findViewById(R.id.ExtSettSetWiFiText)
        mTextSetSensors = findViewById(R.id.ExtSettSetSensorsText)


        //Set the Callbacks for setOnClick listener
        mTextCardInfo.setOnClickListener { controlCard(mLinearLayoutInfo) }
        mTextCardSecurity.setOnClickListener { controlCard(mLinearLayoutSecurity) }
        mTextCardControl.setOnClickListener { controlCard(mLinearLayoutControl) }
        mTextCardSetting.setOnClickListener { controlCard(mLinearLayoutSetting) }

        mTextReadUid.setOnClickListener { readUid() }
        mTextReadVFw.setOnClickListener { readvFw() }
        mTextReadVInfo.setOnClickListener { readInfo() }
        mTextReadVHelp.setOnClickListener { readHelp() }
        mTextReadVPowerStatus.setOnClickListener { readPowStatus() }

        mTextReadCert.setOnClickListener { readCert() }
        mTextChangePin.setOnClickListener { setPIN() }
        mTextClearDB.setOnClickListener { clearDB() }
        mTextSetCert.setOnClickListener { requestCert() }

        mTextDFU.setOnClickListener { setDFU() }
        mTextPowerOff.setOnClickListener { powerOff() }

        mTextSetName.setOnClickListener { setName() }
        mTextSetTime.setOnClickListener { setTime() }
        mTextSetDate.setOnClickListener { setDate() }
        mTextSetWiFi.setOnClickListener { setWifi() }
        mTextSetSensors.setOnClickListener{ readSensors()}
        mTextCustomCommands.setOnClickListener { readCustomCommands() }

        //Set the CustomCommands section
        mCardCustomCommands = findViewById(R.id.ExtSettCustomCommandsCard)
        mTextCardCustom     = findViewById(R.id.ExtSettCustomCommandsText)
        //Set the Recycler View
        recyclerViewCustomCommand = findViewById(R.id.ExtSettCustomCommandsRecycler)
        recyclerViewCustomCommand.adapter = adapterCustomCommand
        recyclerViewCustomCommand.layoutManager = LinearLayoutManager(applicationContext)
        //recyclerViewCustomCommand.setHasFixedSize(true)
        mTextCardCustom.setOnClickListener {
            if (recyclerViewCustomCommand.visibility == View.VISIBLE) {
                recyclerViewCustomCommand.visibility = View.GONE
            } else {
                recyclerViewCustomCommand.visibility = View.VISIBLE
            }}

        node?.let { n -> enableNeededNotification(n) }

        attachCommandCommandList()
        attachCommandPowerStatus()
        attachCommandHelp()
        attachCommandVersionFw()
        attachCommandUID()
        attachCommandCertificate()
        attachCommandInfo()
        attachCommandError()
        attachCommandCustomCommandList()
    }

    private fun controlCard(layout: LinearLayout) {
        if (layout.visibility == View.VISIBLE) {
            layout.visibility = View.GONE
        } else {
            layout.visibility = View.VISIBLE
        }
    }

    private fun attachCommandCommandList() {
        viewModel.commandlist_answer.observe(this, Observer { newString ->
            if (newString != null) {
                mTextReadUid.isEnabled = newString.contains(FeatureExtConfiguration.READ_UID)
                //mTextReadUid.isEnabled = false
                if (mTextReadUid.isEnabled == false) {
                    node?.let { viewModel.setMCUID(node!!.tag.replace(":", "")) }
                } else {
                    if (viewModel.getMCUID() == null) {
                        showMcuIdDialog = false
                        //Ask to the board the STM32 Unique ID
                        readUid();
                    }
                }

                viewModel.commandListReceived()

                mTextReadVFw.isEnabled = newString.contains(FeatureExtConfiguration.READ_VERSION_FW)
                mTextReadVInfo.isEnabled = newString.contains(FeatureExtConfiguration.READ_INFO)
                mTextReadVHelp.isEnabled = newString.contains(FeatureExtConfiguration.READ_HELP)
                mTextReadVPowerStatus.isEnabled = newString.contains(FeatureExtConfiguration.READ_POWER_STATUS)

                mTextChangePin.isEnabled = newString.contains(FeatureExtConfiguration.CHANGE_PIN)
                mTextClearDB.isEnabled = newString.contains(FeatureExtConfiguration.CLEAR_DB)
                mTextReadCert.isEnabled = newString.contains(FeatureExtConfiguration.READ_CERTIFICATE)
                mTextSetCert.isEnabled = newString.contains(FeatureExtConfiguration.SET_CERTIFICATE)

                mTextDFU.isEnabled = newString.contains(FeatureExtConfiguration.SET_DFU)
                mTextPowerOff.isEnabled = newString.contains(FeatureExtConfiguration.POWER_OFF)

                mTextSetName.isEnabled = newString.contains(FeatureExtConfiguration.SET_NAME)
                mTextSetTime.isEnabled = newString.contains(FeatureExtConfiguration.SET_TIME)
                mTextSetDate.isEnabled = newString.contains(FeatureExtConfiguration.SET_DATE)
                mTextSetWiFi.isEnabled = newString.contains(FeatureExtConfiguration.SET_WIFI)
                mTextSetSensors.isEnabled = newString.contains(FeatureExtConfiguration.READ_SENSORS)
                mTextCustomCommands.isEnabled = newString.contains(FeatureExtConfiguration.READ_CUSTOM_COMMANDS)

                //Enable each Card that contains at least a valid command
                if (!((mTextReadUid.isEnabled) || (mTextReadVFw.isEnabled) || (mTextReadVInfo.isEnabled) || (mTextReadVHelp.isEnabled) || (mTextReadVPowerStatus.isEnabled))) {
                    mTextCardInfo.isEnabled = false
                    mLinearLayoutInfo.visibility = View.GONE
                }

                if (!((mTextChangePin.isEnabled) || (mTextClearDB.isEnabled) || (mTextReadCert.isEnabled) || (mTextSetCert.isEnabled))) {
                    mTextCardSecurity.isEnabled = false
                    mLinearLayoutSecurity.visibility = View.GONE
                }

                if (!((mTextDFU.isEnabled) || (mTextPowerOff.isEnabled))) {
                    mTextCardControl.isEnabled = false
                    mLinearLayoutControl.visibility = View.GONE
                }

                if (!((mTextSetName.isEnabled) || (mTextSetTime.isEnabled) || (mTextSetDate.isEnabled) || (mTextSetWiFi.isEnabled) || (mTextCustomCommands.isEnabled) || (mTextSetSensors.isEnabled))) {
                    mTextCardSetting.isEnabled = false
                    mLinearLayoutSetting.visibility = View.GONE
                }

                val certificate = viewModel.getRetrivedCertiticate()
                certificate?.let { viewModel.setCert(certificate) }
                viewModel.setRetrivedCertificate(null)

                mProgressLayoutBar.visibility = View.GONE
            }
        })
    }

    private fun attachCommandCustomCommandList() {
        viewModel.customcommandlist_answer.observe(this, Observer { newList ->
            if (newList != null) {
                customCommand = newList
                mProgressLayoutBar.visibility = View.GONE
                adapterCustomCommand.updateCustomCommandList(newList)
            }
        })
    }

    private fun attachCommandHelp() {
        viewModel.help_answer.observe(this, Observer { newString ->
            if (newString != null) {
                mProgressLayoutBar.visibility = View.GONE
                this.let {
                    val builder = AlertDialog.Builder(it)
                    builder.apply {
                        setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                    }
                    builder.setTitle("Help")
                    builder.setMessage(newString)
                    builder.create()
                }.show()
                viewModel.helpReceived()
            }
        })
    }

    private fun attachCommandInfo() {
        viewModel.info_answer.observe(this, Observer { newString ->
            if (newString != null) {
                mProgressLayoutBar.visibility = View.GONE
                val alertDialog: AlertDialog = AlertDialog.Builder(this).create()
                alertDialog.setTitle("Info")
                alertDialog.setMessage(newString)
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
                viewModel.infoReceived()
            }
        })
    }

    private fun attachCommandError() {
        viewModel.error_answer.observe(this, Observer { newString ->
            if (newString != null) {
                val alertDialog: AlertDialog = AlertDialog.Builder(this).create()
                val titleText: String = "ERROR!"

                // Initialize a new foreground color span instance
                val foregroundColorSpan = ForegroundColorSpan(Color.RED)

                // Initialize a new spannable string builder instance
                val ssBuilder = SpannableStringBuilder(titleText)

                // Apply the text color span
                ssBuilder.setSpan(
                    foregroundColorSpan,
                    0,
                    titleText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                alertDialog.setTitle(ssBuilder)
                alertDialog.setMessage(newString)
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
                viewModel.errorReceived()
            }
        })
    }

    private fun attachCommandUID() {
        viewModel.uid_answer.observe(this, Observer { newString ->
            if (newString != null) {
                if (showMcuIdDialog) {
                    this.let {
                        val builder = AlertDialog.Builder(it)
                        builder.apply {
                            setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                        }
                        builder.setTitle("STM32 UID")
                        builder.setMessage(newString)
                        builder.create()
                    }.show()
                } else {
                    showMcuIdDialog = true
                }
                viewModel.setMCUID(newString)
                viewModel.uidReceived()
            }
        })
    }

    private fun attachCommandPowerStatus() {
        viewModel.powerstatus_answer.observe(this, Observer { newString ->
            if (newString != null) {
                val alertDialog: AlertDialog = AlertDialog.Builder(this).create()
                alertDialog.setTitle("Power Status")
                alertDialog.setMessage(newString)
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
                viewModel.powerstatusReceived()
            }
        })
    }

    private fun attachCommandVersionFw() {
        viewModel.versionfw_answer.observe(this, Observer { newString ->
            if (newString != null) {
                val alertDialog: AlertDialog = AlertDialog.Builder(this).create()
                alertDialog.setTitle("Version FW")
                alertDialog.setMessage(newString)

                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
                viewModel.versionfwReceived()
            }
        })
    }

    private fun attachCommandCertificate() {
        viewModel.certificate_answer.observe(this, Observer { newString ->
            if (newString != null) {
                mProgressLayoutBar.visibility = View.GONE
                viewModel.certificateReceived()
                if (applicationContext != null) {
                    val intent = Intent(applicationContext, AssetTrackingCertManager::class.java)
                    intent.putExtra(AssetTrackingCertManager.DEVICE_ID_EXTRA, viewModel.getMCUID())
                    intent.putExtra(AssetTrackingCertManager.DEVICE_TYPE_EXTRA, "wifi")
                    intent.putExtra(AssetTrackingCertManager.DEVICE_CERT_EXTRA, newString)
                    startActivityForResult(intent, AssetTrackingCertManager.REGISTER_CERTIFICATE_REQUEST_CODE)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.commandlist_answer.removeObservers(this)
        viewModel.certificate_answer.removeObservers(this)
        viewModel.versionfw_answer.removeObservers(this)
        viewModel.powerstatus_answer.removeObservers(this)
        viewModel.uid_answer.removeObservers(this)
        viewModel.info_answer.removeObservers(this)
        viewModel.error_answer.removeObservers(this)
        viewModel.help_answer.removeObservers(this)
        viewModel.customcommandlist_answer.removeObservers(this)

        disableNeedNotification(node!!)
    }

    private fun enableNeededNotification(node: Node) {

        //showIntroductionMessage("In this section it's possible to configure the Firmware running on the board", context);

        viewModel.enableNotification(node)
    }

    private fun disableNeedNotification(node: Node) {
        viewModel.disableNotification(node)
    }

    private fun readPowStatus() {
        viewModel.readPowStatus()
    }

    private fun readHelp() {
        mProgressLayoutBar.visibility = View.VISIBLE
        viewModel.readHelp()
    }

    private fun readvFw() {
        viewModel.readvFw()
    }

    private fun readUid() {
        viewModel.readUid()
    }

    private fun readCert() {
        mProgressLayoutBar.visibility = View.VISIBLE
        viewModel.readCert()
    }

    private fun readInfo() {
        mProgressLayoutBar.visibility = View.VISIBLE
        viewModel.readInfo()
    }

    private fun readCustomCommands() {
        mCardCustomCommands.visibility = View.VISIBLE
        mProgressLayoutBar.visibility = View.VISIBLE
        viewModel.readCustomCommands()
    }

    private fun readSensors() {
        val sensorConfig = SensorConfigDialogFragment(node)
        sensorConfig.show(supportFragmentManager, REGISTER_DIALOG_TAG)
    }

    private fun setWifi() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)

        // Get the layout inflater
        val inflater = this.layoutInflater
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        val v = inflater.inflate(R.layout.wifi_credentials, null)

        val securityTypeList = listOf("OPEN", "WEP", "WPA", "WPA2", "WPA/WPA2")
        val securitySpinner = v.findViewById<Spinner>(R.id.wifi_security)

        val dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, securityTypeList).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val ssidTextView = v.findViewById<TextView>(R.id.wifi_ssid)
        val passwdTextView = v.findViewById<TextView>(R.id.wifi_password)
        var adapterPosition = 0;
        securitySpinner.adapter = dataAdapter

        securitySpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                adapterPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                adapterPosition = 0
            }
        }


        builder.setView(v)

        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }

        builder.setPositiveButton("Send to Node") { dialog: DialogInterface, _: Int ->
            val wifiConf = WifSettings(enable = true, ssid = ssidTextView.text.toString(), password = passwdTextView.text.toString(), securityType = dataAdapter.getItem(adapterPosition).toString())
            viewModel.setWiFi(wifiConf)
            dialog.dismiss()
            snackBarWithConfirmation("Wi-Fi Credential Sent to Board")

        }
        builder.create()
        builder.show()
    }

    private fun setName() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Set the Board Name")

        val customLayout = layoutInflater.inflate(R.layout.set_input_extconfig, null);
        builder.setView(customLayout);
        val editText: EditText = customLayout.findViewById(R.id.extconfig_text_input)
        val inputTex: TextInputLayout = customLayout.findViewById(R.id.extconfig_text_input_layout)

        //Set the Default Name
        editText.hint = node?.name

        // Specify the type of input expected;
        editText.inputType = InputType.TYPE_CLASS_TEXT
        inputTex.counterMaxLength = 7
        editText.filters = arrayOf<InputFilter>(LengthFilter(7))

        builder.setPositiveButton("OK") { _: DialogInterface, _: Int ->
            run {
                viewModel.setName(editText.text.toString())
                snackBarWithConfirmation("The Board will change the name after the disconnection")
            }
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }

        builder.show()
    }

    private fun requestCert() {
        /*
        val availableCloudDashboards = arrayOf("Asset Tracking", "Predictive Maintenance")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose Cloud Dashboard")
            .setItems(
                availableCloudDashboards
            ) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(requireContext(), AssetTrackingCertManager::class.java)
                        intent.putExtra(AssetTrackingCertManager.DEVICE_ID_EXTRA, viewModel.getMCUID())
                        intent.putExtra(AssetTrackingCertManager.DEVICE_TYPE_EXTRA, "ble")
                        startActivityForResult(intent, AssetTrackingCertManager.REQUEST_CERTIFICATE_REQUEST_CODE)
                    }
                    1 -> {
                        val optArgBundle = PredictiveMaintenanceDashboardActivity.getOptArgBundle(
                            viewModel.getMCUID()!!,
                            mTextSetWiFi.isEnabled
                        )

                        val intent = PredictiveMaintenanceDashboardActivity.getStartIntent(
                            requireContext(),
                            node!!,
                            optArgBundle
                        )
                        startActivity(intent)
                    }
                }
            }
            .setNegativeButton(R.string.cancel
            ) { _, _ ->
                // User cancelled the dialog
            }
        builder.create().show()
    */
    }

    private fun snackBarWithConfirmation(message: String) {
        view.let {
            val snack = Snackbar.make(it,message,Snackbar.LENGTH_LONG)
            snack.setAction("OK") { snack.dismiss() }
            snack.show()
        }
    }


    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (
                requestCode == AssetTrackingCertManager.REQUEST_CERTIFICATE_REQUEST_CODE
            ) {
                if (resultCode == Activity.RESULT_OK) {
                    val retrived_certificate = data.getStringExtra("return_cert")
                    //Log.i("AWS Certificate", "Certificate to Send=$retrived_certificate")
                    snackBarWithConfirmation("Certificated received")
                    viewModel.setRetrivedCertificate(retrived_certificate)
                    Log.d("CERTIFICATE", retrived_certificate)
                } else {
                    //Ignore
                }
            }
            else if (requestCode == AssetTrackingCertManager.REGISTER_CERTIFICATE_REQUEST_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    snackBarWithConfirmation("Certificated registered")
                } else {
                    //Ignore
                }
            }
        }
    }*/

    private fun setPIN() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Change the PIN")

        val customLayout = layoutInflater.inflate(R.layout.set_input_extconfig, null);
        builder.setView(customLayout);
        val editText: EditText = customLayout.findViewById(R.id.extconfig_text_input)
        val inputTex: TextInputLayout = customLayout.findViewById(R.id.extconfig_text_input_layout)

        //Set the Default Name
        editText.hint = 123456.toString()

        // Specify the type of input expected;
        editText.inputType = InputType.TYPE_CLASS_NUMBER
        inputTex.counterMaxLength = 6
        editText.filters = arrayOf<InputFilter>(LengthFilter(6))

        // Set up the buttons
        builder.setPositiveButton("OK") { _: DialogInterface, _: Int ->
            run {
                viewModel.setPIN(editText.text.toString().toInt())
                snackBarWithConfirmation("The Board will use the new PIN")
            }}
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }

        builder.show()
    }

    private fun clearDB() {
        viewModel.clearDB()
        snackBarWithConfirmation("Board Security Database Cleared")
    }

    private fun setDFU() {
        viewModel.setDFU()
        snackBarWithConfirmation("The Board will reboot on DFU Mode")
    }

    private fun powerOff() {
        viewModel.powerOff()
        snackBarWithConfirmation("The Board will be switched off")
    }

    private fun setTime() {
        viewModel.setTime()
        snackBarWithConfirmation("Board Time synchronized")
    }

    private fun setDate() {
        viewModel.setDate()
        snackBarWithConfirmation("Board Date synchronized")
    }

    /* for the Custom Command Adapter */
    override fun onItemClick(position: Int) {
        when (customCommand[position].type) {
            "String" -> {
                customCommandDialogString(position)
            }
            "Integer" -> {
                customCommandDialogInteger(position)
            }
            "Boolean" -> {
                customCommandDialogBoolean(position)
            }
            "Void" -> {
                //Send Directly the Command without creating the Dialog
                viewModel.sendCustomCommandVoid(customCommand[position].name)
            }
            "EnumInteger" -> {
                customCommandDialogEnumInteger(position)
            }
            "EnumString" -> {
                customCommandDialogEnumString(position)
            }
        }
    }

    private fun customCommandDialogInteger(position: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)

        if(customCommand[position].description!=null) {
            builder.setTitle(customCommand[position].description)
        } else {
            builder.setTitle(customCommand[position].name)
        }

        val customLayout = layoutInflater.inflate(R.layout.custom_command_validation_entry, null);
        builder.setView(customLayout);
        val inputTex: TextInputLayout = customLayout.findViewById(R.id.custom_command_number)
        inputTex.visibility=View.VISIBLE
        val editText = inputTex.editText
        val helpText: TextView = customLayout.findViewById(R.id.custom_command_help)

        // Retrieve Max and Minimum allowed value
        val min = customCommand[position].min
        val max = customCommand[position].max

        val helpString = "$min <= Valid Value <= $max"
        helpText.text = helpString

        val buttonPositive: Button = customLayout.findViewById(R.id.custom_command_positive)
        val buttonNegative: Button = customLayout.findViewById(R.id.custom_command_negative)

        // Check the input
        editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Check if it's empty
                val textInput: String = editText.text.toString()
                if (textInput.isEmpty()) {
                    editText.error = "Field can't be empty";
                } else {
                    editText.error = null
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val textInput: String = editText.text.toString()
                if (textInput.isEmpty()) {
                    editText.error = "Field can't be empty";
                    buttonPositive.isEnabled = false
                    buttonPositive.visibility = View.INVISIBLE
                } else {
                    if ((textInput[0] != '-') || (textInput.length > 1)) {
                        when {
                            Integer.parseInt(textInput) < min -> {
                                editText.error = "Field must be > $min";
                                buttonPositive.isEnabled = false
                                buttonPositive.visibility = View.INVISIBLE
                            }
                            Integer.parseInt(textInput) > max -> {
                                editText.error = "Field must be < $max ";
                                buttonPositive.isEnabled = false
                                buttonPositive.visibility = View.INVISIBLE
                            }
                            else -> {
                                editText.error = null
                                buttonPositive.isEnabled = true
                                buttonPositive.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        val dialog = builder.create();
        dialog.show();

        buttonNegative.setOnClickListener {
            dialog.dismiss()
        }

        buttonPositive.setOnClickListener {
            if(editText!=null) {
                viewModel.sendCustomCommandInteger(customCommand[position].name, Integer.parseInt(editText.text.toString()))
                dialog.dismiss()
            }
        }
    }

    private fun customCommandDialogEnumInteger(position: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)

        if(customCommand[position].description!=null) {
            builder.setTitle(customCommand[position].description)
        } else {
            builder.setTitle(customCommand[position].name)
        }

        val customLayout = layoutInflater.inflate(R.layout.custom_command_validation_entry, null);
        builder.setView(customLayout);
        val spinner: Spinner = customLayout.findViewById(R.id.custom_command_enum)

        val dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, customCommand[position].integerValues).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        var adapterPosition = 0
        spinner.visibility=View.VISIBLE
        spinner.adapter = dataAdapter

        val buttonPositive: Button = customLayout.findViewById(R.id.custom_command_positive)
        val buttonNegative: Button = customLayout.findViewById(R.id.custom_command_negative)

        buttonPositive.isEnabled = true
        buttonPositive.visibility = View.VISIBLE

        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                adapterPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                adapterPosition = 0
            }
        }

        val dialog = builder.create();
        dialog.show();

        buttonNegative.setOnClickListener {
            dialog.dismiss()
        }

        buttonPositive.setOnClickListener {
            viewModel.sendCustomCommandInteger(customCommand[position].name, customCommand[position].integerValues[adapterPosition])
            dialog.dismiss()
        }
    }

    private fun customCommandDialogEnumString(position: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)

        if(customCommand[position].description!=null) {
            builder.setTitle(customCommand[position].description)
        } else {
            builder.setTitle(customCommand[position].name)
        }

        val customLayout = layoutInflater.inflate(R.layout.custom_command_validation_entry, null);
        builder.setView(customLayout);
        val spinner: Spinner = customLayout.findViewById(R.id.custom_command_enum)
        val dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, customCommand[position].stringValues).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        var adapterPosition = 0
        spinner.visibility=View.VISIBLE
        spinner.adapter = dataAdapter

        val buttonPositive: Button = customLayout.findViewById(R.id.custom_command_positive)
        val buttonNegative: Button = customLayout.findViewById(R.id.custom_command_negative)

        buttonPositive.isEnabled = true
        buttonPositive.visibility = View.VISIBLE

        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                adapterPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                adapterPosition = 0
            }
        }

        val dialog = builder.create();
        dialog.show();

        buttonNegative.setOnClickListener {
            dialog.dismiss()
        }

        buttonPositive.setOnClickListener {
            viewModel.sendCustomCommandString(customCommand[position].name, customCommand[position].stringValues[adapterPosition].toString())
            dialog.dismiss()
        }
    }

    private fun customCommandDialogString(position: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)

        if(customCommand[position].description!=null) {
            builder.setTitle(customCommand[position].description)
        } else {
            builder.setTitle(customCommand[position].name)
        }

        val customLayout = layoutInflater.inflate(R.layout.custom_command_validation_entry, null);
        builder.setView(customLayout);

        val inputText: TextInputLayout = customLayout.findViewById(R.id.custom_command_string)
        inputText.visibility=View.VISIBLE
        val helpText: TextView = customLayout.findViewById(R.id.custom_command_help)
        val editText = inputText.editText

        // Retrieve Max and Minimum allowed value
        val min = customCommand[position].min

        val max = customCommand[position].max

        inputText.counterMaxLength = max

        val helpString = "$min chars <= Valid Entry <= $max chars"
        helpText.text = helpString

        val buttonPositive: Button = customLayout.findViewById(R.id.custom_command_positive)
        val buttonNegative: Button = customLayout.findViewById(R.id.custom_command_negative)

        //if the custom command allows a command with a string of 0 char dimension
        if(min==0) {
            buttonPositive.isEnabled= true
            buttonPositive.visibility= View.VISIBLE
        }

        // Check the input
        editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Check if it's empty
                val textInput: String = editText.text.toString().trim()
                if (textInput.isEmpty() && (min != 0)) {
                    editText.error = "Field can't be empty";
                } else {
                    editText.error = null
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val textInput: String = editText.text.toString().trim()
                if (textInput.isEmpty() && (min != 0)) {
                    editText.error = "Field can't be empty";
                    buttonPositive.isEnabled = false
                    buttonPositive.visibility = View.INVISIBLE
                } else if (textInput.length < min) {
                    buttonPositive.isEnabled = false
                    buttonPositive.visibility = View.INVISIBLE
                    editText.error = "Field must be at least $min chars";
                } else if (textInput.length > max) {
                    editText.error = "Field must be less than $max chars";
                    buttonPositive.isEnabled = false
                    buttonPositive.visibility = View.INVISIBLE
                } else {
                    editText.error = null
                    buttonPositive.isEnabled = true
                    buttonPositive.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        val dialog = builder.create();
        dialog.show();

        buttonNegative.setOnClickListener {
            dialog.dismiss()
        }

        buttonPositive.setOnClickListener {
            viewModel.sendCustomCommandString(customCommand[position].name, editText?.text.toString())
            dialog.dismiss()
        }
    }

    private fun customCommandDialogBoolean(position: Int) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)

        if(customCommand[position].description!=null) {
            builder.setTitle(customCommand[position].description)
        } else {
            builder.setTitle(customCommand[position].name)
        }

        val customLayout = layoutInflater.inflate(R.layout.custom_command_validation_entry, null);
        builder.setView(customLayout);
        val inputSwitch: SwitchCompat = customLayout.findViewById(R.id.custom_command_boolean)
        inputSwitch.visibility = View.VISIBLE

        val buttonPositive: Button = customLayout.findViewById(R.id.custom_command_positive)
        val buttonNegative: Button = customLayout.findViewById(R.id.custom_command_negative)

        buttonPositive.isEnabled=true;
        buttonPositive.visibility= View.VISIBLE

        val dialog = builder.create();
        dialog.show();

        buttonNegative.setOnClickListener {
            dialog.dismiss()
        }

        buttonPositive.setOnClickListener {
            viewModel.sendCustomCommandString(customCommand[position].name, inputSwitch.isChecked.toString())
            dialog.dismiss()
        }
    }

    /*companion object{
        val  REGISTER_DIALOG_TAG = ExtConfigurationActivity::class.java.name+".REGISTER_DIALOG_TAG"
    }*/
}