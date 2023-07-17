package com.st.assetTracking.threshold.view.thresholdSelector

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.st.assetTracking.threshold.R
import com.st.assetTracking.threshold.model.SensorThreshold


import com.st.assetTracking.threshold.model.ThresholdSensorType
import com.st.assetTracking.threshold.view.thresholdSelector.ThresholdBuilderViewModel.ViewState

/**
 * create a dialog to create a new threshold,
 * this fragment will display the sensor type selection, and the threshold value input field

class ThresholdBuilderFragment : AppCompatDialogFragment() {

    companion object {
        private const val SENSOR_SELECTION_REQUEST = 1
        private const val SENSOR_THRESHOLD_REQUEST = 2
        private const val WAKEUP_THRESHOLD_REQUEST = 3
        private const val ORIENTATION_THRESHOLD_REQUEST = 4

        private const val SENSOR_THRESHOLD_EXTRA = "SENSOR_THRESHOLD_EXTRA"

        private const val SENSOR_THRESHOLD_EXTRA_LESS = "SENSOR_THRESHOLD_EXTRA_LESS"
        private const val SENSOR_THRESHOLD_EXTRA_GREATER = "SENSOR_THRESHOLD_EXTRA_GREATER"

        private fun wrapThreshold(th: SensorThreshold): Intent {
            return Intent().apply {
                putExtra(SENSOR_THRESHOLD_EXTRA, th)
            }
        }

        private fun wrapThresholdLesserGreater(th1: SensorThreshold, th2: SensorThreshold): Intent {
            return Intent().apply {
                putExtra(SENSOR_THRESHOLD_EXTRA_LESS, th1)
                putExtra(SENSOR_THRESHOLD_EXTRA_GREATER, th2)
            }
        }

        fun extractLessThanThreshold(intent: Intent?): SensorThreshold? {
            return intent?.getParcelableExtra(SENSOR_THRESHOLD_EXTRA_LESS)
        }
        fun extractGreaterThanThreshold(intent: Intent?): SensorThreshold? {
            return intent?.getParcelableExtra(SENSOR_THRESHOLD_EXTRA_GREATER)
        }

        fun extractThreshold(intent: Intent?): SensorThreshold? {
            return intent?.getParcelableExtra(SENSOR_THRESHOLD_EXTRA)
        }

        fun newInstance() = ThresholdBuilderFragment()
    }

    private val viewModel by viewModels<ThresholdBuilderViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        dialog?.setTitle("Set up Sensor")
        return inflater.inflate(R.layout.threshold_builder_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.viewState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                ViewState.SelectSensorType -> viewSensorSelector()
                is ViewState.SelectThreshold -> viewThresholdSelector(state.sensor)
                ViewState.SelectOrientation -> viewOrientationSelector()
                ViewState.SelectWakeUpThreshold -> viewWakeUpThSelector()
                is ViewState.SensorThresholdBuilt -> completeThreshold(state.threshold)
                is ViewState.SensorThresholdLessAndGreaterdBuilt -> completeEnvironmentalThreshold(state.threshold1, state.treshold2)
            }
        })

    }

    private fun completeThreshold(threshold: SensorThreshold) {
        safeTargetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, wrapThreshold(threshold))
        dismiss()
    }

    private fun completeEnvironmentalThreshold(threshold1: SensorThreshold, threshold2: SensorThreshold) {
        safeTargetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, wrapThresholdLesserGreater(threshold1, threshold2))
        dismiss()
    }


    private fun viewWakeUpThSelector() {
        val thSelector = SelectWakeUpThresholdFragment()
        thSelector.setTargetFragment(null, WAKEUP_THRESHOLD_REQUEST)
        childFragmentManager.commit {
            replace(R.id.threshold_builder_content, thSelector)
        }
    }

    private fun viewOrientationSelector() {
        val thSelector = OrientationSelectorFragment()
        thSelector.setTargetFragment(null, ORIENTATION_THRESHOLD_REQUEST)
        childFragmentManager.commit {
            replace(R.id.threshold_builder_content, thSelector)
        }
    }

    private fun viewThresholdSelector(sensor: ThresholdSensorType) {
        val thSelector = SelectThresholdValueFragment.instanceForSensor(sensor)
        thSelector.setTargetFragment(null, SENSOR_THRESHOLD_REQUEST)
        childFragmentManager.commit {
            replace(R.id.threshold_builder_content, thSelector)
        }
    }

    private fun viewSensorSelector() {
        val sensorType = SensorTypeSelectorFragment()
        sensorType.setTargetFragment(null, SENSOR_SELECTION_REQUEST)
        childFragmentManager.commit {
            replace(R.id.threshold_builder_content, sensorType)
        }
    }

    private fun cancelRequest() {
        safeTargetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, null)
        dismiss()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            cancelRequest()
        }
        when (requestCode) {
            SENSOR_SELECTION_REQUEST -> {
                val sensor = SensorTypeSelectorFragment.extractSelectedSensor(data)
                if (sensor != null) {
                    viewModel.selectSensor(sensor)
                }
            }
            SENSOR_THRESHOLD_REQUEST -> {
                val value1 = SelectThresholdValueFragment.extractValue1(data)
                val value2= SelectThresholdValueFragment.extractValue2(data)
                if (value1 != null && value2 != null) {
                    viewModel.selectThreshold(value1, value2)
                } else {
                    cancelRequest()
                }
            }
            ORIENTATION_THRESHOLD_REQUEST -> {
                val thValue = OrientationSelectorFragment.extractOrientation(data)
                if (thValue != null) {
                    viewModel.selectOrientationThreshold(thValue)
                }
            }
            WAKEUP_THRESHOLD_REQUEST -> {
                val thValue = SelectWakeUpThresholdFragment.extractValue(data)
                if (thValue != null) {
                    viewModel.selectWakeThreshold(thValue)
                }
            }
        }
    }

} */
