package com.st.polaris.ble.connectivity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.st.polaris.R
import com.st.polaris.ble.BlePolarisDiscoveryForConfiguration
import com.st.nfcSmarTag.v2.NfcTag2MainActivity

class AstraConnectivityActivity : AppCompatActivity() {

    private var id: String? = null
    private var macAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_astra_connectivity)

        title = "Connectivity"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        id = intent.getStringExtra("id")
        macAddress = intent.getStringExtra("mac")

        findViewById<CardView>(R.id.bleCardView).setOnClickListener {
            val intent = Intent(applicationContext, BlePolarisDiscoveryForConfiguration::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("mac", macAddress)
            intent.putExtra("id", id)
            applicationContext?.startActivity(intent)
        }

        findViewById<CardView>(R.id.nfcCardView).setOnClickListener {
            val intent = Intent(applicationContext, NfcTag2MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("id", id)
            applicationContext?.startActivity(intent)
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