package com.st.assetTracking.sigfox

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.commit
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.gui.ActivityWithNode
import com.st.assetTracking.atrBle1.sensorTileBox.settings.LogSettingsRepository

class AtrBleSigfoxDetails : ActivityWithNode() {

    companion object {
        const val SETTINGS_FRAGMENT = "AtrBleDeviceDetails.SETTINGS_FRAGMENT"
        fun startWithNode(context: Context, node: Node): Intent {
            return getStartIntent(context, AtrBleSigfoxDetails::class.java, node, false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val node = getNodeFromIntent(intent)
        supportFragmentManager.fragmentFactory = AtrBleSigfoxDetailsFragmentFactory(node)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_atr_ble_sigfox_details)

        showSettingsFragment()
    }

    private fun showSettingsFragment() {
        val fm = supportFragmentManager
        val existingSettingFragment = fm.findFragmentByTag(SETTINGS_FRAGMENT)

        supportFragmentManager.commit {
            if (existingSettingFragment == null) {
                val settingsFragment = fm.fragmentFactory.instantiate(
                        this@AtrBleSigfoxDetails.classLoader, SettingsSigfoxFragment::class.java.name)
                add(R.id.deviceDetails_content, settingsFragment, SETTINGS_FRAGMENT)
            } else {
                show(existingSettingFragment)
            }
        }
    }
}

private class AtrBleSigfoxDetailsFragmentFactory(private val node: Node) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            SettingsSigfoxFragment::class.java.name -> SettingsSigfoxFragment(LogSettingsRepository(node), node)
            else -> super.instantiate(classLoader, className)
        }
    }
}