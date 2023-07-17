package com.st.assetTracking.management.deviceData

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.st.assetTracking.dashboard.model.DeviceData
import com.st.assetTracking.dashboard.persistance.DeviceDataRepository
import com.st.assetTracking.dashboard.util.LoadingView
import com.st.assetTracking.data.eventDataSamples
import com.st.assetTracking.data.sensorDataSamples
import com.st.assetTracking.data.ui.EventDataListFragment
import com.st.assetTracking.data.ui.SensorDataPlotFragment
import kotlin.time.ExperimentalTime
import com.st.assetTracking.R

@ExperimentalTime
internal class SingleDeviceFragment(deviceDataRepository: DeviceDataRepository) : Fragment() {

    private val mDeviceViewModel by viewModels<DeviceDataViewModel> {
        DeviceDataViewModel.Factory(deviceDataRepository, null, null)
    }

    private lateinit var mLoadingView: LoadingView

    private lateinit var mDataView: View
    private lateinit var mTabLayout: TabLayout
    private lateinit var mViewPager: ViewPager2
    private lateinit var mToggleButton: MaterialButtonToggleGroup

    private var mTabLayoutMediator: TabLayoutMediator? = null

    private val strOffline: String = "You are offline. Please check your connectivity"

    private var networkConnection: Boolean = false

    private fun showSnackbar(message: String) {
        Snackbar.make(requireView(),
                message,
                Snackbar.LENGTH_SHORT)
                .show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_single_device, container, false)

        networkConnection= true

        mLoadingView = rootView.findViewById(R.id.single_device_loadingView)

        mDataView = rootView.findViewById(R.id.single_device_dataView)
        mViewPager = rootView.findViewById(R.id.single_device_viewPager)
        mTabLayout = rootView.findViewById(R.id.single_device_TabLayout)

        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.let {
            try{
                it.registerDefaultNetworkCallback(@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        //take action when network connection is gained
                        networkConnection = true
                    }
                    override fun onLost(network: Network) {
                        //take action when network connection is lost
                        networkConnection = false
                    }
                })
            }catch (e: NoSuchMethodError){ networkConnection = true }
        }

        mDeviceViewModel.deviceDataStatus.observe(viewLifecycleOwner, Observer { status ->
            manageDeviceDataStatus(status)
        })

        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val timingEditor: SharedPreferences.Editor = requireContext().getSharedPreferences("Timing", Context.MODE_PRIVATE).edit()

        mToggleButton = view.findViewById(R.id.dashboardToggleGroup)
        mToggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked)// run only when the button is selected
                return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.single_device_load_3hData -> {
                    if(networkConnection){
                        mDeviceViewModel.getDataFromLast3Hours(false)
                        timingEditor.putString("time", "3h")
                        timingEditor.apply()
                    }else{
                        showSnackbar(strOffline)
                    }
                }
                R.id.single_device_load_6hData -> {
                    if(networkConnection){
                        mDeviceViewModel.getDataFromLast6Hours(false)
                        timingEditor.putString("time", "6h")
                        timingEditor.apply()
                    }else{
                        showSnackbar(strOffline)
                    }
                }
                R.id.single_device_load_1dData -> {
                    if(networkConnection) {
                        mDeviceViewModel.getDataFromLast24Hours(false)
                        timingEditor.putString("time", "1d")
                        timingEditor.apply()
                    }else{
                        showSnackbar(strOffline)
                    }
                }
                R.id.single_device_load_2dData -> {
                    if(networkConnection) {
                        mDeviceViewModel.getDataFromLast48Hours(false)
                        timingEditor.putString("time", "2d")
                        timingEditor.apply()
                    }else{
                        showSnackbar(strOffline)
                    }
                }
                R.id.single_device_load_7dData -> {
                    if(networkConnection) {
                        mDeviceViewModel.getDataFromLastWeek(false)
                        timingEditor.putString("time", "7d")
                        timingEditor.apply()
                    }else{
                        showSnackbar(strOffline)
                    }
                }
            }
        }

        mToggleButton.check(R.id.single_device_load_1dData)
        timingEditor.putString("time", "1d")
        timingEditor.apply()

        super.onActivityCreated(savedInstanceState)
    }

    private fun manageDeviceDataStatus(status: DeviceDataRepository.Result) {
        when (status) {
            is DeviceDataRepository.Result.Loading -> {
                mDataView.visibility = View.GONE
                mLoadingView.loadingText = getString(R.string.singleDevice_loadingData)
                mLoadingView.visibility = View.VISIBLE
            }
            is DeviceDataRepository.Result.Error -> {
                mDataView.visibility = View.GONE
                mLoadingView.loadingText = getString(R.string.singleDevice_errorLoadingData)
                mLoadingView.visibility = View.VISIBLE
            }
            is DeviceDataRepository.Result.Complete -> {
                showData(status.data)
            }
            is DeviceDataRepository.Result.Partial -> {
                showData(status.data)
            }
        }
    }


    private fun showData(deviceData: DeviceData) {
        val adapter = ViewPageAdapter(this, deviceData)
        mViewPager.adapter = adapter
        mTabLayoutMediator = TabLayoutMediator(mTabLayout, mViewPager) { tab, position ->
            tab.text = getString(adapter.getTabName(position))
        }.apply {
            attach()
        }

        mDataView.visibility = View.VISIBLE
        mLoadingView.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mViewPager.adapter = null
        mTabLayoutMediator?.detach()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().finish()
    }

}

private class ViewPageAdapter(fragment: Fragment, private val deviceData: DeviceData) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment =
            when (position) {
                0 -> SensorDataPlotFragment.createWith(deviceData.telemetryData.sensorDataSamples, null)
                1 -> SingleDeviceLocationFragment.newInstance(deviceData.locationData)
                2 -> EventDataListFragment.createWith(deviceData.telemetryData.eventDataSamples)
                else -> throw  IllegalArgumentException("Invalid position: $position")
            }

    @StringRes
    fun getTabName(position: Int): Int = when (position) {
        0 -> R.string.singleDevice_telemetryTab
        1 -> R.string.singleDevice_locationTab
        2 -> R.string.singleDevice_eventsTab
        else -> throw  IllegalArgumentException("Invalid position: $position")
    }
}