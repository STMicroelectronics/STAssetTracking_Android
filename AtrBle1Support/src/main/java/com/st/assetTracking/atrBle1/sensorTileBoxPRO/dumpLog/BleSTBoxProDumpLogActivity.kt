package com.st.assetTracking.atrBle1.sensorTileBoxPRO.dumpLog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.FeatureBinaryContent
import com.st.BlueSTSDK.Features.FeatureBinaryContent.Companion.getBinaryContentToUInt32
import com.st.BlueSTSDK.Features.PnPL.FeaturePnPL
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.ActivityWithNode
import com.st.assetTracking.atrBle1.R
import com.st.assetTracking.atrBle1.sensorTileBox.communication.waitStatus
import com.st.assetTracking.dashboard.model.Device
import com.st.assetTracking.dashboard.uploadData.uploader.UploaderGenericDataActivity
import com.st.assetTracking.data.GenericDSHDataSample
import com.st.assetTracking.data.GenericDSHSample
import com.st.assetTracking.data.GenericSample
import com.st.assetTracking.data.getSensorDataSample
import com.st.login.Configuration
import com.st.login.LoginManager
import com.st.login.loginprovider.LoginProviderFactory
import com.st.nfcSmarTag.v2.samples.Tag2SamplesListedFragment
import com.st.nfcSmarTag.v2.samples.Tag2SamplesPlotFragment
import com.st.nfcSmarTag.v2.samples.Tag2SamplesViewModel
import com.st.smartaglibrary.v2.SmarTag2BLE
import com.st.smartaglibrary.v2.catalog.NFCBoardCatalogService
import com.st.smartaglibrary.v2.catalog.NfcV2BoardCatalog
import com.st.smartaglibrary.v2.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*


class BleSTBoxProDumpLogActivity : ActivityWithNode() {

    private lateinit var readProgress: ProgressBar

    private lateinit var nfcV2BLE: SmarTag2BLE
    private lateinit var data: ByteArray

    private lateinit var dumpLogViewModel: Tag2SamplesViewModel

    private val _nfcCatalog = MutableLiveData<NfcV2BoardCatalog?>()
    private val nfcCatalog: LiveData<NfcV2BoardCatalog?>
        get() = _nfcCatalog

    companion object {
        var deviceID: String = ""
        fun startWithNode(context: Context, node: Node, id: String): Intent {
            deviceID = id
            return getStartIntent(
                context,
                BleSTBoxProDumpLogActivity::class.java,
                node,
                true
            )
        }
    }

    /** Feature PnPL */
    private var mFeaturePnPL : FeaturePnPL? =null
    private val mFeaturePnPLListener = Feature.FeatureListener { _, _ -> }

    /** Feature Binary Content */
    private var mFeatureBinaryContent : FeatureBinaryContent? =null
    private val mFeatureBinaryContentListener = Feature.FeatureListener { _, _ ->

        data = sExtractBinaryContent.let {
            mFeatureBinaryContent?.let { it1 ->
                extractData(
                    it1,
                    it
                )
            }
        }!!

        val dataLong = getBinaryContentToUInt32(data)

        nfcV2BLE = SmarTag2BLE(dataLong)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                nfcV2BLE.readDataSample(
                    onReadNumberOfSample = { numberOfSample ->
                        if(numberOfSample!=null)
                            dumpLogViewModel.setNumberSample(numberOfSample)
                        else
                            Toast.makeText(applicationContext, "Error reading number of sample.", Toast.LENGTH_LONG).show()
                    },
                    onReadSample = {
                        dumpLogViewModel.appendSample(it)
                        println("DATA SAMPLE: $it")
                    }
                )
            } catch (e: IOException) {
                Toast.makeText(applicationContext, "Something went wrong.", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * get a sample for each feature and extract the float data from it
     *
     * @param features            list of feature
     * @param extractDataFunction object that will extract the data from the sample
     * @return list of values inside the feature
     */
    private fun extractData(feature: FeatureBinaryContent, extractDataFunction: ExtractDataFunction): ByteArray {
        return extractDataFunction.getData(feature.sample)
    }

    private interface ExtractDataFunction {
        fun getData(s: Feature.Sample?): ByteArray
    }

    /**
     * object that extract the temperature from a feature sample
     */
    private val sExtractBinaryContent: ExtractDataFunction =
        object : ExtractDataFunction {
            override fun getData(s: Feature.Sample?): ByteArray {
                return FeatureBinaryContent.getBinaryContent(s)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Local Data"
        setContentView(R.layout.activity_ble_stbox_pro_dump_log)

        readProgress = findViewById(R.id.sensor_tile_box_pro_samples_readProgress)
        findViewById<ViewPager>(R.id.sensor_tile_box_pro_samples_viewPager).adapter = TabViewAdapter(
            supportFragmentManager
        )

        dumpLogViewModel = Tag2SamplesViewModel.create(this@BleSTBoxProDumpLogActivity)

        initializeSamplesObserver()

        CoroutineScope(Dispatchers.Main).launch {
            val nfcCatalog = NFCBoardCatalogService().getNfcCatalog()
            _nfcCatalog.postValue(nfcCatalog)
        }
        nfcCatalog.observe(this, Observer { catalog ->
            if (catalog != null) {
                NFCBoardCatalogService.storeCatalog(catalog)
            } else {
                Toast.makeText(applicationContext, "Impossible to retrieve NFC Catalog. Please check your internet connectivity.", Toast.LENGTH_SHORT).show()
            }
        })

        CoroutineScope(Dispatchers.Main).launch {
            node?.waitStatus(Node.State.Connected)
            if(node?.isConnected == true) {
                node?.let { n -> enableNotification(n) }
            }
        }
    }

    override fun onDestroy() {
        disableNotification(node!!)
        super.onDestroy()
    }

    /** Enable all BLE notifications */
    private fun enableNotification(node: Node){
        mFeatureBinaryContent = node.getFeature(FeatureBinaryContent::class.java)
        mFeatureBinaryContent?.apply {
            addFeatureListener(mFeatureBinaryContentListener)
            enableNotification()
        }
        mFeaturePnPL = node.getFeature(FeaturePnPL::class.java)
        mFeaturePnPL?.apply {
            addFeatureListener(mFeaturePnPLListener)
            enableNotification()
        }

        sendReadLogCommand()
    }

    private fun sendReadLogCommand(){
        mFeaturePnPL.apply {
            mFeaturePnPL?.sendPnPLCommandCmd("control", "read_log")
        }
    }

    /** Disable all BLE notifications */
    private fun disableNotification(node: Node){
        node.getFeature(FeatureBinaryContent::class.java)?.apply {
            removeFeatureListener(mFeatureBinaryContentListener)
            disableNotification()
        }
        mFeatureBinaryContent = null

        node.getFeature(FeaturePnPL::class.java)?.apply {
            removeFeatureListener(mFeaturePnPLListener)
            disableNotification()
        }
        mFeaturePnPL = null

    }

    private fun initializeSamplesObserver() {
        dumpLogViewModel.numberSample.observe(this, Observer {
            it?.let { nSample ->
                Log.d("progress", "Total: $nSample")
                readProgress.max = nSample
                readProgress.visibility = View.VISIBLE
            }
        })

        dumpLogViewModel.lastSensorSample.observe(this,getProgressUpdate())

        dumpLogViewModel.isUpdating.observe(this, Observer {
            it?.let { isReading ->
                if(!isReading){
                    val id: String = deviceID

                    val samplesAWS = dumpLogViewModel.allSampleList.value
                    Log.d("ALL SENSORTILE.BOX PRO SAMPLES --->", samplesAWS.toString())

                    if(samplesAWS!=null){
                        if(samplesAWS.size != 0) {
                            askToSyncData(id, samplesAWS)
                        }
                    }
                }
            }
        })
    }

    private fun <T> getProgressUpdate():Observer<T>{
        return Observer {
            val nReadSample = dumpLogViewModel.allSampleList.value?.size ?: 0
            val nTotalSample = dumpLogViewModel.numberSample.value ?: 0
            Log.d("progress","$nReadSample / $nTotalSample")
            if(nReadSample>=nTotalSample){
                readProgress.visibility = View.GONE
            }else{
                readProgress.progress = nReadSample
            }
        }
    }

    private fun askToSyncData(deviceId: String, data: List<GenericSample>) {
        val dialog = AlertDialog.Builder(this@BleSTBoxProDumpLogActivity)
            .setTitle("Sync cloud Data")
            .setMessage("Do you want to upload the data on cloud?")
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                syncData(deviceId, data)
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    private fun syncData(deviceId: String, data: List<GenericSample>) {
        val loginManager = LoginManager(activityResultRegistry,
            this@BleSTBoxProDumpLogActivity,
            applicationContext,
            LoginProviderFactory.LoginProviderType.COGNITO,
            Configuration.getInstance(applicationContext, com.st.login.R.raw.auth_config_cognito)
        )


        loginManager.getAtrAuthData { authData ->
            if (authData != null) {
                val dshSamples = genericSampleToDSHSample(data)
                UploaderGenericDataActivity.startActivityToUploadGenericData(authData, applicationContext, deviceId, dshSamples, Device.Type.SENSORTILEBOXPRO.toString(), "ble")
            }
        }
    }

    private fun genericSampleToDSHSample(data: List<GenericSample>): List<GenericDSHSample> {
        val samples = data.getSensorDataSample()
        val dshSamples: ArrayList<GenericDSHSample> = ArrayList()
        samples.forEach { sample ->
            val strV = String.format("%.2f", sample.value)
            val stringValue = strV.replace(",", ".")
            val v = stringValue.toDouble()
            dshSamples.add(GenericDSHDataSample(sample.id, sample.type, sample.date, v))
        }
        return dshSamples
    }

    private class TabViewAdapter(fm: FragmentManager) : androidx.fragment.app.FragmentPagerAdapter(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        companion object {
            val FRAGMENT_VIEW = arrayOf(
                Tag2SamplesPlotFragment::class.java,
                Tag2SamplesListedFragment::class.java
            )
        }

        override fun getCount(): Int {
            return FRAGMENT_VIEW.size
        }

        override fun getItem(position: Int): Fragment {
            return FRAGMENT_VIEW[position].newInstance()
        }

        override fun getPageTitle(position: Int): CharSequence {
            return if (position==0)
                "Data Plots"
            else
                "Data Listed"
        }
    }

}
