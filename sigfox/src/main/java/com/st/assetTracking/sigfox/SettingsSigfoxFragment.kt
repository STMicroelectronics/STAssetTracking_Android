package com.st.assetTracking.sigfox

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.st.BlueSTSDK.Node
import com.st.assetTracking.atrBle1.sensorTileBox.settings.*
import com.st.assetTracking.atrBle1.sensorTileBox.util.InfoTresholdsDialogFragment
import com.st.assetTracking.threshold.model.Orientation
import com.st.assetTracking.threshold.model.SensorThreshold
import com.st.assetTracking.threshold.model.ThresholdComparison
import com.st.assetTracking.threshold.model.ThresholdSensorType
import com.st.assetTracking.threshold.view.util.SensorSamplingInputChecker
import com.st.assetTracking.threshold.view.util.shortValue

internal class SettingsSigfoxFragment(settingsRepository: LogSettingsRepository, private val node: Node) : Fragment(R.layout.settings_sigfox_fragment) {

    private lateinit var viewContent: View

    private lateinit var temperatureConfig: BleSensorSettingsView
    private lateinit var pressureConfig: BleSensorSettingsView
    private lateinit var humidityConfig: BleSensorSettingsView

    private lateinit var tiltConfig: BleTiltSettingsView
    private lateinit var wakeUpConfig: BleWakeUpSettingsView

    private lateinit var orientationConfig: BleOrientationSettingsView

    private var nTresholds: Int = 0

    companion object {
        private const val BUILD_NEW_TH = 1
        private const val BUILD_NEW_TH_DIALOG_TAG = "SettingsFragment.BUILD_NEW_TH_DIALOG_TAG"
    }

    private val mViewModel by viewModels<SettingsSigfoxViewModel> {
        SettingsSigfoxViewModel.Factory(settingsRepository)
    }

    private lateinit var mSensorUpdateTextLayout: TextInputLayout
    private lateinit var mCloudSyncTextLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewContent = requireView()

        initView(viewContent)

        setupInfoButton(view)

        setupContentLoadingProgress(view)
        setupSensorUpdateText(view)

        setupThresholdList()

        setupSaveOperationComplete(view)

        val uidLabel = view.findViewById<TextView>(R.id.settings_uidLabel)
        uidLabel.text = node.device.address.toString()

        val fbPlayStop = view.findViewById<FloatingActionButton>(R.id.fb_save_sigfox_config)
        fbPlayStop.setOnClickListener{
            saveOperation(view)
        }

        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * Set info dialog
     */
    private fun setupInfoButton(view: View) {
        view.findViewById<View>(R.id.iv_info_tresholds).setOnClickListener { _ ->
            val infoThDialogBuilder = InfoTresholdsDialogFragment.newInstance()
            infoThDialogBuilder.setTargetFragment(null, BUILD_NEW_TH)
            infoThDialogBuilder.show(childFragmentManager, BUILD_NEW_TH_DIALOG_TAG)
        }
    }

    /**
     * Initialize view
     */
    private fun initView(viewContent: View){
        //nfc -40.0f..85.0f
        temperatureConfig = viewContent.findViewById(R.id.settingsTemperatureConf)
        temperatureConfig.validRange = -20.0f..100.0f

        //nfc 810.0f..1210.0f
        pressureConfig = viewContent.findViewById(R.id.settingsPressureConf)
        pressureConfig.validRange = 500.0f..1260.0f

        humidityConfig = viewContent.findViewById(R.id.settingsHumidityConf)
        humidityConfig.validRange = 0.0f..100.0f

        tiltConfig = viewContent.findViewById(R.id.settingsTiltConf)

        wakeUpConfig = viewContent.findViewById(R.id.settingsWakeUpConf)
        wakeUpConfig.validRange = 1.0f..16.0f

        orientationConfig = viewContent.findViewById(R.id.settingsOrientationConf)

        temperatureConfig.showThreshold = true
        pressureConfig.showThreshold = true
        humidityConfig.showThreshold = true
        tiltConfig.showThreshold = true
        wakeUpConfig.showThreshold = true
        orientationConfig.showThreshold = true
    }

    /**
     * Set sensor reading configuration [Sampling options card]
     */
    private fun setupSensorUpdateText(view: View) {
        mSensorUpdateTextLayout = view.findViewById(R.id.settings_sampling_layout)
        mCloudSyncTextLayout = view.findViewById(R.id.settings_cloud_sync_layout)
        mViewModel.sensorReadingInterval.observe(viewLifecycleOwner, Observer {
            mSensorUpdateTextLayout.shortValue = it
        })

        mSensorUpdateTextLayout.editText?.addTextChangedListener(
                SensorSamplingInputChecker(mSensorUpdateTextLayout)
        )
    }

    /**
     * Set progress bar visibility
     */
    private fun setupContentLoadingProgress(view: View) {
        val progressBar = view.findViewById<View>(R.id.settings_content_loading_bar)
        mViewModel.showContentLoadingBar.observe(viewLifecycleOwner, Observer { show ->
            progressBar.visibility = if (show) {
                View.VISIBLE
            } else {
                View.GONE
            }
        })
    }

    /**
     * Set the configuration read from the board
     */
    private fun setupThresholdList() {
        mViewModel.thresholdListClean.observe(viewLifecycleOwner, Observer { list ->
            println(list)
            list.forEach {
                when (it.sensor) {
                    ThresholdSensorType.Temperature -> {
                        setCurrentSettingToView(temperatureConfig, it)
                    }
                    ThresholdSensorType.Pressure -> {
                        setCurrentSettingToView(pressureConfig, it)
                    }
                    ThresholdSensorType.Humidity -> {
                        setCurrentSettingToView(humidityConfig, it)
                    }
                    ThresholdSensorType.Tilt -> {
                        tiltConfig.isSensorEnabled = true
                    }
                    ThresholdSensorType.WakeUp -> {
                        wakeUpConfig.isSensorEnabled = true
                        wakeUpConfig.maxThreshold = it.threshold
                    }
                    ThresholdSensorType.Orientation -> {
                        orientationConfig.isSensorEnabled = true
                        setUpOrientationAvailable(it)
                    }
                }
            }
        })
    }

    /**
     * Set the configuration of Temperature, Pressure, Humidity thresholds read from board
     */
    private fun setCurrentSettingToView(view: BleSensorSettingsView, sensor: SensorThreshold){
        view.isSensorEnabled = true
        if(sensor.comparison == ThresholdComparison.Less){
            view.minThreshold = sensor.threshold
        }else{
            view.maxThreshold = sensor.threshold
        }
    }

    /**
     * Set the configuration of Orientation thresholds read from board
     */
    private fun  setUpOrientationAvailable(sensor: SensorThreshold) {
        when (Orientation.fromRawValue(sensor.threshold.toInt().toShort())) {
            Orientation.TOP_LEFT -> {
                orientationConfig.findViewById<CheckBox>(com.st.assetTracking.atrBle1.R.id.cb_top_left).isChecked = true
            }
            Orientation.TOP_RIGHT -> {
                orientationConfig.findViewById<CheckBox>(com.st.assetTracking.atrBle1.R.id.cb_top_right).isChecked = true
            }
            Orientation.BOTTOM_LEFT -> {
                orientationConfig.findViewById<CheckBox>(com.st.assetTracking.atrBle1.R.id.cb_bottom_left).isChecked = true
            }
            Orientation.BOTTOM_RIGHT -> {
                orientationConfig.findViewById<CheckBox>(com.st.assetTracking.atrBle1.R.id.cb_bottom_right).isChecked = true
            }
            Orientation.UP -> {
                orientationConfig.findViewById<CheckBox>(com.st.assetTracking.atrBle1.R.id.cb_up).isChecked = true
            }
            Orientation.DOWN -> {
                orientationConfig.findViewById<CheckBox>(com.st.assetTracking.atrBle1.R.id.cb_down).isChecked = true
            }
            else -> Toast.makeText(requireContext(), "No orientation recognized", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     *  Showing message relative to saving configuration
     */
    private fun setupSaveOperationComplete(view: View) {
        mViewModel.saveOperationSuccess.observe(viewLifecycleOwner, Observer { success ->
            if (success) {
                Snackbar.make(view, R.string.settings_save_completeSuccess, Snackbar.LENGTH_SHORT).show()
            }else {

                Snackbar.make(view, R.string.settings_save_completeError, Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveOperation(view: View) {
        /**
         * Save configuration
         */

        mViewModel.clearConfiguration()

        var checkSettingsOk: Boolean
        view.let { checkSettingsOk = checkNewConfigurationSaved() }

        if(checkSettingsOk) {
            if (nTresholds > 10) {
                Snackbar.make(view, "Too many thresholds set", Snackbar.LENGTH_SHORT).show()
                nTresholds = 0
            } else {
                mViewModel.saveCurrentSettings(mSensorUpdateTextLayout.shortValue, mCloudSyncTextLayout.shortValue)
                nTresholds = 0
            }
        }

    }

    private fun checkNewConfigurationSaved() : Boolean{

        if(nullSensorThresholds()){
            return false
        }else{
            getCurrentSettingFromViewSensor(temperatureConfig, ThresholdSensorType.Temperature)
            getCurrentSettingFromViewSensor(pressureConfig, ThresholdSensorType.Pressure)
            getCurrentSettingFromViewSensor(humidityConfig, ThresholdSensorType.Humidity)

            getCurrentSettingFromViewTilt(tiltConfig)
            getCurrentSettingFromViewWakeUp(wakeUpConfig)

            getCurrentSettingFromViewOrientation(orientationConfig)
            return true
        }
    }

    private fun getCurrentSettingFromViewSensor(view: BleSensorSettingsView, sensor: ThresholdSensorType){
        if(view.isSensorEnabled) {
            mViewModel.addThreshold(SensorThreshold(sensor, ThresholdComparison.Less, view.minThreshold!!))
            mViewModel.addThreshold(SensorThreshold(sensor, ThresholdComparison.BiggerOrEqual, view.maxThreshold!!))
            nTresholds += 2
        }
    }

    private fun getCurrentSettingFromViewTilt(view: BleTiltSettingsView){
        if(view.isSensorEnabled){
            mViewModel.addThreshold(SensorThreshold.tiltThreshold())
            nTresholds += 1
        }
    }

    private fun getCurrentSettingFromViewWakeUp(view: BleWakeUpSettingsView){
        if(view.isSensorEnabled){
            mViewModel.addThreshold(SensorThreshold.wakeUpThreshold(view.maxThreshold!!))
            nTresholds += 1
        }
    }

    private fun getCurrentSettingFromViewOrientation(view: BleOrientationSettingsView){
        if(view.isSensorEnabled){
            val orientationValueAvailable = checkOrientationAvailable(view)
            orientationValueAvailable.forEach{
                mViewModel.addThreshold(SensorThreshold.orientationThreshold(it))
            }
            nTresholds += orientationValueAvailable.size
        }
    }


    private fun  checkOrientationAvailable(view: BleOrientationSettingsView) : ArrayList<Orientation>{
        val orientationValueAvailable = ArrayList<Orientation>()

        if(view.findViewById<CheckBox>(com.st.assetTracking.atrBle1.R.id.cb_top_left).isChecked) { orientationValueAvailable.add(Orientation.TOP_LEFT) }
        if(view.findViewById<CheckBox>(com.st.assetTracking.atrBle1.R.id.cb_top_right).isChecked) { orientationValueAvailable.add(Orientation.TOP_RIGHT) }
        if(view.findViewById<CheckBox>(com.st.assetTracking.atrBle1.R.id.cb_bottom_left).isChecked) { orientationValueAvailable.add(Orientation.BOTTOM_LEFT) }
        if(view.findViewById<CheckBox>(com.st.assetTracking.atrBle1.R.id.cb_bottom_right).isChecked) { orientationValueAvailable.add(Orientation.BOTTOM_RIGHT) }
        if(view.findViewById<CheckBox>(com.st.assetTracking.atrBle1.R.id.cb_up).isChecked) { orientationValueAvailable.add(Orientation.UP) }
        if(view.findViewById<CheckBox>(com.st.assetTracking.atrBle1.R.id.cb_down).isChecked) { orientationValueAvailable.add(Orientation.DOWN) }


        return orientationValueAvailable
    }

    private fun nullSensorThresholds() : Boolean{
        if(temperatureConfig.isSensorEnabled){
            if(temperatureConfig.minThreshold == null || temperatureConfig.maxThreshold == null){
                Snackbar.make(requireView(), "Enter the range values for selected sensor", Snackbar.LENGTH_SHORT).show()
                return true
            }
        }
        if(pressureConfig.isSensorEnabled){
            if(pressureConfig.minThreshold == null || pressureConfig.maxThreshold == null){
                Snackbar.make(requireView(), "Enter the range values for selected sensor", Snackbar.LENGTH_SHORT).show()
                return true
            }
        }
        if(humidityConfig.isSensorEnabled){
            if(humidityConfig.minThreshold == null || humidityConfig.maxThreshold == null){
                Snackbar.make(requireView(), "Enter the range values for selected sensor", Snackbar.LENGTH_SHORT).show()
                return true
            }
        }
        if(wakeUpConfig.isSensorEnabled){
            if(wakeUpConfig.maxThreshold == null){
                Snackbar.make(requireView(), "Enter the value for selected sensor", Snackbar.LENGTH_SHORT).show()
                return true
            }
        }
        if(orientationConfig.isSensorEnabled){
            if(!(orientationConfig.findViewById<CheckBox>(com.st.assetTracking.atrBle1.R.id.cb_top_left).isChecked || orientationConfig.findViewById<CheckBox>(com.st.assetTracking.atrBle1.R.id.cb_top_right).isChecked ||
                            orientationConfig.findViewById<CheckBox>(com.st.assetTracking.atrBle1.R.id.cb_bottom_left).isChecked || orientationConfig.findViewById<CheckBox>(com.st.assetTracking.atrBle1.R.id.cb_bottom_right).isChecked ||
                            orientationConfig.findViewById<CheckBox>(com.st.assetTracking.atrBle1.R.id.cb_up).isChecked || orientationConfig.findViewById<CheckBox>(com.st.assetTracking.atrBle1.R.id.cb_down).isChecked)){
                Snackbar.make(requireView(), "Select at least one Orientation sensor", Snackbar.LENGTH_SHORT).show()
                return true
            }
        }
        return false
    }

}