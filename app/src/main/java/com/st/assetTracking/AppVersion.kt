package com.st.assetTracking

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.st.BlueSTSDK.fwDataBase.ReadBoardFirmwareDataBase
import com.st.BlueSTSDK.gui.AboutActivity

class AppVersion : AppCompatActivity() {

    private var counter = 0

    val BETA_PREFERENCE = AboutActivity::class.java.canonicalName?.plus(".BETA_PREFERENCE")
    val ENABLE_BETA_FUNCTIONALITIES = AboutActivity::class.java.canonicalName?.plus(".ENABLE_BETA_FUNCTIONALITIES")

    private lateinit var betaTextView: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_version)
        title = "Version"

        betaTextView = findViewById(R.id.betaAPPVersion)
        checkBetaStatus()

        val tvVersion: TextView = findViewById(R.id.appVersion)
        val pInfo: PackageInfo = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
        tvVersion.text = pInfo.versionName + " - Build (${BuildConfig.VERSION_CODE})"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvVersion.setOnClickListener {
            if(counter == 5) {
                if (!(getSharedPreferences(BETA_PREFERENCE, MODE_PRIVATE).getBoolean(ENABLE_BETA_FUNCTIONALITIES, false))) {
                    enableBetaDialog()
                    counter = 0
                } else {
                    disableBetaDialog()
                     counter = 0
                }
            } else {
                counter += 1
            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkBetaStatus(){
        if (!(getSharedPreferences(BETA_PREFERENCE, MODE_PRIVATE).getBoolean(ENABLE_BETA_FUNCTIONALITIES, false))) {
            betaTextView.visibility = View.GONE
        }
    }

    private fun enableBetaDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("BETA")
        builder.setMessage("Do you want to enable BETA features?")
        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            enableBETA()
            dialog.dismiss()
            betaTextView.visibility = View.VISIBLE
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun disableBetaDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("BETA")
        builder.setMessage("Do you want to disable BETA features?")
        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            disableBETA()
            dialog.dismiss()
            betaTextView.visibility = View.GONE
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun enableBETA() {
        getSharedPreferences(BETA_PREFERENCE, MODE_PRIVATE).edit()
            .putBoolean(ENABLE_BETA_FUNCTIONALITIES, true)
            .apply()
        val firmwareDB = ReadBoardFirmwareDataBase(this)
        firmwareDB.readDbBeta()
    }

    private fun disableBETA() {
        getSharedPreferences(BETA_PREFERENCE, MODE_PRIVATE).edit()
            .remove(ENABLE_BETA_FUNCTIONALITIES)
            .apply()
        val firmwareDB = ReadBoardFirmwareDataBase(this)
        firmwareDB.readDb()
    }

}

