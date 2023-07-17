package com.st.polaris.ble

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.cardview.widget.CardView
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.ActivityWithNode
import com.st.demos.polaris.ExtConfig.ExtConfigurationActivity
import com.st.polaris.R

class BleAstraOptions : ActivityWithNode() {

    companion object {
        var deviceID: String = ""
        fun startWithNode(context: Context, node: Node, id: String): Intent {
            deviceID = id
            return getStartIntent(
                context,
                BleAstraOptions::class.java,
                node,
                true
            )
        }
    }

    private var id: String? = null
    private var macAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_astra_options)

        title = "Bluetooth"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        id = intent.getStringExtra("id")
        macAddress = intent.getStringExtra("mac")

        findViewById<CardView>(R.id.settings_card_view).setOnClickListener {
            if(node != null) {
                val intent = ExtConfigurationActivity.startWithNode(this, node!!)
                startActivity(intent)
            }
        }

        findViewById<CardView>(R.id.real_time_sync_card_view).setOnClickListener {
            if(node != null && deviceID != "") {
                val intent = BlePolarisConfiguration.startWithNode(this, node!!, deviceID)
                startActivityForResult(intent, 0)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            super.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}