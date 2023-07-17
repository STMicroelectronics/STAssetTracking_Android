package com.st.assetTracking.management.genericData

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.st.assetTracking.R
import com.st.assetTracking.dashboard.model.Device
import com.st.assetTracking.dashboard.model.DeviceGenericData
import com.st.assetTracking.dashboard.persistance.DeviceDataRepository
import com.st.assetTracking.dashboard.util.LoadingView
import com.st.assetTracking.data.ui.genericDataUI.GenericDataListFragment
import com.st.assetTracking.data.ui.genericDataUI.GenericDataPlotFragment
import com.st.assetTracking.management.deviceData.DeviceDataViewModel
import com.st.assetTracking.management.deviceData.SingleDeviceLocationFragment
import com.st.smartaglibrary.v2.catalog.NFCBoardCatalogService
import com.st.smartaglibrary.v2.catalog.NfcV2BoardCatalog
import com.st.smartaglibrary.v2.catalog.NfcV2Firmware
import kotlinx.coroutines.runBlocking
import kotlin.time.ExperimentalTime

@ExperimentalTime
internal class DeviceGenericDataFragment(val device: Device, deviceDataRepository: DeviceDataRepository) : Fragment() {

    private val mDeviceViewModel by viewModels<DeviceDataViewModel> {
        DeviceDataViewModel.Factory(deviceDataRepository, device.boardID, device.firmwareID)
    }

    private lateinit var mLoadingView: LoadingView

    private lateinit var mDataView: View
    private lateinit var mTabLayout: TabLayout
    private lateinit var mViewPager: ViewPager2
    private lateinit var mToggleButton: MaterialButtonToggleGroup

    private var mTabLayoutMediator: TabLayoutMediator? = null

    private val strOffline: String = "You are offline. Please check your connectivity"
    private var networkConnection: Boolean = false

    private var currentFw: NfcV2Firmware? = null
    private var timing: Int = 1

    private fun showSnackbar(message: String) {
        Snackbar.make(requireView(),
            message,
            Snackbar.LENGTH_SHORT)
            .show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_device_generic_data, container, false)

        networkConnection= true

        mLoadingView = rootView.findViewById(R.id.device_generic_loadingView)

        mDataView = rootView.findViewById(R.id.device_generic_dataView)
        mViewPager = rootView.findViewById(R.id.device_generic_viewPager)
        mTabLayout = rootView.findViewById(R.id.device_generic_TabLayout)

        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.let {
            try{
                it.registerDefaultNetworkCallback(
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

        mDeviceViewModel.deviceGenericDataStatus.observe(viewLifecycleOwner, Observer { status ->
            manageDeviceDataStatus(status)
        })

        if(device.boardID != null && device.firmwareID != null) {
            retrieveNfcCatalog()
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val timingEditor: SharedPreferences.Editor = requireContext().getSharedPreferences("Timing", Context.MODE_PRIVATE).edit()

        mToggleButton = view.findViewById(R.id.device_generic_toggle_group)
        mToggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked)// run only when the button is selected
                return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.device_generic_load_3hData -> {
                    if(networkConnection){
                        mDeviceViewModel.getDataFromLast3Hours(true)
                        timingEditor.putString("time", "3h")
                        timingEditor.apply()
                        timing = 3
                    }else{
                        showSnackbar(strOffline)
                    }
                }
                R.id.device_generic_load_6hData -> {
                    if(networkConnection){
                        mDeviceViewModel.getDataFromLast6Hours(true)
                        timingEditor.putString("time", "6h")
                        timingEditor.apply()
                        timing = 6
                    }else{
                        showSnackbar(strOffline)
                    }
                }
                R.id.device_generic_load_1dData -> {
                    if(networkConnection) {
                        mDeviceViewModel.getDataFromLast24Hours(true)
                        timingEditor.putString("time", "1d")
                        timingEditor.apply()
                        timing = 1
                    }else{
                        showSnackbar(strOffline)
                    }
                }
                R.id.device_generic_load_2dData -> {
                    if(networkConnection) {
                        mDeviceViewModel.getDataFromLast48Hours(true)
                        timingEditor.putString("time", "2d")
                        timingEditor.apply()
                        timing = 2
                    }else{
                        showSnackbar(strOffline)
                    }
                }
                R.id.device_generic_load_7dData -> {
                    if(networkConnection) {
                        mDeviceViewModel.getDataFromLastWeek(true)
                        timingEditor.putString("time", "7d")
                        timingEditor.apply()
                        timing = 7
                    }else{
                        showSnackbar(strOffline)
                    }
                }
            }
        }

        mToggleButton.check(R.id.device_generic_load_1dData)
        timingEditor.putString("time", "1d")
        timingEditor.apply()

        super.onActivityCreated(savedInstanceState)
    }

    private fun manageDeviceDataStatus(status: DeviceDataRepository.GenericResult) {
        when (status) {
            is DeviceDataRepository.GenericResult.Loading -> {
                mDataView.visibility = View.GONE
                mLoadingView.loadingText = getString(R.string.singleDevice_loadingData)
                mLoadingView.visibility = View.VISIBLE
            }
            is DeviceDataRepository.GenericResult.Error -> {
                mDataView.visibility = View.GONE
                mLoadingView.loadingText = getString(R.string.singleDevice_errorLoadingData)
                mLoadingView.visibility = View.VISIBLE
            }
            is DeviceDataRepository.GenericResult.Complete -> {
                showData(status.data)
            }
            is DeviceDataRepository.GenericResult.Partial -> {
                showData(status.data)
            }
        }
    }

    private fun retrieveNfcCatalog() {
        var nfcCatalog: NfcV2BoardCatalog? = null
        runBlocking {
            nfcCatalog = NFCBoardCatalogService().getNfcCatalog()
        }
        if(nfcCatalog!=null) {
            NFCBoardCatalogService.storeCatalog(nfcCatalog!!)
            currentFw = NFCBoardCatalogService.getCurrentFirmware(device.boardID!!, device.firmwareID!!)
        }
    }


    private fun showData(deviceData: DeviceGenericData) {
        if(currentFw != null) {
            val adapter = ViewPageAdapter(this, currentFw!!, deviceData, timing)
            mViewPager.adapter = adapter
            mTabLayoutMediator = TabLayoutMediator(mTabLayout, mViewPager) { tab, position ->
                tab.text = getString(adapter.getTabName(position))
            }.apply {
                attach()
            }

            mDataView.visibility = View.VISIBLE
            mLoadingView.visibility = View.GONE
        }
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

private class ViewPageAdapter(fragment: Fragment, private val currentFw: NfcV2Firmware, private val deviceData: DeviceGenericData, private val timing: Int) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment =
        when (position) {
            0 -> GenericDataPlotFragment(currentFw, deviceData.genericData, timing)
            1 -> SingleDeviceLocationFragment.newInstance(deviceData.locationData)
            2 -> GenericDataListFragment(currentFw, deviceData.genericData)
            else -> throw  IllegalArgumentException("Invalid position: $position")
        }

    @StringRes
    fun getTabName(position: Int): Int = when (position) {
        0 -> R.string.singleDevice_plotTab
        1 -> R.string.singleDevice_locationTab
        2 -> R.string.singleDevice_listTab
        else -> throw  IllegalArgumentException("Invalid position: $position")
    }
}