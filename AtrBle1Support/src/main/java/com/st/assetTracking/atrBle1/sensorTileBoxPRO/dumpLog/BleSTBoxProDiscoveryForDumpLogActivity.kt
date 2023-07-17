package com.st.assetTracking.atrBle1.sensorTileBoxPRO.dumpLog

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.bottomappbar.BottomAppBar
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.NodeListActivity
import com.st.assetTracking.atrBle1.BuildConfig
import com.st.assetTracking.atrBle1.R
import com.st.utility.databases.associatedBoard.AssociatedBoard
import com.st.utility.databases.associatedBoard.ReadAssociatedBoardDataBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import sensorTileBoxPro.PnPL.PnPLConfigurationActivity
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

internal interface DtmiManagerRestApi {
    @GET("{path}")
    suspend fun getDtmi(@Path("path", encoded = true) path: String): Response<ResponseBody>
}

class BleSTBoxProDiscoveryForDumpLogActivity : NodeListActivity() {

    private lateinit var remoteDtmiRestApi: DtmiManagerRestApi

    private val STBOX_PIN_DIALOG_STATUS = "STBOX_PIN_DIALOG"
    private lateinit var spEditor: SharedPreferences.Editor
    private lateinit var spReader: SharedPreferences

    private var isPnPLRequest = false
    private var id: String? = null
    private var macAddress: String? = null

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "Select Your SensorTile.Box PRO"

        // Remove useful menu items (for ATR app) from BottomAppBar
        //val bottomAppBar: BottomAppBar = findViewById(R.id.bottomAppBar)
        //removeMenuItems(bottomAppBar.menu)

        spEditor = applicationContext.getSharedPreferences(STBOX_PIN_DIALOG_STATUS, Context.MODE_PRIVATE).edit()
        spReader = applicationContext.getSharedPreferences(STBOX_PIN_DIALOG_STATUS, Context.MODE_PRIVATE)

        isPnPLRequest = intent.getBooleanExtra("pnpl", false)
        id = intent.getStringExtra("id")
        if(intent.getStringExtra("mac") != null) {
            macAddress = intent.getStringExtra("mac")
        }else{
            displayMACinfoMissing()
        }
    }

    override fun onNodeSelected(n: Node) {
        if(id != null && id != "") {
            if(isPnPLRequest) {
                downloadDtmi(n)
            } else {
                val intent = BleSTBoxProDumpLogActivity.startWithNode(this, n, id!!)
                startActivityForResult(intent, 0)
            }
        }
    }

    private fun downloadDtmi(n: Node) {
        val fwBoard = n.fwDetails
        if (fwBoard != null) {
            val dtmi = fwBoard.dtmi
            if (dtmi != null) {
                val dtmiUriPath = dtmi.replace(':', '/')
                    .replace(';', '-') + ".expanded.json"

                val httpClient = OkHttpClient.Builder()
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)

                remoteDtmiRestApi = Retrofit.Builder()
                    .baseUrl(BuildConfig.DTDL_DB_BASE_URL_BETA)
                    .client(httpClient.build())
                    .build()
                    .create(DtmiManagerRestApi::class.java)

                CoroutineScope(Dispatchers.Main).launch {
                    val result = remoteDtmiRestApi.getDtmi(dtmiUriPath)
                    when(result.code()) {
                        200 -> {
                            val a = result.body()?.string()
                            n.dtdlModel = a
                            /** Start PnPL Activity */
                            val intent = PnPLConfigurationActivity.startWithNode(applicationContext, n)
                            startActivityForResult(intent, 0)
                        }
                        else -> {
                            Toast.makeText(applicationContext, result.errorBody().toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    Executors.newSingleThreadScheduledExecutor().schedule({
                        downloadDtmi(n)
                    }, 1, TimeUnit.SECONDS)
                }
            }
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                Executors.newSingleThreadScheduledExecutor().schedule({
                    downloadDtmi(n)
                }, 1, TimeUnit.SECONDS)
            }
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
        if((macAddress != null) && (n.advertiseInfo.address == macAddress)){
            onNodeSelected(n)
        }
        return n.type == Node.Type.SENSOR_TILE_BOX_PRO
    }

    private fun displayMACinfoMissing() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Missing Information")
        builder.setMessage("I have no information to automatically connect to the board.\nPlease select manually the correct board.")

        builder.setPositiveButton(android.R.string.yes) { dialog, _ ->
            dialog.dismiss()
        }

        builder.setCancelable(false)

        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == 1) {
            super.onBackPressed()
        }
    }

    /*
    private fun removeMenuItems(menu: Menu){
        //navBar.menu.getItem(0).isVisible = false
        for (i in 0 until menu.size()){
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
    */
}