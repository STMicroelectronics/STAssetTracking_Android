package com.st.assetTracking.management.deviceData

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.commit
import com.st.assetTracking.R
import com.st.assetTracking.dashboard.model.Device
import com.st.assetTracking.dashboard.persistance.DeviceDataRepository
import com.st.assetTracking.management.genericData.DeviceGenericDataFragment
import kotlin.time.ExperimentalTime

class DeviceHistoryData : AppCompatActivity() {

    private lateinit var mDeviceViewModel: DeviceDataViewModel

    companion object {

        private val DEVICE_LIST_TAG = DeviceHistoryData::class.java.name + ".MAIN_ACTIVITY"
        private val SHOW_DEVICE_DETAILS_FRAGMENT_TAG = DeviceHistoryData::class.java.name + ".SHOW_DEVICE_DETAILS_FRAGMENT_TAG"

        private lateinit var currentDevice: Device
        private lateinit var deviceDataRepository: DeviceDataRepository

        fun startWithDeviceDataRepository(context: Context, device: Device, deviceData: DeviceDataRepository): Intent? {
            currentDevice = device
            deviceDataRepository = deviceData
            return Intent(context, DeviceHistoryData::class.java)
        }
        fun getCurrentDeviceIntent(): Device {
            return currentDevice
        }
        fun getDeviceDataRepositoryIntent(): DeviceDataRepository {
            return deviceDataRepository
        }
    }

    private fun initializeViewModel(dataRepository: DeviceDataRepository){
        mDeviceViewModel = DeviceDataViewModel(dataRepository, currentDevice.boardID, currentDevice.firmwareID)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId ==android.R.id.home) {
            super.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @OptIn(ExperimentalTime::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_history_data)

        title = "Device History"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val currentDevice = getCurrentDeviceIntent()
        val dataRepository = getDeviceDataRepositoryIntent()
        initializeViewModel(dataRepository)

        supportFragmentManager.fragmentFactory = AssetTrackingShowDataFragmentFactory(
            deviceBuilder = { currentDevice },
            deviceDetailsBuilder = { dataRepository }
        )

        showDeviceDetailsPage(currentDevice)
    }

    @ExperimentalTime
    private fun showDeviceDetailsPage(currentDevice: Device) {
        val fm = supportFragmentManager
        var fragment = fm.findFragmentByTag(SHOW_DEVICE_DETAILS_FRAGMENT_TAG)

        if(currentDevice.type == Device.Type.NFCTAG2){
            if (fragment == null) {
                fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, DeviceGenericDataFragment::class.java.name)
                supportFragmentManager.commit {
                    replace(R.id.device_data_history_rootView, fragment!!, SHOW_DEVICE_DETAILS_FRAGMENT_TAG)
                    addToBackStack(DEVICE_LIST_TAG)
                }
            }
        } else {
            if (fragment == null) {
                fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, SingleDeviceFragment::class.java.name)
                supportFragmentManager.commit {
                    replace(R.id.device_data_history_rootView, fragment, SHOW_DEVICE_DETAILS_FRAGMENT_TAG)
                    addToBackStack(DEVICE_LIST_TAG)
                }
            }
        }
    }
}

@ExperimentalTime
internal class AssetTrackingShowDataFragmentFactory(private val deviceBuilder: () -> Device, private val deviceDetailsBuilder: () -> DeviceDataRepository?) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            SingleDeviceFragment::class.java.name -> {
                val deviceManager = deviceDetailsBuilder()!!
                SingleDeviceFragment(deviceManager)
            }
            DeviceGenericDataFragment::class.java.name -> {
                val currentDevice = deviceBuilder()
                val deviceManager = deviceDetailsBuilder()!!
                DeviceGenericDataFragment(currentDevice, deviceManager)
            }
            else -> super.instantiate(classLoader, className)
        }
    }
}
