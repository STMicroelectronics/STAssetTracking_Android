package com.st.assetTracking.atrBle1.sensorTileBox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.st.assetTracking.atrBle1.R
import com.st.assetTracking.atrBle1.sensorTileBox.settings.LogDataRepository
import com.st.assetTracking.dashboard.AssetTrackingUploadData
import com.st.assetTracking.data.DataSample
import com.st.assetTracking.data.eventDataSamples
import com.st.assetTracking.data.sensorDataSamples
import com.st.assetTracking.data.ui.EventDataListFragment
import com.st.assetTracking.data.ui.SensorDataPlotFragment
import com.st.login.Configuration
import com.st.login.LoginManager
import com.st.login.loginprovider.LoginProviderFactory
import java.lang.IllegalArgumentException

internal class DataFragment(dataRepository: LogDataRepository) : Fragment(R.layout.data_fragment) {

    private val mViewModel by viewModels<DataViewModel> {
        DataViewModel.Factory(dataRepository)
    }

    private lateinit var mViewPager: ViewPager2
    private lateinit var mTabLayout: TabLayout
    private var mTabLayoutMediator: TabLayoutMediator? = null

    private lateinit var mLoadingView: View
    private lateinit var mLoadingProgressBar: ProgressBar
    private lateinit var mLoadingProgressText: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState) ?: return null

        activity?.title = "Samples"

        mViewPager = view.findViewById(R.id.data_viewPager)
        mTabLayout = view.findViewById(R.id.data_tabLayout)

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
        (activity as AtrBleDeviceDetails?)?.disableSettingsMinMax()
        mLoadingView.visibility = View.VISIBLE
        mLoadingProgressBar.isIndeterminate = true
        mLoadingProgressText.setText(R.string.data_dumpingData)
    }

    override fun onStart() {
        super.onStart()
        mViewModel.loadData()
    }


    private fun updateProgress(progress: Float) {
        mLoadingView.visibility = View.VISIBLE
        mLoadingProgressBar.isIndeterminate = false
        mLoadingProgressBar.progress = progress.toInt()
        mLoadingProgressText.text = getString(R.string.data_loadingProgressFormat, progress)
    }

    private fun showData(data: List<DataSample>) {
        (activity as AtrBleDeviceDetails?)?.enableSettingsMinMax()
        val adapter = DataFragmentAdapter(this, data)
        mLoadingView.visibility = View.GONE
        mViewPager.adapter = adapter
        mTabLayoutMediator = TabLayoutMediator(mTabLayout, mViewPager) { tab, position ->
            tab.text = getString(adapter.getItemTitleId(position))
        }.apply {
            attach()
        }
        if (mViewModel.askToSyncData) {
            askToSyncData(mViewModel.deviceId, data)
        }
    }

    private fun askToSyncData(deviceId: String, data: List<DataSample>) {
        val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.data_syncDataTitle)
                .setMessage(R.string.data_syncDataMessage)
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

    private fun syncData(deviceId: String, data: List<DataSample>) {
        val loginManager = LoginManager(
            requireActivity().activityResultRegistry,
            requireActivity() as AppCompatActivity,
            requireContext(),
            LoginProviderFactory.LoginProviderType.COGNITO,
            Configuration.getInstance(requireContext(), com.st.login.R.raw.auth_config_cognito)
        )

        loginManager.getAtrAuthData { authData ->
            if (authData != null) {
                AssetTrackingUploadData.startActivityToUploadDataFrom(authData, requireContext(), deviceId, "ble", data, "ble")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mViewPager.adapter = null
        mTabLayoutMediator?.detach()
    }

    private class DataFragmentAdapter(parent: Fragment, data: List<DataSample>) : FragmentStateAdapter(parent) {

        private val sensorDataSample = data.sensorDataSamples
        private val eventDataSample = data.eventDataSamples

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SensorDataPlotFragment.createWith(sensorDataSample, true)
                1 -> EventDataListFragment.createWith(eventDataSample)
                else -> throw IllegalArgumentException("Invalid position index")
            }
        }

        fun getItemTitleId(position: Int): Int {
            return when (position) {
                0 -> R.string.data_sensorTabName
                1 -> R.string.data_eventTabName
                else -> throw IllegalArgumentException("Invalid position index")
            }
        }

    }


}