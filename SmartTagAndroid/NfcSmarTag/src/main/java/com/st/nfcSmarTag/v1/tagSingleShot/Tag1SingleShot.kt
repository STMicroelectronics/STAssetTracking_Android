package com.st.nfcSmarTag.v1.tagSingleShot

import android.content.Intent

import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.st.nfcSmarTag.SmarTagViewModel
import com.st.nfcSmarTag.R

class Tag1SingleShot : AppCompatActivity() {

    private val singleShotFragment = TagSingleShotFragment()

    private lateinit var nfcTagHolder: SmarTagViewModel
    private lateinit var nfcAdapter: NfcAdapter

    private var errorToast: Toast? = null
    private lateinit var mainView: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "SmarTag 1"

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContentView(R.layout.activity_tag1_single_shot)
        mainView = findViewById(R.id.main_root_view)

        if (savedInstanceState == null)//first time
            showFragment(singleShotFragment)

        errorToast = Toast(this)

        nfcTagHolder = SmarTagViewModel.create(this)
        initializeNfcObserver()

        if (intent?.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                nfcTagHolder.nfcTagDiscovered(tag)
            }
        }
    }

    private fun initializeNfcObserver() {
        nfcTagHolder.nfcTag.observe(this, Observer {
            if (it == null) {
                //showSearchTag = true
                Toast.makeText(applicationContext, getString(R.string.main_tag_missing), Toast.LENGTH_SHORT).show()
            } else {
                //showSearchTag = false
                Toast.makeText(applicationContext, R.string.main_tag_detected, Toast.LENGTH_SHORT).show()
            }
        })
        nfcTagHolder.ioError.observe(this, Observer { errorMsg ->
            if (errorMsg != null)
                Log.e("SmartTag", "error: $errorMsg")
        })
    }


    override fun onResume() {
        super.onResume()

        if (!nfcAdapter.isEnabled) {
            Snackbar.make(mainView, R.string.main_nfc_disabled,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.main_nfc_enable_button) {
                    startActivity(Intent(android.provider.Settings.ACTION_NFC_SETTINGS))
                }
                .show()
            return
        }

        nfcAdapter.enableReaderMode(this,
            { foundTag -> nfcTagHolder.nfcTagDiscovered(foundTag) },
            NfcAdapter.FLAG_READER_NFC_V, Bundle())
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableReaderMode(this)
    }

    private fun showFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_contentView, fragment)
            .commit()
    }

}