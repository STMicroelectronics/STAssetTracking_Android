package com.st.polaris.ble.provisioning

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AlertDialog.Builder
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.snackbar.Snackbar
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.NodeListActivity
import com.st.polaris.R
import com.st.nfcSmarTag.v2.NfcTag2ViewModel
import com.st.smartaglibrary.v2.SmarTag2Service
import com.st.utility.databases.associatedBoard.AssociatedBoard
import com.st.utility.databases.associatedBoard.ReadAssociatedBoardDataBase

class BlePolarisDiscoveryForProvisioning : NodeListActivity() {

    private val nfcServiceResponse = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                SmarTag2Service.READ_TAG_BLE_MAC_ADDRESS_ACTION -> {
                    val mac: String? = intent.getStringExtra(
                        SmarTag2Service.EXTRA_TAG_BLE_MAC_ADDRESS
                    )
                    if(mac != null) {
                        _nfcTagMacAddress.postValue(mac)
                    }
                }
            }
        }
    }

    private var goBack = false
    private var networkConnection: Boolean = false

    private val STBOX_PIN_DIALOG_STATUS = "STBOX_PIN_DIALOG"
    private lateinit var spEditor: SharedPreferences.Editor
    private lateinit var spReader: SharedPreferences

    private lateinit var nfcDialog: AlertDialog
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var nfcTag2ViewModel: NfcTag2ViewModel

    private val nfcTagMacAddress: LiveData<String?>
        get() = _nfcTagMacAddress
    private val _nfcTagMacAddress = MutableLiveData<String?>()

    private var showSearchTag: Boolean
        get() = nfcDialog.isShowing
        set(value) { if (!(value)) { nfcDialog.dismiss() } }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val pm: PackageManager = applicationContext.packageManager
        if (pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            menuInflater.inflate(R.menu.nfc_discovery_mac_address_for_ble_menu, menu)
            return true
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_nfc_discovery_ble_mac -> {
            if (!nfcAdapter.isEnabled) {
                Toast.makeText(applicationContext, "Please, enable NFC.", Toast.LENGTH_LONG).show()
                false
            } else {
                showWaitingNfcTagDialog()
                true
            }
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun createNfcTagDialog(){
        val builder = Builder(this@BlePolarisDiscoveryForProvisioning)
        val nfcWaitingView = LayoutInflater.from(this).inflate(R.layout.nfc_progress_bar_with_text, null, false)
        builder.setView(nfcWaitingView)
        nfcDialog = builder.create()
    }

    private fun showWaitingNfcTagDialog(){
        nfcDialog.show()
    }

    private fun initializeNfcObserver() {
        nfcTag2ViewModel.nfcTag.observe(this, Observer {
            if (it == null) {
                showSearchTag = true
                Toast.makeText(applicationContext, getString(com.st.nfcSmarTag.R.string.main_tag_missing), Toast.LENGTH_SHORT).show()
            } else {
                showSearchTag = false
                startReadingConfiguration(tag = it)
                Toast.makeText(applicationContext, com.st.nfcSmarTag.R.string.main_tag_detected, Toast.LENGTH_SHORT).show()
            }
        })
        nfcTag2ViewModel.ioError.observe(this, Observer { errorMsg ->
            if (errorMsg != null)
                Log.e("SmartTag", "error: $errorMsg")
        })
    }

    private fun startReadingConfiguration(tag: Tag) {
        SmarTag2Service.startReadingBleMacAddress(
            applicationContext,
            tag
        )
    }

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "Select Your ASTRA"

        val pm: PackageManager = applicationContext.packageManager
        if (pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this)
            nfcTag2ViewModel = NfcTag2ViewModel.create(this)
            initializeNfcObserver()
            createNfcTagDialog()
            nfcAdapter.enableReaderMode(
                this,
                { foundTag -> nfcTag2ViewModel.nfcTagDiscovered(foundTag) },
                NfcAdapter.FLAG_READER_NFC_V, Bundle()
            )
        }

        // Remove useful menu items (for ATR app) from BottomAppBar
        val bottomAppBar: BottomAppBar = findViewById(R.id.bottomAppBar)
        removeMenuItems(bottomAppBar.menu)

        spEditor = applicationContext.getSharedPreferences(STBOX_PIN_DIALOG_STATUS, Context.MODE_PRIVATE).edit()
        spReader = applicationContext.getSharedPreferences(STBOX_PIN_DIALOG_STATUS, Context.MODE_PRIVATE)

        // Check Phone Connection
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.let {
            try{
                it.registerDefaultNetworkCallback(
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        networkConnection = true
                    }
                    override fun onLost(network: Network) {
                        networkConnection = false
                    }
                })
            }catch (e: NoSuchMethodError){ networkConnection = true }
        }

        nfcTagMacAddress.observe(this, Observer { mac ->
            val node = mAdapter.findNodeByMacAddress(mac)
            if(node != null){
                onNodeSelected(node)
            }
        })
    }

    override fun onResume() {
        if(goBack){
            onBackPressed()
        }
        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(nfcServiceResponse, SmarTag2Service.getReadWriteConfigurationFilter())
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableReaderMode(this)
        LocalBroadcastManager.getInstance(applicationContext)
            .unregisterReceiver(nfcServiceResponse)
    }

    override fun onNodeSelected(n: Node) {
        if(networkConnection) {
            val intent = BlePolarisProvisioningDevice.startWithNode(this, n)
            //startActivity(intent)
            startActivityForResult(intent, 0)
        } else {
            showSnackbar(window.decorView.rootView, "Offline. Check your Internet connection.")
        }
    }

    override fun onNodeAdded(mItem: Node?, mNodeAddedIcon: ImageView?) {
        if(mItem != null) {
            val associatedDB = ReadAssociatedBoardDataBase(
                applicationContext
            )
            val associatedBoard: AssociatedBoard? = associatedDB.getBoardDetailsWithMAC(mItem.tag)

            if (associatedBoard != null) {
                associatedDB.removeWithMAC(mItem.tag)
                mNodeAddedIcon!!.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext, R.drawable.ic_not_favorite
                    )
                )
            } else {
                val associatedBoardNew: ArrayList<AssociatedBoard> = ArrayList<AssociatedBoard>()
                associatedBoardNew.add(
                    AssociatedBoard(
                        mItem.tag,
                        mItem.name,
                        AssociatedBoard.ConnectivityType.ble,
                        null,
                        null,
                        null,
                        false,
                        null
                    )
                )
                associatedDB.add(associatedBoardNew)
                mNodeAddedIcon!!.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext, R.drawable.ic_favorite
                    )
                )
            }
        }
    }

    override fun displayNode(n: Node): Boolean {
        return n.type == Node.Type.ASTRA1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == 1) {
            goBack = true
        }
    }

    private fun showSnackbar(view: View, message: String) {
        Snackbar.make(view,
                message,
                Snackbar.LENGTH_SHORT)
                .show()
    }

    private fun removeMenuItems(menu: Menu){
        for (i in 0 until menu.size){
            val menuItem = menu.getItem(i)
            when (menuItem.itemId) {
                R.id.action_add_db_entry -> {
                    menuItem.isVisible = false
                }
                R.id.action_add_dtdl_entry -> {
                    menuItem.isVisible = false
                }
                R.id.action_reset_db_entry -> {
                    menuItem.isVisible = false
                }
            }
        }
    }
}