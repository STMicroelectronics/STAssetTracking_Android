package com.st.polaris.ble.provisioning

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.st.BlueSTSDK.Features.ExtConfiguration.CustomCommand
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.ActivityWithNode
import com.st.assetTracking.dashboard.communication.aws.AwsAssetTrackingService
import com.st.assetTracking.dashboard.model.Device
import com.st.assetTracking.dashboard.model.DeviceProfile
import com.st.assetTracking.dashboard.persistance.DeviceListRepository
import com.st.assetTracking.dashboard.provisioning.ProvisioningDevice
import com.st.login.*
import com.st.login.loginprovider.CognitoLoginProvider
import com.st.login.loginprovider.LoginProviderFactory
import com.st.polaris.R
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.resume


class BlePolarisProvisioningDevice : ActivityWithNode() {

    companion object {
        fun startWithNode(context: Context, node: Node): Intent {
            return ActivityWithNode.getStartIntent(
                context,
                BlePolarisProvisioningDevice::class.java,
                node,
                true
            )
        }
    }

    internal suspend fun Node.waitStatus(status: Node.State) {
        if (state == status)
            return
        //else wait
        suspendCancellableCoroutine<Unit> { cancellableContinuation ->
            val nodeListener = WaitStateListener(status, cancellableContinuation)
            addNodeStateListener(nodeListener)
            cancellableContinuation.invokeOnCancellation {
                removeNodeStateListener(nodeListener)
            }
        }
    }

    private var log: String = ""

    private lateinit var mainView: View
    private lateinit var ivBoard: ImageView
    private lateinit var pbRetrieveInformation: ProgressBar
    private lateinit var mCheckDeviceID : ImageView
    private lateinit var nameEditText : EditText
    private lateinit var nameInput : TextInputLayout
    private lateinit var mRegisterButton : MaterialButton
    private lateinit var mShowLog : MaterialButton

    private lateinit var yourTextWatcher: TextWatcher

    //Polaris
    private lateinit var mTextUid: TextView
    //private lateinit var mTextDevEui: TextView

    private lateinit var viewModel: ExtConfigurationViewModel
    private var customCommand = listOf<CustomCommand>()

    //Polaris Lora
    private lateinit var authenticationData: AuthData
    private lateinit var deviceListRepository: DeviceListRepository
    private var spinnerArray: MutableList<String> = ArrayList()
    private lateinit var spDeviceProfiles: Spinner
    //private lateinit var mTextDebugLoraDP: TextView

    //Device Profile Informations
    private val _devEuiAnswered = MutableLiveData<Boolean>(false)
    val devEuiAnswered: LiveData<Boolean>
        get() = _devEuiAnswered

    private lateinit var devEui: String

    private var deviceProfilesList: ArrayList<DeviceProfile> = ArrayList()

    override fun onDestroy() {
        viewModel.disableNotification(node!!)
        super.onDestroy()
    }

    override fun onBackPressed() {
        viewModel.disableNotification(node!!)
        setResult(1, Intent())
        super.onBackPressed()
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        // Hide title bar
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {}

        val node = ActivityWithNode.getNodeFromIntent(intent)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_polaris_provisioning_device)

        /*setup graphics elements*/
        setupLayout()

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

        val loginManager = LoginManager(
            this@BlePolarisProvisioningDevice.activityResultRegistry,
            this@BlePolarisProvisioningDevice,
            applicationContext,
            LoginProviderFactory.LoginProviderType.COGNITO,
            Configuration.getInstance(applicationContext, com.st.login.R.raw.auth_config_cognito)
        )

        loginManager.getAtrAuthData { authData ->
            if (authData != null) {
                authenticationData = authData
                CoroutineScope(Dispatchers.Main).launch {
                    node.waitStatus(Node.State.Connected)
                    showProgressBar()
                    retrieveBoardInformations(node)
                }
            }
        }

        // Get the ViewModel
        viewModel = ViewModelProvider(this).get(ExtConfigurationViewModel::class.java)
        viewModel.context = applicationContext
        attachCommandCommandList()


        // Registration Button
        mRegisterButton.setOnClickListener{
            provisioningDevice(this, this.activityResultRegistry, node)
            setupCompleteRegistrationLayout()
        }

        // Show Log Button
        mShowLog.setOnClickListener{
            findViewById<TextView>(R.id.tvLog).text = "LOG:\n\n$log\n\n"
        }
    }

    private fun setupLayout(){
        mainView = findViewById(R.id.ble_provisioning_root_view)
        ivBoard = findViewById(R.id.board_ble_image)
        nameEditText = findViewById(R.id.register_ble_device_name)
        nameInput = findViewById(R.id.registerDevice_deviceNameLayout)
        mRegisterButton = findViewById(R.id.btn_BLE_registration)
        mShowLog = findViewById(R.id.btn_show_log)

        mCheckDeviceID = findViewById(R.id.check_device_id)
        pbRetrieveInformation = findViewById(R.id.pbRetrievePolarisInformation)

        mTextUid = findViewById(R.id.tv_polaris_id)

        spDeviceProfiles = findViewById(R.id.spDeviceProfiles)

        setBoardImageResized(ivBoard)
    }
    private fun showProgressBar(){
        mCheckDeviceID.visibility = View.GONE
        pbRetrieveInformation.visibility = View.VISIBLE
    }
    private fun hideProgressBar(){
        mCheckDeviceID.visibility = View.VISIBLE
        pbRetrieveInformation.visibility = View.GONE

        nameInput.isEnabled = true
    }

    /** 0. Base function used to search specific custom command */
    private fun searchAndSendCommand(command: String, argument: String?){
        customCommand.forEach{ c ->
                if(c.name == command){
                    if (argument!=null){
                        viewModel.sendCustomCommandString(c.name, argument)
                    } else {
                        viewModel.sendCustomCommandVoid(c.name)
                    }
                }
        }
    }

    private fun sendLoRaConfig(booleanValue: String){
        customCommand.forEach{ c ->
            if(c.name == "LoRa config"){
                viewModel.sendCustomCommandString(c.name, booleanValue)
            }
        }
    }

    private fun setupCompleteRegistrationLayout(){
        sendLoRaConfig("false")

        spDeviceProfiles.isEnabled = false

        val appEui = deviceProfilesList[spDeviceProfiles.selectedItemPosition].context.application_eui
        val appKey = deviceProfilesList[spDeviceProfiles.selectedItemPosition].context.application_key

        log += "SEND LoRa config -> false\n"
        log += "SEND AppEui -> $appEui \nSEND AppKey -> $appKey\nSEND NtwKey -> $appKey\n"

        if(appEui != null && appKey != null) {
            searchAndSendCommand("SetLoRaAppEui", appEui)
            searchAndSendCommand("SetLoRaNwkKey", appKey)
            searchAndSendCommand("SetLoRaAppKey", appKey)
            viewModel.setCompletedProvisioning()
            mShowLog.visibility = View.VISIBLE

            sendLoRaConfig("true")
            log += "SEND LoRa config -> true\n"
        }
    }

    /**
     * - Setup MAC ADDRESS information
     * - Enable notifications
     * - Read UID from Custom Command
     * - Retrieve Device Profile
     * - Set AppEui, AppKey, NtwKey
     */
    private fun retrieveBoardInformations(node: Node) {
        log = "MAC Address: ${node.advertiseInfo.address} \n"

        viewModel.enableNotification(node)

        viewModel.readUid()
        attachCommandUID()

        viewModel.readCustomCommands()
        attachCommandCustomCommandList()

        attachCommandInfo()

        retrieveDeviceProfileInformations()

        devEuiAnswered.observe(this, Observer { answered ->
            if(answered){
                /* Send Custom Command - Set LoRa parameters */
                mCheckDeviceID.setImageResource(R.drawable.ic_check)
                hideProgressBar()
            }
        })
    }

    private fun retrieveDeviceProfileInformations(){
        val deviceListRemote = AwsAssetTrackingService(authenticationData, applicationContext)
        deviceListRepository = DeviceListRepository(
            authenticationData,
            deviceListRemote
        )

        CoroutineScope(Dispatchers.Main).launch {
            viewModel.getDeviceProfiles(deviceListRepository)
        }

        /**
         * Spinner Device Profile
         */
        viewModel.deviceProfiles.observe(this, Observer { dP ->
            dP.forEach{
                spinnerArray.add(it.id)
                deviceProfilesList.add(it)
            }

            val spinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerArray)
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spDeviceProfiles.adapter = spinnerArrayAdapter

        })
    }

    private fun setBoardImageResized(ivBoard: ImageView) {
        Glide
            .with(applicationContext)
            .load(R.drawable.real_board_astra)
            .fitCenter()
            .into(ivBoard)
    }



    /**
     * Command List Response
     */
    private fun attachCommandCommandList() {
        viewModel.commandlist_answer.observe(this, Observer { newString ->
            if (newString != null) {
                viewModel.commandListReceived()
            }
        })
    }

    /**
     * Board MCUID Response
     */
    private fun attachCommandUID() {
        viewModel.uid_answer.observe(this, Observer { newString ->
            if (newString != null) {
                viewModel.setMCUID(newString)
                viewModel.uidReceived()
                mTextUid.text = newString
                log += "MCUID: $newString \n"
            }
        })
    }

    var customCommandStep = 0
    /**
     * Board Firmware Response
     */
    private fun attachCommandCustomCommandList() {
        viewModel.customcommandlist_answer.observe(this, Observer { newList ->
            if (newList != null) {
                if(customCommandStep==0){
                    customCommand = newList
                    println("CUSTOM COMMAND: $customCommand")
                    customCommandStep = 1
                    searchAndSendCommand("LoRa conf", null)
                }else {
                    customCommand = newList
                    println("CUSTOM COMMAND: $customCommand")
                    searchAndSendCommand("GetStm32wlKeys", null)
                }
            }
        })
    }

    var i = 0
    private fun attachCommandInfo() {

        viewModel.info_answer.observe(this, Observer { newString ->
            if (newString != null) {
                if(i==0) {
                    viewModel.infoReceived()

                    val splittedResponse = newString.split("\n")
                    val splittedDevEui = splittedResponse[1].split(":")
                    val eui = splittedDevEui[1]

                    log += "DevEUI: $eui \n"

                    devEui = eui
                    i += 1
                    _devEuiAnswered.postValue(true)
                }
            }
        })
    }

    /**
     * BOARD PROVISIONING ---> ATR Cloud DSH
     */
    private fun provisioningDevice(
        blePolarisProvisioningDevice: BlePolarisProvisioningDevice,
        activityResultRegistry: ActivityResultRegistry,
        node: Node
    ) {

        val uid = mTextUid.text.toString().trim().toLowerCase(Locale.ROOT)
        //val mac = mTextBleMac.text.toString().trim()
        //val devEui = mTextDevEui.text.toString().trim()
        val devProfile = spDeviceProfiles.selectedItem.toString()

        val loginManager = LoginManager(
            this@BlePolarisProvisioningDevice.activityResultRegistry,
            this@BlePolarisProvisioningDevice,
            applicationContext,
            LoginProviderFactory.LoginProviderType.COGNITO,
            Configuration.getInstance(applicationContext, com.st.login.R.raw.auth_config_cognito)
        )

        loginManager.getAtrAuthData { authData ->
            if (authData != null) {
                authenticationData = authData
                ProvisioningDevice.startActivityToProvisioningFrom(
                    authData,
                    applicationContext,
                    uid,
                    nameEditText.text.toString().filter { !it.isWhitespace() },
                    Device.Type.ASTRA.toString(),
                    node.advertiseInfo.address,
                    devEui,
                    devProfile
                )
            }
        }
    }

    private fun sendBackToMainMenu(){
        setResult(1, Intent())
        onBackPressed()
    }

}

private class WaitStateListener(private val finalState: Node.State,
                                private val continuation: CancellableContinuation<Unit>
) :
    Node.NodeStateListener {
    override fun onStateChange(node: Node, newState: Node.State, prevState: Node.State) {
        if (newState == finalState) {
            node.removeNodeStateListener(this)
            continuation.resume(Unit)
        }
    }

}