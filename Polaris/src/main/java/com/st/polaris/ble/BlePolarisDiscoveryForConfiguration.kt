package com.st.polaris.ble

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.size
import com.google.android.material.bottomappbar.BottomAppBar
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.NodeListActivity
import com.st.polaris.R
import com.st.utility.databases.associatedBoard.AssociatedBoard
import com.st.utility.databases.associatedBoard.ReadAssociatedBoardDataBase

class BlePolarisDiscoveryForConfiguration : NodeListActivity() {

    private val STBOX_PIN_DIALOG_STATUS = "STBOX_PIN_DIALOG"
    private lateinit var spEditor: SharedPreferences.Editor
    private lateinit var spReader: SharedPreferences

    private var id: String? = null
    private var macAddress: String? = null

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "Select Your ASTRA"

        // Remove useful menu items (for ATR app) from BottomAppBar
        val bottomAppBar: BottomAppBar = findViewById(R.id.bottomAppBar)
        removeMenuItems(bottomAppBar.menu)

        spEditor = applicationContext.getSharedPreferences(STBOX_PIN_DIALOG_STATUS, Context.MODE_PRIVATE).edit()
        spReader = applicationContext.getSharedPreferences(STBOX_PIN_DIALOG_STATUS, Context.MODE_PRIVATE)

        id = intent.getStringExtra("id")
        if(intent.getStringExtra("mac") != null) {
            macAddress = intent.getStringExtra("mac")
        }else{
            displayMACinfoMissing()
        }
    }
    override fun onResume() {
        super.onResume()
    }

    override fun onNodeSelected(n: Node) {
        val intent = BleAstraOptions.startWithNode(this, n, id!!)
        startActivityForResult(intent, 0)
        //startActivity(intent)
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
        return n.type == Node.Type.ASTRA1
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