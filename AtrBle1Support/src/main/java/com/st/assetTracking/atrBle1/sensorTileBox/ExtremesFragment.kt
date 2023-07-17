package com.st.assetTracking.atrBle1.sensorTileBox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.st.assetTracking.atrBle1.R
import com.st.assetTracking.atrBle1.sensorTileBox.settings.LogDataRepository
import com.st.assetTracking.data.DataSample
import com.st.assetTracking.data.sensorDataSamples
import com.st.assetTracking.threshold.model.AcquisitionExtremes
import com.st.assetTracking.threshold.model.DataExtreme
import com.st.assetTracking.threshold.view.ExtremeDataView
import java.text.SimpleDateFormat
import java.util.*

internal class ExtremesFragment(dataRepository: LogDataRepository) : Fragment(R.layout.activity_extremes_fragment) {

    private val mViewModel by viewModels<ExtremesViewModel> {
        ExtremesViewModel.Factory(dataRepository)
    }

    private lateinit var temperatureView: ExtremeDataView
    private lateinit var humidityView: ExtremeDataView
    private lateinit var pressureView: ExtremeDataView
    private lateinit var vibrationView: ExtremeDataView


    private lateinit var mLoadingView: LinearLayout
    private lateinit var mLoadingProgressBar: ProgressBar
    private lateinit var mLoadingProgressText: TextView
    private lateinit var extremesLayout: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState) ?: return null

        activity?.title = "Extremes Data"

        temperatureView = view.findViewById(R.id.extreme_temperature)
        humidityView = view.findViewById(R.id.extreme_humidity)
        pressureView = view.findViewById(R.id.extreme_pressure)
        vibrationView = view.findViewById(R.id.extreme_vibration)

        extremesLayout = view.findViewById(R.id.data_extremesView)
        mLoadingView = view.findViewById(R.id.data_loadingView)

        mLoadingProgressBar = view.findViewById(R.id.data_loadingProgressBar)
        mLoadingProgressText = view.findViewById(R.id.data_loadingProgressText)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel.logData.observe(viewLifecycleOwner, Observer { progress ->
            when (progress) {
                is LogDataRepository.LoadingProgress.Completed -> {
                    showData(progress.data)
                }
                is LogDataRepository.LoadingProgress.Ongoing -> {
                    updateProgress(progress.progress)
                }
                is LogDataRepository.LoadingProgress.DumpingData -> {
                    showDumpingData()
                }
            }
        })
    }

    private fun showDumpingData() {
        (activity as AtrBleDeviceDetails?)?.disableSettingsData()
        mLoadingView.visibility = View.VISIBLE
        extremesLayout.visibility = View.GONE
        mLoadingProgressBar.isIndeterminate = true
        mLoadingProgressText.setText(R.string.data_dumpingData)
    }

    override fun onStart() {
        super.onStart()
        mViewModel.loadData()
    }

    private fun updateProgress(progress: Float) {
        mLoadingView.visibility = View.VISIBLE
        extremesLayout.visibility = View.GONE
        mLoadingProgressBar.isIndeterminate = false
        mLoadingProgressBar.progress = progress.toInt()
        mLoadingProgressText.text = getString(R.string.data_loadingProgressFormat, progress)
    }

    private fun showData(data: List<DataSample>) {
        (activity as AtrBleDeviceDetails?)?.enableSettingsData()
        mLoadingView.visibility = View.GONE
        extremesLayout.visibility = View.VISIBLE
        val sensorDataSample = data.sensorDataSamples
        if(sensorDataSample.isNotEmpty()){
            mViewModel.calcMinMax(sensorDataSample)
        }
        mViewModel.dataExtreme.observe(viewLifecycleOwner, Observer {
            updateExtremeData(it)
        })
    }

    private fun updateExtremeData(extremes: AcquisitionExtremes?) {
        temperatureView.setExtreme(extremes?.temperature)
        humidityView.setExtreme(extremes?.humidity)
        pressureView.setExtreme(extremes?.pressure)
        vibrationView.setExtreme(extremes?.vibration)
    }

    private fun ExtremeDataView.setExtreme(data: DataExtreme?) {
        if (data == null) {
            visibility = View.GONE
            return
        }
        val dateFormatter = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault())
        visibility = View.VISIBLE
        setMax(data.maxValue, dateFormatter.format(data.maxDate))
        setMin(data.minValue, dateFormatter.format(data.minDate))
    }
}