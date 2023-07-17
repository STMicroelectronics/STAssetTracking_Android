package com.st.assetTracking.atrBle1.sensorTileBox

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.st.BlueSTSDK.Node
import com.st.assetTracking.atrBle1.R
import com.st.assetTracking.atrBle1.sensorTileBox.settings.*
import com.st.assetTracking.atrBle1.sensorTileBox.util.InfoTresholdsDialogFragment
import com.st.assetTracking.threshold.model.Orientation
import com.st.assetTracking.threshold.model.SensorThreshold
import com.st.assetTracking.threshold.model.ThresholdComparison
import com.st.assetTracking.threshold.model.ThresholdSensorType
import com.st.assetTracking.threshold.view.util.SensorSamplingInputChecker
import com.st.assetTracking.threshold.view.util.shortValue


internal class SettingsFragment(settingsRepository: LogSettingsRepository, private val node: Node, private val deviceID: String) : Fragment(
    R.layout.settings_fragment
) {

    private lateinit var viewContent: View

    private lateinit var temperatureConfig: BleSensorSettingsView
    private lateinit var pressureConfig: BleSensorSettingsView
    private lateinit var humidityConfig: BleSensorSettingsView

    private lateinit var tiltConfig: BleTiltSettingsView
    private lateinit var wakeUpConfig: BleWakeUpSettingsView

    private lateinit var orientationConfig: BleOrientationSettingsView

    private var playRecording: Boolean = false
    private var nTresholds: Int = 0

    private var animRotateButton: Animation? = null

    companion object {
        private const val BUILD_NEW_TH = 1
        private const val BUILD_NEW_TH_DIALOG_TAG = "SettingsFragment.BUILD_NEW_TH_DIALOG_TAG"
    }

    private val mViewModel by viewModels<SettingsViewModel> {
        SettingsViewModel.Factory(node, settingsRepository)
    }

    private lateinit var mSensorUpdateTextLayout: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        activity?.title = "Settings"

        animRotateButton = AnimationUtils.loadAnimation(requireContext(), com.st.BlueSTSDK.gui.R.anim.fab_rotate)

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        var fw = ""

        /**
         * Check if SensorTile.Box ha ATR-BLE-1 fw
         */
        mViewModel.atrFirmware.observe(viewLifecycleOwner, Observer { atrFw ->
            if (!atrFw) {
                requireActivity().onBackPressed()
                Toast.makeText(requireContext(), "Incorrect firmware", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            }
        })

        /**
         * Setup UID information
         */
        mViewModel.uid.observe(viewLifecycleOwner, Observer { uid ->
            val uidLabel = requireView().findViewById<TextView>(R.id.settings_uidLabel)
            uidLabel.text = uid
            if(deviceID != uid){
                incorrectID(uid, deviceID)
            }
        })


        /**
         * Enable bottom navigationView when finish BLE initialization
         */
        mViewModel.showContentLoadingBar.observe(viewLifecycleOwner, Observer { loadingBar ->
            if (!loadingBar) {
                (activity as AtrBleDeviceDetails?)?.finishBLEInitialization()
            }
        })

        viewContent = requireView()

        initView(viewContent)

        setupInfoButton(view)

        setupContentLoadingProgress(view)
        setupSensorUpdateText(view)

        setupThresholdList()

        setupSaveOperationComplete(view)
        setupLoggingStateSwitchFButton(view)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun incorrectID(uid: String, deviceID: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Incorrect Board")
        builder.setMessage("The board ID [$deviceID] is different from expected board ID [ $uid ].\nPlease select the correct board.")

        builder.setPositiveButton(android.R.string.yes) { _, _ ->
            requireActivity().onBackPressed()
        }

        builder.setCancelable(false)

        builder.show()
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
        wakeUpConfig.validRange = 1000.0000f..16000.0000f

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
                        wakeUpConfig.maxThreshold = it.threshold * 1000.0000f //(1-16)g <--> (1000-16000)mg
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
                orientationConfig.findViewById<CheckBox>(R.id.cb_top_left).isChecked = true
            }
            Orientation.TOP_RIGHT -> {
                orientationConfig.findViewById<CheckBox>(R.id.cb_top_right).isChecked = true
            }
            Orientation.BOTTOM_LEFT -> {
                orientationConfig.findViewById<CheckBox>(R.id.cb_bottom_left).isChecked = true
            }
            Orientation.BOTTOM_RIGHT -> {
                orientationConfig.findViewById<CheckBox>(R.id.cb_bottom_right).isChecked = true
            }
            Orientation.UP -> {
                orientationConfig.findViewById<CheckBox>(R.id.cb_up).isChecked = true
            }
            Orientation.DOWN -> {
                orientationConfig.findViewById<CheckBox>(R.id.cb_down).isChecked = true
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
                /**
                 * Play Recording
                 */
                val tvStatusBoard = view.findViewById<TextView>(R.id.recordingStatusBoard)
                val fbPlayStop = view.findViewById<FloatingActionButton>(R.id.fb_startStop)
                //fbPlayStop.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#7F0000"))
                tvStatusBoard.background = ContextCompat.getDrawable(requireContext(),
                    R.drawable.tv_log_border
                )
                tvStatusBoard.text = "Logging data"
                tvStatusBoard.setTextColor(ContextCompat.getColor(requireContext(), com.st.assetTracking.data.ui.R.color.colorRecording))
                playRecording = true
                fbPlayStop.setImageResource(R.drawable.ic_stop)
                fbPlayStop.startAnimation(animRotateButton)
            } else {

                Snackbar.make(view, R.string.settings_save_completeError, Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Setup state of RECORDING status read from Configuration of board.
     */
    private fun setupLoggingStateSwitchFButton(view: View) {
        val fbPlayStop = view.findViewById<FloatingActionButton>(R.id.fb_startStop)
        val tvStatusBoard = view.findViewById<TextView>(R.id.recordingStatusBoard)

        mViewModel.boardIsLogging.observe(viewLifecycleOwner, Observer { isLogging ->
            if (isLogging) {
                playRecording = true
                tvStatusBoard.text = "Logging data"
                tvStatusBoard.setTextColor(ContextCompat.getColor(requireContext(), com.st.assetTracking.data.ui.R.color.colorRecording))
                tvStatusBoard.background = ContextCompat.getDrawable(requireContext(),
                    R.drawable.tv_log_border
                )
                fbPlayStop.setImageResource(R.drawable.ic_stop)
                fbPlayStop.startAnimation(animRotateButton)
            }
        })

        fbPlayStop.setOnClickListener {
            refreshPlayStopLayout(view)
        }
    }

    /**
     * Manage PLAY, STOP Recording and SAVE Configuration
     */
    private fun refreshPlayStopLayout(view: View){
        val fbPlayStop = view.findViewById<FloatingActionButton>(R.id.fb_startStop)
        var tvStatusBoard = view.findViewById<TextView>(R.id.recordingStatusBoard)

        if(playRecording){
            /**
             * Stop Recording
             */
            tvStatusBoard.text = "Idle"
            tvStatusBoard.background = ContextCompat.getDrawable(requireContext(),
                R.drawable.tv_no_log_border
            )
            tvStatusBoard.setTextColor(ContextCompat.getColor(requireContext(), com.st.assetTracking.data.ui.R.color.colorIdle))
            playRecording = false
            mViewModel.disableLogging()

            fbPlayStop.setImageResource(R.drawable.ic_play)
            fbPlayStop.startAnimation(animRotateButton)
        }else{
            /**
             * Save configuration and START RECORDING
             */
            mViewModel.clearConfiguration()

            var checkSettingsOk : Boolean
            view.let { checkSettingsOk = checkNewConfigurationSaved() }

            if(checkSettingsOk){
                if(nTresholds>10){
                    Snackbar.make(view, "Too many thresholds set", Snackbar.LENGTH_SHORT).show()
                    nTresholds = 0
                }else{
                    mViewModel.saveCurrentSettings(mSensorUpdateTextLayout.shortValue)
                    nTresholds = 0
                }
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
            //(1-16)g <--> (1000-16000)mg
            mViewModel.addThreshold(SensorThreshold.wakeUpThreshold((view.maxThreshold!!) / 1000.000f))
            nTresholds += 1
        }
    }

    private fun getCurrentSettingFromViewOrientation(view: BleOrientationSettingsView){
        val orientationValueAvailable: ArrayList<Orientation>
        if(view.isSensorEnabled){
            orientationValueAvailable = checkOrientationAvailable(view)
            orientationValueAvailable.forEach{
                mViewModel.addThreshold(SensorThreshold.orientationThreshold(it))
            }
            nTresholds += orientationValueAvailable.size
        }
    }


    private fun  checkOrientationAvailable(view: BleOrientationSettingsView) : ArrayList<Orientation>{
        val orientationValueAvailable = ArrayList<Orientation>()

        if(view.findViewById<CheckBox>(R.id.cb_top_left).isChecked) { orientationValueAvailable.add(Orientation.TOP_LEFT) }
        if(view.findViewById<CheckBox>(R.id.cb_top_right).isChecked) { orientationValueAvailable.add(Orientation.TOP_RIGHT) }
        if(view.findViewById<CheckBox>(R.id.cb_bottom_left).isChecked) { orientationValueAvailable.add(Orientation.BOTTOM_LEFT) }
        if(view.findViewById<CheckBox>(R.id.cb_bottom_right).isChecked) { orientationValueAvailable.add(Orientation.BOTTOM_RIGHT) }
        if(view.findViewById<CheckBox>(R.id.cb_up).isChecked) { orientationValueAvailable.add(Orientation.UP) }
        if(view.findViewById<CheckBox>(R.id.cb_down).isChecked) { orientationValueAvailable.add(Orientation.DOWN) }


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
            if(!(orientationConfig.findViewById<CheckBox>(R.id.cb_top_left).isChecked || orientationConfig.findViewById<CheckBox>(
                    R.id.cb_top_right
                ).isChecked ||
                    orientationConfig.findViewById<CheckBox>(R.id.cb_bottom_left).isChecked || orientationConfig.findViewById<CheckBox>(
                    R.id.cb_bottom_right
                ).isChecked ||
                    orientationConfig.findViewById<CheckBox>(R.id.cb_up).isChecked || orientationConfig.findViewById<CheckBox>(
                    R.id.cb_down
                ).isChecked)){
                Snackbar.make(requireView(), "Select at least one Orientation sensor", Snackbar.LENGTH_SHORT).show()
                return true
            }
        }
        return false
    }

}