package com.st.nfcSmarTag.v2.singleshot

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.st.nfcSmarTag.R
import com.st.nfcSmarTag.SmarTagViewModel
import com.st.nfcSmarTag.v2.NfcTag2ViewModel
import com.st.smartaglibrary.v2.catalog.NFCBoardCatalogService
import com.st.smartaglibrary.v2.catalog.NfcV2BoardCatalog
import com.st.smartaglibrary.v2.catalog.NfcV2Firmware

class Tag2SingleShot : AppCompatActivity() {

    private val tag2SingleShotFragment = Tag2SingleShotFragment()

    private lateinit var nfcTag2ViewModel: NfcTag2ViewModel
    private lateinit var smartTag: Tag2SingleShotViewModel
    private lateinit var nfcTagHolder: SmarTagViewModel
    private lateinit var nfcAdapter: NfcAdapter

    private var errorToast: Toast? = null
    private lateinit var mainView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "SmarTag 2"

        nfcTag2ViewModel = NfcTag2ViewModel.create(this@Tag2SingleShot)
        smartTag = Tag2SingleShotViewModel()
        initializeNFCCatalog()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContentView(R.layout.activity_tag2_single_shot)
        mainView = findViewById(R.id.tag2_main_root_view)

        if (savedInstanceState == null)//first time
            showFragment(tag2SingleShotFragment)

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_single_shot_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.custom_entry -> {
                val intent = Intent()
                    .setType("*/*")
                    .setAction(Intent.ACTION_GET_CONTENT)

                startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initializeNFCCatalog(){
        smartTag.retrieveNfcCatalog()
        smartTag.nfcCatalog.observe(this, Observer { catalog ->
            if (catalog != null) {
                val customFw = nfcTag2ViewModel.getCustomFwEntry()
                if(customFw != null){
                    appendCustomFwEntry(catalog, customFw)
                } else {
                    NFCBoardCatalogService.storeCatalog(catalog)
                }
            } else {
                Toast.makeText(applicationContext, "Impossible to retrieve NFC Catalog. Please check your internet connectivity.", Toast.LENGTH_SHORT).show()
                onBackPressed()
            }
        })
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
                    startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
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
            .replace(R.id.tag2_main_contentView, fragment)
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 111 && resultCode == RESULT_OK) {
            //val selectedFile = data?.data // The URI with the location of the file
            val path = data?.data
            val jsonSelectedFile = path?.let { contentResolver.openInputStream(it) }
            val inputAsString = jsonSelectedFile?.bufferedReader().use { it?.readText() }

            val gson = Gson()
            val customFw: NfcV2Firmware? = try {
                gson.fromJson(inputAsString, NfcV2Firmware::class.java)
            } catch (e:java.lang.Exception) {
                null
            }

            if(customFw != null) {
                nfcTag2ViewModel.putCustomFwEntry(customFw)
                appendCustomFwEntry(NFCBoardCatalogService.getCatalog(), customFw)
                Toast.makeText(this, "Custom Firmware Entry ADDED.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "WRONG JSON FORMAT.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun appendCustomFwEntry(catalog: NfcV2BoardCatalog, customFwEntry: NfcV2Firmware) {
        val fwList = catalog.nfcV2FirmwareList
        val fwArrayList: ArrayList<NfcV2Firmware> = ArrayList()
        fwList.forEach { fw ->
            if(fw.nfcFwID != customFwEntry.nfcFwID) {
                fwArrayList.add(fw)
            }
        }
        fwArrayList.add(customFwEntry)
        catalog.nfcV2FirmwareList = fwArrayList.toList()
        NFCBoardCatalogService.storeCatalog(catalog)
    }

}