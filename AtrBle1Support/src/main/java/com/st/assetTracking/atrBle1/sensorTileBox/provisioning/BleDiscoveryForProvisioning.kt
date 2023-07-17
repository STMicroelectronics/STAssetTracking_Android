package com.st.assetTracking.atrBle1.sensorTileBox.provisioning

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.size
import com.google.android.material.bottomappbar.BottomAppBar
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.NodeListActivity
import com.st.BlueSTSDK.gui.util.SimpleFragmentDialog
import com.st.assetTracking.atrBle1.R
import com.st.utility.databases.associatedBoard.AssociatedBoard
import com.st.utility.databases.associatedBoard.ReadAssociatedBoardDataBase


class BleDiscoveryForProvisioning : NodeListActivity() {

    private val STBOX_PIN_DIALOG_SHOWN = BleDiscoveryForProvisioning::class.java.canonicalName + ".STBOX_PIN_DIALOG_SHOWN"
    private val STBOX_PIN_DIALOG_SHOWN_TAG = BleDiscoveryForProvisioning::class.java.canonicalName + ".STBOX_PIN_DIALOG_SHOWN_TAG"
    private val STBOX_PIN_DIALOG_STATUS = "STBOX_PIN_DIALOG"
    private lateinit var spEditor: SharedPreferences.Editor
    private lateinit var spReader: SharedPreferences

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "Select SensorTile.box"

        // Remove useful menu items (for ATR app) from BottomAppBar
        val bottomAppBar: BottomAppBar = findViewById(R.id.bottomAppBar)
        removeMenuItems(bottomAppBar.menu)

        displayInfoMessage(applicationContext)
        spEditor = applicationContext.getSharedPreferences(STBOX_PIN_DIALOG_STATUS, Context.MODE_PRIVATE).edit()
        spReader = applicationContext.getSharedPreferences(STBOX_PIN_DIALOG_STATUS, Context.MODE_PRIVATE)
    }
    override fun onResume() {
        super.onResume()
        displayInfoMessage(applicationContext)
    }

    override fun onNodeSelected(n: Node) {
        if(!(spReader.getBoolean(STBOX_PIN_DIALOG_SHOWN, false))){
            displayPinWarnings(n)
        }else{
            val intent = BleProvisioningDevice.startWithNode(this, n)
            startActivity(intent)
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
        return n.type == Node.Type.SENSOR_TILE_BOX
    }

    private fun displayInfoMessage(context: Context){
        Toast.makeText(context, "Press USER button on SensorTile.Box", Toast.LENGTH_SHORT).show()
    }

    private fun displayPinWarnings(node: Node) {
        val dialog: SimpleFragmentDialog = SimpleFragmentDialog.newInstance(R.string.nodeList_stbox_pinTitle, R.string.nodeList_stbox_pinDesc)
        dialog.show(supportFragmentManager, STBOX_PIN_DIALOG_SHOWN_TAG)
        dialog.setOnclickListener { _: DialogInterface?, _: Int ->
            spEditor.putBoolean(STBOX_PIN_DIALOG_SHOWN, true).apply()
            dialog.dismiss()
            //val intent = AtrBleDeviceDetails.startWithNode(applicationContext, node)
            //startActivity(intent)
        }
    }

    private fun removeMenuItems(menu: Menu){
        //navBar.menu.getItem(0).isVisible = false
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