package com.st.assetTracking.dashboard


import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.Observer
import com.st.assetTracking.dashboard.persistance.DeviceListRepository
import com.st.assetTracking.dashboard.certManager.CertManagerViewModel
import com.st.assetTracking.dashboard.certManager.CertManagerViewModel.Destination
import com.st.assetTracking.dashboard.certManager.CertManagerViewModel.Factory
import com.st.assetTracking.dashboard.certManager.manager.CertManagerPageFragment
import com.st.assetTracking.dashboard.certManager.registerDevice.RegisterDeviceFragment


/**
 * activity used to upload the certificate of a specific device to the cloud
 * or retrieve one certificate for it
 * This activity will close when the upload/download is completed
 */
class AssetTrackingCertManager : AppCompatActivity() {

    /**
     * shared vm used to decide what to display in this activity
     */
    private val mNavigator by viewModels<CertManagerViewModel> {
        val deviceId = intent.getStringExtra(DEVICE_ID_EXTRA)
                ?: throw IllegalArgumentException("Missing Device ID parameters")
        val deviceType = intent.getStringExtra(DEVICE_TYPE_EXTRA)
                ?: throw IllegalArgumentException("Missing Device type parameters")
        val certificate = intent.getStringExtra(DEVICE_CERT_EXTRA)
        Factory(
            deviceId,
            deviceType,
            certificate,
            certificate!=null
        )
    }

    companion object {
        val DEVICE_ID_EXTRA = AssetTrackingCertManager::class.java.name + ".DEVICE_ID_EXTRA"
        val DEVICE_TYPE_EXTRA = AssetTrackingCertManager::class.java.name + ".DEVICE_TYPE_EXTRA"
        val DEVICE_CERT_EXTRA = AssetTrackingCertManager::class.java.name + ".DEVICE_CERT_EXTRA"
        const val REQUEST_CERTIFICATE_REQUEST_CODE = 15
        const val REGISTER_CERTIFICATE_REQUEST_CODE = 16
        private val SHOW_LOGIN_FRAGMENT_TAG = AssetTrackingCertManager::class.java.name + ".SHOW_LOGIN_FRAGMENT_TAG"
        private val SHOW_CERT_MANAGER_FRAGMENT_TAG = AssetTrackingCertManager::class.java.name + ".SHOW_CERT_MANAGER_FRAGMENT_TAG"
        private val CREATE_DEVICE_FRAGMENT_TAG = AssetTrackingCertManager::class.java.name + ".CREATE_DEVICE_FRAGMENT_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = DeviceListFragmentFactory(
                deviceIdBuilder = { mNavigator.deviceId },
                deviceTypeBuilder = { mNavigator.deviceType },
                certificateBuilder = {mNavigator.certificate},
                sefSignedBuilder =  {mNavigator.selfSigned},
                deviceListBuilder = { mNavigator.deviceListRepository }
        )
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_asset_tracking_cert_manager)
        mNavigator.currentView.observe(this, Observer { destination ->
            changeView(destination)
        })
    }

    private fun changeView(destinationView: Destination) {
        when (destinationView) {
            is Destination.Unknown -> {
                showCertificateManagerPage()
            }
            is Destination.CertificateManagerPage -> {
                mNavigator.checkLogin(this@AssetTrackingCertManager.activityResultRegistry, this@AssetTrackingCertManager , applicationContext)
            }
            is Destination.LoginPage -> {
                //showLoginPage()
            }
            is Destination.CreateDevice -> {
                showCreateDevicePage()
            }
            is Destination.RegistrationCompleted -> {

                Toast.makeText(this,
                        getString(R.string.certificate_completeMessage),
                        Toast.LENGTH_SHORT)
                        .show()
                //close the activity
                //Log.i("AWS Certificate","AssetTrackingCertManager Certificate="+mNavigator.getDevice()?.certificate)

                if(mNavigator.getDevice()?.certificate!=null) {
                    val intentResult = intent
                    if(mNavigator.getDevice()?.selfSigned==true) {
                        intentResult.putExtra("return_registration", "Done")
                    } else {
                        intentResult.putExtra("return_cert", mNavigator.getDevice()?.certificate)
                    }
                    setResult(Activity.RESULT_OK,intentResult)
                }
                finish()
            }
        }
    }

    private fun showCreateDevicePage() {
        val fm = supportFragmentManager
        var fragment = fm.findFragmentByTag(CREATE_DEVICE_FRAGMENT_TAG)
        if (fragment == null) {
            fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, RegisterDeviceFragment::class.java.name)
            supportFragmentManager.beginTransaction()
                    .replace(R.id.assetTracker_cert_manager_rootView, fragment, CREATE_DEVICE_FRAGMENT_TAG)
                    .commit()
        }
    }

    private fun showCertificateManagerPage() {
        val fm = supportFragmentManager
        var fragment = fm.findFragmentByTag(SHOW_CERT_MANAGER_FRAGMENT_TAG)
        if (fragment == null) {
            fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, CertManagerPageFragment::class.java.name)
            supportFragmentManager.beginTransaction()
                    .replace(R.id.assetTracker_cert_manager_rootView, fragment, SHOW_CERT_MANAGER_FRAGMENT_TAG)
                    .commit()
        }
    }


    internal class DeviceListFragmentFactory(
            private val deviceIdBuilder: () -> String,
            private val deviceTypeBuilder: () -> String,
            private val certificateBuilder: () -> String?,
            private val sefSignedBuilder: () -> Boolean,
            private val deviceListBuilder: () -> DeviceListRepository?) : FragmentFactory() {

        override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
            return when (className) {
                RegisterDeviceFragment::class.java.name -> {
                    val deviceListRepository = deviceListBuilder()!!
                    val deviceId = deviceIdBuilder()
                    val deviceType = deviceTypeBuilder()
                    RegisterDeviceFragment(deviceId, deviceListRepository, deviceType)
                }
                CertManagerPageFragment::class.java.name -> {
                    val deviceId = deviceIdBuilder()
                    val deviceType = deviceTypeBuilder()
                    val certificate = certificateBuilder()
                    val selfSigned = sefSignedBuilder()
                    CertManagerPageFragment(deviceId,deviceType,certificate,selfSigned)
                }
                else -> super.instantiate(classLoader, className)
            }
        }
    }

}
