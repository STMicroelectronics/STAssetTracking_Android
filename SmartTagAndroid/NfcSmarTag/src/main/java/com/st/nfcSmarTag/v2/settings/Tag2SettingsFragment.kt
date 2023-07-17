package com.st.nfcSmarTag.v2.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.st.nfcSmarTag.R
import com.st.nfcSmarTag.v2.NfcTag2ViewModel
import com.st.smartaglibrary.util.getTypeSerializableExtra
import com.st.smartaglibrary.v2.SmarTag2Service
import com.st.smartaglibrary.v2.catalog.*
import com.st.smartaglibrary.v2.model.IncompatibleVirtualSensors
import com.st.smartaglibrary.v2.model.SmarTag2Configuration
import com.st.smartaglibrary.v2.model.VirtualSensorConfiguration

class Tag2SettingsFragment(private val deviceID: String?): Fragment() {

    private val nfcServiceResponse = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                SmarTag2Service.READ_TAG_CONFIGURATION_ACTION -> {
                    val smarTag2Configuration: SmarTag2Configuration = intent.getTypeSerializableExtra(
                        SmarTag2Service.EXTRA_TAG_CONFIGURATION
                    )
                    tag2SettingsViewModel.newConfiguration(smarTag2Configuration)
                }
                SmarTag2Service.WRITE_TAG_COMPLETE_ACTION -> {
                    showSnackMessage("Write completed.")
                    tag2SettingsViewModel.onSettingsWrote()
                }
                SmarTag2Service.READ_TAG_ERROR_ACTION, SmarTag2Service.WRITE_TAG_ERROR_ACTION -> {
                    val msg = intent.getStringExtra(SmarTag2Service.EXTRA_ERROR_STR)
                    nfcTag2ViewModel.nfcTagError(msg ?: "Error.")
                }
            }
        }
    }

    private lateinit var rootView: View
    private lateinit var tag2SettingsViewModel: Tag2SettingsViewModel
    private lateinit var nfcTag2ViewModel: NfcTag2ViewModel

    private var idSmartTag : String = ""

    private lateinit var configurationTv: TextView

    private lateinit var tag2IdTv: TextView
    private lateinit var tag2SamplingTimeTv: TextView
    private lateinit var tag2RecyclerView: RecyclerView
    private lateinit var storeButton: FloatingActionButton

    private lateinit var adapter: Tag2VirtualSensorAdapter
    private lateinit var catalog: NfcV2BoardCatalog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_tag2_settings, container, false)
        initializeView()
        activity?.title = "Settings"
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nfcTag2ViewModel = NfcTag2ViewModel.create(requireActivity())
        tag2SettingsViewModel = Tag2SettingsViewModel.create(requireActivity())
        initializeTagSettingsObserver()
        initializeNfcTagObserver()
    }

    private fun initializeTagSettingsObserver() {
        tag2SettingsViewModel.currentSettings.observe(viewLifecycleOwner, Observer { newConf ->
            if (newConf != null)
                displaySettings(newConf)
        })
        tag2SettingsViewModel.desiredSettings.observe(viewLifecycleOwner, Observer { conf ->
            if (conf != null) {
                writeConfiguraiton(conf)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(nfcServiceResponse, SmarTag2Service.getReadWriteConfigurationFilter())
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(nfcServiceResponse)
    }

    private fun displaySettings(configuration: SmarTag2Configuration) {
        tag2SamplingTimeTv.text = configuration.smarTag2BaseInformation.sampleTime.toString()

        catalog = NFCBoardCatalogService.getCatalog()
        val currentFw = NFCTag2CurrentFw.getCurrentFw()

        tag2RecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val completeVSConfig = buildCompleteVirtualSensorConfigurationList(
            currentFw,
            configuration.virtualSensors
        )
        adapter =
            Tag2VirtualSensorAdapter(currentFw, completeVSConfig, requireContext())
        tag2RecyclerView.adapter = adapter
        //print("BASE INFORMATION\n\n${configuration.smarTag2BaseInformation.toString()}\n\nVIRTUAL_SENSOR\n\n${configuration.virtualSensors.toString()}\n\nVIRTUAL_SENSOR_MIN_MAX\n\n${configuration.virtualSensorsMinMax.toString()}")

    }

    private fun buildCompleteVirtualSensorConfigurationList(catalog: NfcV2Firmware, virtualSensors: List<VirtualSensorConfiguration>): ArrayList<VirtualSensorConfiguration>{
        val virtualSensorsCompleteList: ArrayList<VirtualSensorConfiguration> = ArrayList()

        catalog.virtualSensors.forEach { catalogVS ->
            var virtualSensorConfiguration: VirtualSensorConfiguration? = null
            virtualSensors.forEach { vs ->
                if (vs.id == catalogVS.id) {
                    virtualSensorConfiguration = vs
                }
            }
            if (virtualSensorConfiguration == null) {
                virtualSensorConfiguration = VirtualSensorConfiguration(
                    catalogVS.id,
                    false,
                    catalogVS.sensorName,
                    catalogVS.displayName,
                    null,
                    null,
                    null
                )
                virtualSensorsCompleteList.add(virtualSensorConfiguration!!)
            } else {
                virtualSensorsCompleteList.add(virtualSensorConfiguration!!)
            }
        }

        return virtualSensorsCompleteList
    }

    private fun writeConfiguraiton(conf: SmarTag2Configuration) {
        val tag = nfcTag2ViewModel.nfcTag.value
        if (tag != null)
            SmarTag2Service.storeTag2Configuration(requireContext(), tag, conf)
    }

    private fun initializeNfcTagObserver() {
        nfcTag2ViewModel.nfcTag.observe(viewLifecycleOwner, Observer { tag ->
            if (tag != null){
                startReadingConfiguration(tag = tag)
                //redoLastOperation(it)
                storeButton.visibility = if (tag == null) View.GONE else View.VISIBLE
            }
        })
        if(deviceID == null) {
            nfcTag2ViewModel.nfcTagId.observe(viewLifecycleOwner, Observer { id ->
                if (id == null) {
                    tag2IdTv.setText(R.string.settings_tagIdUnknown)
                } else {
                    //if(idSmartTag == id) {
                    tag2IdTv.text = id
                    //}else{
                    //incorrectID(id!!)
                    //}
                }
            })
        } else {
            tag2IdTv.text = deviceID
        }
    }

    /*private fun redoLastOperation(tag: Tag) {
        val desideredSettings = tag2SettingsViewModel.desiredSettings.value
        if (desideredSettings != null) { //we have to writeConfiguraiton something
            SmarTag2Service.storeTag2Configuration(requireContext(), tag, desideredSettings)
        } else {
            SmarTag2Service.startReadingTag2Configurations(requireContext(), tag)
        }
    }*/

    private fun startReadingConfiguration(tag: Tag) {
        SmarTag2Service.startReadingTag2Configurations(
            requireContext(),
            tag
        )
    }

    private fun initializeView() {
        tag2IdTv = rootView.findViewById(R.id.tag2_settings_id_label)
        tag2SamplingTimeTv = rootView.findViewById(R.id.tag2_settings_sampling_time_tv)
        tag2RecyclerView = rootView.findViewById(R.id.tag2_settings_rv)
        storeButton = rootView.findViewById(R.id.tag2_settings_store_button)
        //setUpSamplingIntervalView()

        storeButton.setOnClickListener{
            updateTagConfiguration()
        }
    }

    private fun showSnackMessage(msg: String) {
        val rootView = activity?.findViewById<View>(android.R.id.content)
        if (rootView != null)
            Snackbar.make(rootView, msg, Snackbar.LENGTH_SHORT).show()
    }

    private fun updateTagConfiguration(){
        val config = adapter.getVirtualSensorConfiguration()
        if(config == null){
            showSnackMessage("Invalid Configuration!")
        } else {
            val filteredConfig = filterConfiguration(config)
            if (filteredConfig.isNotEmpty()) {
                val incompatibleSensors = checkVirtualSensorsIncompatibility(config, NFCTag2CurrentFw.getCurrentFw())
                if (incompatibleSensors != null) {
                    showIncompatibleVSmessage(incompatibleSensors)
                } else {
                    val smarTag2Configuration = prepareSmarTag2Configuration(filteredConfig)
                    if (smarTag2Configuration != null) {
                        tag2SettingsViewModel.updateSettings(smarTag2Configuration)
                    } else {
                        showSnackMessage("Something went wrong.")
                    }
                }
            } else {
                showSnackMessage("Select at least one sensor")
            }
        }

    }

    private fun showIncompatibleVSmessage(incompatibleSensors: IncompatibleVirtualSensors) {
        var vsName1 = ""
        var vsName2 = ""
        val currentFw = NFCTag2CurrentFw.getCurrentFw()
        currentFw.virtualSensors.forEach { vs ->
            if(vs.id == incompatibleSensors.id1) {
                vsName1 = vs.displayName
            }
        }
        currentFw.virtualSensors.forEach { vs ->
            if(vs.id == incompatibleSensors.id2) {
                vsName2 = vs.displayName
            }
        }
        showSnackMessage("$vsName1 sensor is NOT COMPATIBLE with $vsName2 sensor.")
    }

    private fun checkVirtualSensorsIncompatibility(config: ArrayList<VirtualSensorConfiguration>?, catalog: NfcV2Firmware): IncompatibleVirtualSensors? {
        var invalidVS: IncompatibleVirtualSensors? = null

        config?.forEach { vsConfig ->
            val virtualSensorCatalog = retrieveVirtualSensorFromCatalog(catalog, vsConfig.id)
            virtualSensorCatalog?.incompatibility?.forEach { vsIncompatibleID ->
                val detectedVsIncompatibleID = checkVSIncompatibility(config, vsIncompatibleID.id)
                if (detectedVsIncompatibleID != null){
                    invalidVS = IncompatibleVirtualSensors(vsConfig.id, detectedVsIncompatibleID)
                }
            }
        }

        return invalidVS
    }

    private fun checkVSIncompatibility(config: ArrayList<VirtualSensorConfiguration>?, incompatibleID: Int): Int? {
        var validConfiguration: Int? = null
        config?.forEach { vsConfig ->
            if(vsConfig.id == incompatibleID) {
                validConfiguration = incompatibleID
            }
        }
        return validConfiguration
    }

    private fun retrieveVirtualSensorFromCatalog(catalog: NfcV2Firmware, virtualSensorId: Int): VirtualSensor?{
        catalog.virtualSensors.forEach { vSc ->
            if(vSc.id == virtualSensorId){
                return vSc
            }
        }
        return null
    }

    private fun filterConfiguration(config: ArrayList<VirtualSensorConfiguration>): ArrayList<VirtualSensorConfiguration> {
        val sensorToRemove: ArrayList<VirtualSensorConfiguration> = ArrayList()
        config.forEach { vs ->
            if (!(vs.enabled)){
                sensorToRemove.add(vs)
            }
        }
        sensorToRemove.forEach { vsToRemove ->
            config.remove(vsToRemove)
        }
        return config
    }

    private fun prepareSmarTag2Configuration(virtualSensors: ArrayList<VirtualSensorConfiguration>): SmarTag2Configuration?{
        val currentSmarTag2Config = tag2SettingsViewModel.currentSettings.value
        if (currentSmarTag2Config != null) { //we have to writeConfiguraiton something
            currentSmarTag2Config.smarTag2BaseInformation.sampleTime = tag2SamplingTimeTv.text.toString().toInt()
            currentSmarTag2Config.smarTag2BaseInformation.virtualSensorNumber = virtualSensors.count()
            currentSmarTag2Config.virtualSensors = virtualSensors
        }
        return currentSmarTag2Config
    }
}