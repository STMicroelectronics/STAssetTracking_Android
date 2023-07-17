package com.st.assetTracking.atrBle1.sensorTileBox

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.ActivityWithNode
import com.st.assetTracking.atrBle1.R
import com.st.assetTracking.atrBle1.sensorTileBox.settings.LogDataRepository
import com.st.assetTracking.atrBle1.sensorTileBox.settings.LogSettingsRepository

class AtrBleDeviceDetails : ActivityWithNode() {

    companion object {
        const val SETTINGS_FRAGMENT = "AtrBleDeviceDetails.SETTINGS_FRAGMENT"
        const val DATA_FRAGMENT = "AtrBleDeviceDetails.DATA_FRAGMENT"
        const val EXTREMES_FRAGMENT = "AtrBleDeviceDetails.EXTREMES_FRAGMENT"
        var deviceID: String = ""
        fun startWithNode(context: Context, node: Node, id: String): Intent {
            deviceID = id
            return getStartIntent(context, AtrBleDeviceDetails::class.java, node, false)
        }
    }

    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var settingsItem: MenuItem
    private lateinit var minMaxItem: MenuItem
    private lateinit var dataItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {

        title = "BLE Device Details"

        val node = getNodeFromIntent(intent)
        supportFragmentManager.fragmentFactory = AtrBleDeviceDetailsFragmentFactory(node, deviceID)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_atr_ble_device_details)
        findViewById<BottomNavigationView>(R.id.deviceDetails_bottomNav).setOnNavigationItemSelectedListener { onNavigationViewSelected(it) }

        bottomNavView = findViewById(R.id.deviceDetails_bottomNav)
        settingsItem = bottomNavView.menu.getItem(0)
        minMaxItem = bottomNavView.menu.getItem(1)
        dataItem = bottomNavView.menu.getItem(2)

        showSettingsFragment()
    }

    private fun showSettingsFragment() {
        val fm = supportFragmentManager
        val existingSettingFragment = fm.findFragmentByTag(SETTINGS_FRAGMENT)
        val existingDataFragment = fm.findFragmentByTag(DATA_FRAGMENT)
        val existingExtremesFragment = fm.findFragmentByTag(EXTREMES_FRAGMENT)
        supportFragmentManager.commit {
            if (existingSettingFragment == null) {
                val settingsFragment = fm.fragmentFactory.instantiate(
                        this@AtrBleDeviceDetails.classLoader, SettingsFragment::class.java.name)
                add(R.id.deviceDetails_content, settingsFragment, SETTINGS_FRAGMENT)
            } else {
                show(existingSettingFragment)
            }
            if (existingDataFragment != null) {
                hide(existingDataFragment)
            }
            if (existingExtremesFragment != null) {
                hide(existingExtremesFragment)
            }
        }
    }

    private fun showDataFragment() {
        val fm = supportFragmentManager
        val existingSettingFragment = fm.findFragmentByTag(SETTINGS_FRAGMENT)
        val existingDataFragment = fm.findFragmentByTag(DATA_FRAGMENT)
        val existingExtremesFragment = fm.findFragmentByTag(EXTREMES_FRAGMENT)
        supportFragmentManager.commit {
            if (existingDataFragment == null) {
                val dataFragment = fm.fragmentFactory.instantiate(
                        this@AtrBleDeviceDetails.classLoader, DataFragment::class.java.name)
                add(R.id.deviceDetails_content, dataFragment, DATA_FRAGMENT)
            } else {
                show(existingDataFragment)
            }
            if (existingSettingFragment != null) {
                hide(existingSettingFragment)
            }
            if (existingExtremesFragment != null) {
                hide(existingExtremesFragment)
            }
        }
    }

    private fun showExtremesFragment() {
        val fm = supportFragmentManager
        val existingSettingFragment = fm.findFragmentByTag(SETTINGS_FRAGMENT)
        val existingDataFragment = fm.findFragmentByTag(DATA_FRAGMENT)
        val existingExtremesFragment = fm.findFragmentByTag(EXTREMES_FRAGMENT)
        supportFragmentManager.commit {
            if (existingExtremesFragment == null) {
                val extremesFragment = fm.fragmentFactory.instantiate(
                        this@AtrBleDeviceDetails.classLoader, ExtremesFragment::class.java.name)
                add(R.id.deviceDetails_content, extremesFragment, EXTREMES_FRAGMENT)
            } else {
                show(existingExtremesFragment)
            }
            if (existingSettingFragment != null) {
                hide(existingSettingFragment)
            }
            if (existingDataFragment != null) {
                hide(existingDataFragment)
            }
        }
    }


    private fun onNavigationViewSelected(selected: MenuItem): Boolean {
        when (selected.itemId) {
            R.id.device_settings -> {
                showSettingsFragment()
            }
            R.id.device_data -> {
                showDataFragment()
            }
            R.id.device_extremes -> {
                showExtremesFragment()
            }
        }
        return true
    }

    /**
     * Enable "Min/Max" and "Data" Item of bottom navigationView when BLE initialization is over.
     */
    fun finishBLEInitialization() {
        minMaxItem.isEnabled = true
        dataItem.isEnabled = true
    }

    /**
     * Disable "Settings" and "Min/Max" Item of bottom navigationView until reading data is over.
     */
    fun disableSettingsMinMax() {
        settingsItem.isEnabled = false
        minMaxItem.isEnabled = false
    }

    /**
     * Enable "Settings" and "Min/Max" Item of bottom navigationView when reading data is over.
     */
    fun enableSettingsMinMax() {
        settingsItem.isEnabled = true
        minMaxItem.isEnabled = true
    }

    /**
     * Disable "Settings" and "Data" Item of bottom navigationView until reading data is over.
     */
    fun disableSettingsData() {
        settingsItem.isEnabled = false
        dataItem.isEnabled = false
    }

    /**
     * Enable "Settings" and "Data" Item of bottom navigationView when reading data is over.
     */
    fun enableSettingsData() {
        settingsItem.isEnabled = true
        dataItem.isEnabled = true
    }

}

private class AtrBleDeviceDetailsFragmentFactory(private val node: Node, private val id: String) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            SettingsFragment::class.java.name -> SettingsFragment(LogSettingsRepository(node), node, id)
            DataFragment::class.java.name -> DataFragment(LogDataRepository(node))
            ExtremesFragment::class.java.name -> ExtremesFragment(LogDataRepository(node))
            else -> super.instantiate(classLoader, className)
        }
    }
}
