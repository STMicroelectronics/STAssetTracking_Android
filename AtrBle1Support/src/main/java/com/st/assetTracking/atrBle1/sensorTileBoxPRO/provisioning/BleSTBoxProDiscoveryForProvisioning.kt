package com.st.assetTracking.atrBle1.sensorTileBoxPRO.provisioning

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.size
import com.google.android.material.bottomappbar.BottomAppBar
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.NodeListActivity
import com.st.assetTracking.atrBle1.R
import com.st.utility.databases.associatedBoard.AssociatedBoard
import com.st.utility.databases.associatedBoard.ReadAssociatedBoardDataBase

class BleSTBoxProDiscoveryForProvisioning : NodeListActivity() {

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "Select SensorTile.box PRO"

        // Remove useful menu items (for ATR app) from BottomAppBar
        val bottomAppBar: BottomAppBar = findViewById(R.id.bottomAppBar)
        removeMenuItems(bottomAppBar.menu)
    }
    override fun onResume() {
        super.onResume()
        displayInfoMessage(applicationContext)
    }

    override fun onNodeSelected(n: Node) {
        val intent = BleSTBoxProProvisioningDevice.startWithNode(this, n)
        startActivity(intent)
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
        return n.type == Node.Type.SENSOR_TILE_BOX_PRO
    }

    private fun displayInfoMessage(context: Context){
        Toast.makeText(context, "Press USER button on SensorTile.Box PRO", Toast.LENGTH_SHORT)
            .show()
    }

    private fun removeMenuItems(menu: Menu){
        //navBar.menu.getItem(0).isVisible = false
        for (i in 0 until menu.size){
            val menuItem = menu.getItem(i)
            when (menuItem.itemId) {
                R.id.action_add_db_entry -> {
                    menuItem.isVisible = true
                }
                R.id.action_add_dtdl_entry -> {
                    menuItem.isVisible = false
                }
                R.id.action_reset_db_entry -> {
                    menuItem.isVisible = true
                }
            }
        }
    }
}