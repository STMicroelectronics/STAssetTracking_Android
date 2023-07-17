/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *    and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *    conditions and the following disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *    STMicroelectronics company nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *    in a directory whose title begins with st_images may only be used for internal purposes and
 *    shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *    icons, pictures, logos and other images that are provided with the source code in a directory
 *    whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package com.st.nfcSmarTag.v1.tagSettings

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.st.nfcSmarTag.v1.NfcTagViewModel
import com.st.nfcSmarTag.R
import com.st.smartaglibrary.v1.SmarTagService
import com.st.nfcSmarTag.util.InputChecker
import com.st.smartaglibrary.util.getTypeSerializableExtra
import com.st.smartaglibrary.v1.SmarTag
import com.st.smartaglibrary.v1.model.SamplingConfiguration
import com.st.smartaglibrary.v1.model.SensorConfiguration
import com.st.smartaglibrary.v1.model.Threshold


class TagSettingsFragment : androidx.fragment.app.Fragment() {

    private lateinit var smartTag: TagSettingsViewModel
    private lateinit var nfcTagHolder: NfcTagViewModel

    private lateinit var viewContent: View
    private lateinit var samplingIntervalText: TextView
    private lateinit var storeButton: ImageButton
    private lateinit var tagIdLabel:TextView

    private lateinit var useThreshold : CompoundButton
    private lateinit var logOnlyNextSample: CompoundButton


    private lateinit var temperatureConfig: SensorSettingsView
    private lateinit var pressureConfig: SensorSettingsView
    private lateinit var humidityConfig: SensorSettingsView

    private lateinit var accelerationConfig: AccelerationSettingsView

    private lateinit var idSmartTag : String

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        activity?.title = "Settings"

        nfcTagHolder = NfcTagViewModel.create(requireActivity())
        smartTag = TagSettingsViewModel.create(requireActivity())
        initializeTagSettingsObserver()
        initializeNfcTagObserver()
    }

    private fun redoLastOperation(tag: Tag) {
        val desideredSettings = smartTag.desiredSettings.value
        if (desideredSettings != null) { //we have to writeConfiguraiton something
            SmarTagService.storeConfiguration(requireContext(), tag, desideredSettings)
        } else {
            SmarTagService.startReadConfiguration(requireContext(), tag)
        }
    }

    private fun initializeNfcTagObserver() {
        nfcTagHolder.nfcTag.observe(viewLifecycleOwner, Observer {
            if (it != null) redoLastOperation(it)
            storeButton.visibility = if (it == null) View.GONE else View.VISIBLE
        })
        nfcTagHolder.nfcTagId.observe(viewLifecycleOwner, Observer { id ->
            if (id == null) {
                tagIdLabel.setText(R.string.settings_tagIdUnknown)
            } else {
                if(idSmartTag == id) {
                    tagIdLabel.text = id
                }else{
                    incorrectID(id!!)
                }
            }
        })
    }

    private fun incorrectID(id: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Incorrect Board")
        builder.setMessage("The board ID [$id] is different from expected board ID [ $idSmartTag ].\nPlease select the correct board.")

        builder.setPositiveButton(android.R.string.yes) { _, _ ->
            requireActivity().onBackPressed()
        }

        builder.setCancelable(false)

        builder.show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        idSmartTag = requireArguments().getString("idSmartTag").toString()

        viewContent = inflater.inflate(R.layout.fragment_tag_settings, container, false)

        initializeView()
        return viewContent
    }


    private fun initializeTagSettingsObserver() {

        smartTag.currentSettings.observe(viewLifecycleOwner, Observer { newConf ->
            if (newConf != null)
                displaySettings(newConf)
        })

        smartTag.desiredSettings.observe(viewLifecycleOwner, Observer { conf ->
            if (conf != null) {
                writeConfiguraiton(conf)
            }
        })
    }

    private val hideKeyboardWhenFocusIsLost = View.OnFocusChangeListener { _, hasFocus ->
        if(!hasFocus){
            val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
            //Find the currently focused view, so we can grab the correct window token from it.
            val view = activity?.currentFocus
            imm?.hideSoftInputFromWindow(view?.windowToken, 0)
        }
    }

    private fun initializeView() {
        setUpSamplingIntervalView()

        tagIdLabel = viewContent.findViewById(R.id.settings_tagIdLabel)

        storeButton = viewContent.findViewById(R.id.settingsStoreSettings)
        storeButton.setOnClickListener { updateTagConfiguration() }

        temperatureConfig = viewContent.findViewById(R.id.settingsTemperatureConf)
        temperatureConfig.validRange = SmarTag.TEMPERATURE_RANGE_C

        pressureConfig = viewContent.findViewById(R.id.settingsPressureConf)
        pressureConfig.validRange =  SmarTag.PRESSURE_RANGE_MBAR

        humidityConfig = viewContent.findViewById(R.id.settingsHumidityConf)
        humidityConfig.validRange =  SmarTag.HUMIDITY_RANGE

        accelerationConfig = viewContent.findViewById(R.id.settingsAccelerationConf)
        accelerationConfig.maxAccThreshold = SmarTag.ACCELERATION_RANGE_MG.endInclusive

        setUpLogNextSampleButton()
        setUpUseThresholdButton()
    }

    private fun setUpSamplingIntervalView() {
        val samplingIntervalLayout = viewContent.findViewById<TextInputLayout>(R.id.settings_samplingTextLayout)

        samplingIntervalText = viewContent.findViewById(R.id.settings_samplingTextView)
        samplingIntervalText.onFocusChangeListener = hideKeyboardWhenFocusIsLost
        samplingIntervalText.addTextChangedListener(object : InputChecker(samplingIntervalLayout) {
            override fun validate(input: String): Boolean {
                val value = input.toIntOrNull()
                if (value != null)
                    return isValidSamplingInterval(value)
                return false
            }

            override fun getErrorString(): String {
                return getString(R.string.settings_invalidSamplingTime)
            }

        })
    }

    private fun setUpUseThresholdButton(){
        useThreshold = viewContent.findViewById(R.id.settings_logWithThreshold)
        useThreshold.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                logOnlyNextSample.isChecked = false
            }
            temperatureConfig.showThreshold = isChecked
            pressureConfig.showThreshold = isChecked
            humidityConfig.showThreshold = isChecked
            accelerationConfig.enableEvents= isChecked
        }
    }

    private fun setUpLogNextSampleButton(){
        logOnlyNextSample = viewContent.findViewById(R.id.settings_logNextSample)
        logOnlyNextSample.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                useThreshold.isChecked = false
            }
            samplingIntervalText.isEnabled=!isChecked
        }
    }

    private fun getCurrentSettingFromView(view: SensorSettingsView): SensorConfiguration {
        return SensorConfiguration(view.isSensorEnabled, Threshold(view.maxThreshold, view.minThreshold))
    }

    private fun getSamplingMode(): SamplingConfiguration.Mode {
        return when {
            useThreshold.isChecked -> SamplingConfiguration.Mode.SamplingWithThreshold
            logOnlyNextSample.isChecked -> SamplingConfiguration.Mode.SaveNextSample
            else -> SamplingConfiguration.Mode.Sampling
        }
    }

    private fun isValidSensorRange(useRange: Threshold, sensorRange: ClosedRange<Float>):Boolean{
        return (useRange.min ?: Float.MIN_VALUE >= sensorRange.start) &&
               (useRange.max ?: Float.MAX_VALUE <= sensorRange.endInclusive)
    }

    private fun isValidRange(userRange: Threshold):Boolean{
        return (userRange.min?:Float.MIN_VALUE) <= (userRange.max ?: Float.MAX_VALUE)
    }

    private fun showErrorDialog(error: String){
        val context = this.context ?: return
        AlertDialog.Builder(context)
                .setTitle(R.string.settings_invalidRange_title)
                .setMessage(error)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok){ _: DialogInterface, _: Int -> }
                .create()
                .show()
    }

    private fun isValidSensorConfiguration(userRange: Threshold, sensorRange: ClosedRange<Float>,
                                           sensorName: String):Boolean{
        if(!isValidRange(userRange)){
            showErrorDialog(getString(R.string.settings_invalidRange_format, sensorName.toLowerCase()))
            return false
        }
        if(!isValidSensorRange(userRange, sensorRange)){
            showErrorDialog(getString(R.string.settings_outOfRange_format, sensorName.toLowerCase()))
            return false
        }
        return true
    }

    private fun isValidConfiguration(conf: SamplingConfiguration):Boolean{
        return isValidSensorConfiguration(conf.temperatureConf.threshold, SmarTag.TEMPERATURE_RANGE_C, getString(R.string.data_temperature_name)) &&
                isValidSensorConfiguration(conf.pressureConf.threshold, SmarTag.PRESSURE_RANGE_MBAR, getString(R.string.data_pressure_name)) &&
                isValidSensorConfiguration(conf.humidityConf.threshold, SmarTag.HUMIDITY_RANGE, getString(R.string.data_humidity_name)) &&
                isValidSensorConfiguration(conf.accelerometerConf.threshold, SmarTag.ACCELERATION_RANGE_MG, getString(R.string.data_acceleration_name))
    }

    private fun updateTagConfiguration(){
        val conf = getCurrentSettings()
        if(isValidConfiguration(conf))
            smartTag.updateSettings(conf)
    }

    private fun isValidSamplingInterval(interval: Int):Boolean = SmarTag.VALID_SAMPLING_RATE_INTERVAL.contains(interval)

    private fun getSamplingInterval():Int{
        val interval = ((samplingIntervalText.text.toString().toIntOrNull())?.times((60)))
        return interval?.coerceIn(SmarTag.VALID_SAMPLING_RATE_INTERVAL) ?: SmarTag.VALID_SAMPLING_RATE_INTERVAL.first
    }

    private fun getCurrentSettings(): SamplingConfiguration {
        return SamplingConfiguration(
                samplingInterval_s = getSamplingInterval(),
                temperatureConf = getCurrentSettingFromView(temperatureConfig),
                pressureConf = getCurrentSettingFromView(pressureConfig),
                humidityConf = getCurrentSettingFromView(humidityConfig),
                accelerometerConf = SensorConfiguration(accelerationConfig.isSensorEnabled, Threshold(accelerationConfig.accThreshold, null)),
                wakeUpConf = SensorConfiguration(accelerationConfig.isWakeUpEnabled, Threshold(accelerationConfig.accThreshold, null)),
                orientationConf = SensorConfiguration(accelerationConfig.isOrientationEnabled, Threshold(null, null)),
                mode = getSamplingMode())
    }

    private fun setSamplingView(conf: SensorConfiguration,
                                view: SensorSettingsView, showTh: Boolean){
        view.isSensorEnabled = conf.isEnable
        view.showThreshold= showTh
        view.maxThreshold = conf.threshold.max
        view.minThreshold = conf.threshold.min
    }

    private fun displaySettings(configuration: SamplingConfiguration) {
        samplingIntervalText.text = (configuration.samplingInterval_s / 60).toString()
        val enableTh = configuration.mode == SamplingConfiguration.Mode.SamplingWithThreshold
        useThreshold.isChecked=enableTh
        logOnlyNextSample.isChecked = false
        setSamplingView(configuration.temperatureConf, temperatureConfig, enableTh)
        setSamplingView(configuration.pressureConf, pressureConfig, enableTh)
        setSamplingView(configuration.humidityConf, humidityConfig, enableTh)
        accelerationConfig.isSensorEnabled = configuration.accelerometerConf.isEnable
        accelerationConfig.isOrientationEnabled = configuration.orientationConf.isEnable
        accelerationConfig.isWakeUpEnabled = configuration.wakeUpConf.isEnable
        accelerationConfig.accThreshold = configuration.accelerometerConf.threshold.max

    }

    private fun showSnackMessage(msg: String) {
        val rootView = activity?.findViewById<View>(android.R.id.content)
        if (rootView != null)
            Snackbar.make(rootView, msg, Snackbar.LENGTH_SHORT).show()
    }

    private val nfcServiceResponse = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            when (intent?.action) {
                SmarTagService.READ_TAG_CONFIGURATION_ACTION -> {
                    val conf: SamplingConfiguration = intent.getTypeSerializableExtra(SmarTagService.EXTRA_TAG_CONFIGURATION)
                    smartTag.newConfiguration(conf)
                }
            //SmarTagService.WRITE_TAG_START_ACTION -> showProgress(getString(R.string.settings_startWriting))
                SmarTagService.WRITE_TAG_COMPLETE_ACTION -> {
                    showSnackMessage(getString(R.string.settings_writeConfCompleted))
                    smartTag.onSettingsWrote()
                }
                SmarTagService.READ_TAG_ERROR_ACTION,
                SmarTagService.WRITE_TAG_ERROR_ACTION -> {
                    val msg = intent.getStringExtra(SmarTagService.EXTRA_ERROR_STR)
                    nfcTagHolder.nfcTagError(msg!!)
                }
            }
        }
    }

    private fun writeConfiguraiton(conf: SamplingConfiguration) {
        val tag = nfcTagHolder.nfcTag.value
        if (tag != null)
            SmarTagService.storeConfiguration(requireContext(), tag, conf)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(nfcServiceResponse, SmarTagService.getReadWriteConfigurationFilter())
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(nfcServiceResponse)
    }
}