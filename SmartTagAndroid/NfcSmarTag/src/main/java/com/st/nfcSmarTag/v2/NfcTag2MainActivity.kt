package com.st.nfcSmarTag.v2

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.st.nfcSmarTag.*
import com.st.nfcSmarTag.v2.extremes.Tag2ExtremesFragment
import com.st.nfcSmarTag.v2.samples.Tag2SamplesFragment
import com.st.nfcSmarTag.v2.settings.Tag2SettingsFragment
import com.st.smartaglibrary.v2.catalog.NFCBoardCatalogService
import com.st.nfcSmarTag.R

class NfcTag2MainActivity : AppCompatActivity() {

    private var needToShowSamples: Boolean = true

    private lateinit var mainView: View
    private lateinit var navigationView: BottomNavigationView

    private var settingFragment = Tag2SettingsFragment(null)
    private val extremesFragment = Tag2ExtremesFragment()
    private var samplesFragment = Tag2SamplesFragment(null)

    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var nfcTag2ViewModel: NfcTag2ViewModel

    private var showSearchTag: Boolean
        get() = findViewById<View>(R.id.tag2_main_progressBar).visibility == View.VISIBLE
        set(value) {
            val visibility = if (value) View.VISIBLE else View.GONE
            findViewById<View>(R.id.tag2_main_progressBar).visibility = visibility
            findViewById<View>(R.id.tag2_main_progressMessage).visibility = visibility
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc_tag2_main)

        val id = intent.getStringExtra("id")
        needToShowSamples = intent.getBooleanExtra("showSamples", true)

        settingFragment = Tag2SettingsFragment(id)
        samplesFragment = Tag2SamplesFragment(id)

        val pm: PackageManager = applicationContext.packageManager
        if (!(pm.hasSystemFeature(PackageManager.FEATURE_NFC))) {
            onBackPressed()
            Toast.makeText(this, "NFC is not supported.", Toast.LENGTH_SHORT).show()
            onBackPressed()
        } else {

            nfcAdapter = NfcAdapter.getDefaultAdapter(this)
            nfcTag2ViewModel = NfcTag2ViewModel.create(this)

            initializeNFCCatalog()

            mainView = findViewById(R.id.tag2_main_contentView)
            navigationView = findViewById(R.id.tag2_main_navigation)
            navigationView.setOnNavigationItemSelectedListener {
                this.handleNavigationItemSelected(
                    it
                )
            }

            if (!needToShowSamples) {
                removeMenuItems(navigationView.menu)
            }

            if (savedInstanceState == null)
                showFragment(settingFragment)

            initializeNfcObserver()

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                } else {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
            }

            if (intent?.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
                val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                if (tag != null) {
                    nfcTag2ViewModel.nfcTagDiscovered(tag)
                }
            }
        }
    }

    private fun initializeNFCCatalog(){
        nfcTag2ViewModel.retrieveNfcCatalog()
        nfcTag2ViewModel.nfcCatalog.observe(this, Observer { catalog ->
            if (catalog != null) {
                NFCBoardCatalogService.storeCatalog(catalog)
            } else {
                Toast.makeText(applicationContext, "Impossible to retrieve NFC Catalog. Please check your internet connectivity.", Toast.LENGTH_SHORT).show()
                onBackPressed()
            }
        })
    }

    private fun handleNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_tag2_settings -> {
                showFragment(settingFragment)
                return true
            }
            R.id.navigation_tag2_extremes -> {
                showFragment(extremesFragment)
                return true
            }
            R.id.navigation_tag2_samples -> {
                showFragment(samplesFragment)
                return true
            }
        }
        return false
    }

    private fun showFragment(fragment: androidx.fragment.app.Fragment) {
        val bundle = Bundle()
        //bundle.putString("idSmartTag", idBoard)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(mainView.id, fragment)
            .commit()
    }

    private fun initializeNfcObserver() {
        nfcTag2ViewModel.nfcTag.observe(this, Observer {
            if (it == null) {
                showSearchTag = true
                Toast.makeText(applicationContext, getString(R.string.main_tag_missing), Toast.LENGTH_SHORT).show()
            } else {
                showSearchTag = false
                Toast.makeText(applicationContext, R.string.main_tag_detected, Toast.LENGTH_SHORT).show()
            }
        })
        nfcTag2ViewModel.ioError.observe(this, Observer { errorMsg ->
            if (errorMsg != null)
                Log.e("SmartTag", "error: $errorMsg")
        })
    }

    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onResume() {
        super.onResume()

        if(!nfcAdapter.isEnabled) {
            Snackbar.make(mainView, R.string.main_nfc_disabled,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.main_nfc_enable_button) {
                    startActivity(Intent(android.provider.Settings.ACTION_NFC_SETTINGS))
                }
                .show()
            return
        }

        nfcAdapter.enableReaderMode(this,
            { foundTag -> nfcTag2ViewModel.nfcTagDiscovered(foundTag) },
            NfcAdapter.FLAG_READER_NFC_V, Bundle())
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableReaderMode(this)
    }

    private fun removeMenuItems(menu: Menu){
        for (i in 0 until menu.size()){
            val menuItem = menu.getItem(i)
            when (menuItem.itemId) {
                R.id.navigation_tag2_samples -> {
                    menuItem.isVisible = false
                }
            }
        }
    }
}