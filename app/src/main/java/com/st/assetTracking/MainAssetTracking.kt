package com.st.assetTracking

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.Observer
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.st.assetTracking.addboard.AddBoardDialogFragment
import com.st.assetTracking.dashboard.BuildConfig
import com.st.assetTracking.dashboard.communication.aws.AwsAssetTrackingService
import com.st.assetTracking.dashboard.model.LastDeviceLocations
import com.st.assetTracking.dashboard.persistance.DeviceListRepository
import com.st.assetTracking.management.AssetTrackingNavigationViewModel
import com.st.assetTracking.management.deviceData.LastDevicesLocationActivity
import com.st.assetTracking.management.deviceList.DeviceListFragment
import com.st.login.*
import com.st.login.loginprovider.LoginProviderFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class MainAssetTracking : AppCompatActivity() {
    private var isLargeLayout by Delegates.notNull<Boolean>()

    private lateinit var authenticationData: AuthData

    private var networkConnection: Boolean = false
    private val Auth_config_token = "TokenCollection"

    private lateinit var waitLayout: View

    private lateinit var bottomAppBar: BottomAppBar
    private lateinit var floatButtonNewDevice: FloatingActionButton

    /** shared view model that is managing the fragment to display */
    private val mNavigator by viewModels<AssetTrackingNavigationViewModel>()

    @SuppressLint("ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_asset_tracking)

        /* Hide title bar */
        try {
            this.supportActionBar!!.hide()
        } catch (_: NullPointerException) {}

        isLargeLayout = false

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        waitLayout = findViewById(R.id.dashboardInteractionProgress)

        /* Setup Bottom App Bar + FloatingActionButton */
        bottomAppBar = findViewById(R.id.bottomAppBar)
        floatButtonNewDevice = findViewById(R.id.addNewDevice)

        val loginManager = LoginManager(activityResultRegistry, this, applicationContext, LoginProviderFactory.LoginProviderType.COGNITO, Configuration.getInstance(applicationContext, com.st.login.R.raw.auth_config_cognito))

        /* Check user authenticated [ Fire & Forget ] */
        if (savedInstanceState == null) {
            CoroutineScope(Dispatchers.Main).launch {
                loginManager.login()
            }
        }

        authProcess.observe(this, Observer { status ->
            when (status) {
                is AuthDataLoading.Loaded -> {
                    authenticationData = status.authenticationData
                    loadDeviceList(status.authenticationData)
                }
                else -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        loginManager.forceFreshLogin()
                    }
                }
            }
        })

        /* Check Phone Connection */
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.let {
            try{
                it.registerDefaultNetworkCallback(
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        networkConnection = true
                    }
                    override fun onLost(network: Network) {
                        networkConnection = false
                    }
                })
            }catch (e: NoSuchMethodError){ networkConnection = true }
        }

        /* Add [Floating] Button */
        floatButtonNewDevice.setOnClickListener{
            showDialog()
        }

        /* Bottom Navigation Menu */
        bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.boardslocation -> {
                    if(networkConnection) {
                        waitLayout.visibility = View.VISIBLE
                        val deviceListRepository = DeviceListRepository(authenticationData,
                            AwsAssetTrackingService(authenticationData, applicationContext)
                        )
                        CoroutineScope(Dispatchers.Main).launch {
                            val lastDevicesPosition = deviceListRepository.getLastDevicesLocation()
                            if (lastDevicesPosition != null) {
                                showDevicesLocationPage(lastDevicesPosition)
                            } else {
                                waitLayout.visibility = View.GONE
                                showMissingLastDevicesLocation()
                            }
                        }
                    }else{
                        showSnackbar(window.decorView.rootView, "Offline. Check your Internet connection.")
                    }
                    true
                }
                R.id.logout -> {
                    if(networkConnection){
                        val tokenReader: SharedPreferences = applicationContext.getSharedPreferences(Auth_config_token, Context.MODE_PRIVATE)
                        val emailN = tokenReader.getString("email_N", "")
                        if (emailN != null) {
                            logout(emailN)
                        }else{
                            logout("")
                        }
                    }else{
                        showSnackbar(window.decorView.rootView, "Offline. Check your Internet connection.")
                    }
                    true
                }
                R.id.dshcloud -> {
                    val builder = CustomTabsIntent.Builder()
                    val customTabsIntent: CustomTabsIntent = builder.build()
                    customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    customTabsIntent.launchUrl(applicationContext, Uri.parse(BuildConfig.URL_DSH))
                    true
                }
                R.id.main_showLicenses -> {
                    startActivity(Intent(this, OssLicensesMenuActivity::class.java))
                    true
                }
                R.id.main_appversion -> {
                    startActivity(Intent(this, AppVersion::class.java))
                    true
                }
                else -> false
            }
        }

        /* Wait For Loading Device List */
        mNavigator.currentView.observe(this, Observer { destination ->
            changeView(destination)
        })
    }

    private fun loadDeviceList(authData: AuthData) {
        mNavigator.onLogined(authData, applicationContext)
    }

    /** Show Missing Last Devices Locations Dialog */
    private fun showMissingLastDevicesLocation(){
        Toast.makeText(applicationContext, "There is no locations associated to your devices.", Toast.LENGTH_SHORT).show()
    }

    /** Show Add Board Dialog */
    private fun showDialog() {
        val intent = Intent(applicationContext, AddBoardDialogFragment::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        applicationContext.startActivity(intent)
    }

    /** Do logout */
    private fun logout(email: String){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setTitle("Logout")
        builder.setMessage("$email\n\nAre you sure you would like to sign out?")
        builder.setPositiveButton("Yes") { _, _ ->
            val settings = applicationContext?.getSharedPreferences(Auth_config_token, Context.MODE_PRIVATE)
            settings?.edit()?.clear()?.apply()
            Toast.makeText(applicationContext, "$email just logged out", Toast.LENGTH_SHORT).show()
            this.onBackPressed()
        }
        builder.setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
                .create()
                .show()
    }

    private fun showSnackbar(view: View, message: String) {
        Snackbar.make(view,
                message,
                Snackbar.LENGTH_SHORT)
                .show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun changeView(destinationView: AssetTrackingNavigationViewModel.Destination) {
        when (destinationView) {
            is AssetTrackingNavigationViewModel.Destination.DeviceList -> { showDeviceListPage() }
            else -> {}
        }
    }

    private fun showDevicesLocationPage(lastDevicesPosition: List<LastDeviceLocations>) {
        //mapConnectivityToImage(lastDevicesPosition)

        val intent = Intent(this, LastDevicesLocationActivity::class.java).apply {
            putExtra(LAST_DEVICE_LOCATION_DATA, ArrayList(lastDevicesPosition))
        }
        startActivity(intent)
        waitLayout.visibility = View.GONE
    }

    /**
     * Function that assign an icon for marker in map
    private fun mapConnectivityToImage(lastDevicesPosition: List<LastDeviceLocations>) {
        lastDevicesPosition.forEach{
            if(it.connectivity == "nfc"){
                it.connectivity = "smartag1.png"
            }else if(it.connectivity == "ble"){
                it.connectivity = "sensortilebox.png"
            }else if(it.connectivity == "Lora_ttn"){
                it.connectivity = "loratracker.png"
            }else{

            }
        }
    }
    */

    private fun showDeviceListPage() {
        val fm = supportFragmentManager
        if(mNavigator.deviceListRepository != null){
            fm.fragmentFactory = AssetTrackingFragmentFactory(mNavigator.deviceListRepository!!)
            var fragment = fm.findFragmentByTag(SHOW_DEVICE_LIST_FRAGMENT_TAG)
            if (fragment == null) {
                fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, DeviceListFragment::class.java.name)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.assetTracker_dashboard_rootView, fragment, SHOW_DEVICE_LIST_FRAGMENT_TAG)
                    .commit()
            }
        } else {
            Toast.makeText(applicationContext, "Show Device List Page FAILED.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private val SHOW_DEVICE_LIST_FRAGMENT_TAG = MainAssetTracking::class.java.name + ".SHOW_DEVICE_LIST_FRAGMENT_TAG"
        private const val LAST_DEVICE_LOCATION_DATA = "Last Device Location"
    }

}

internal class AssetTrackingFragmentFactory(private val deviceListRepository: DeviceListRepository) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment =
        when (loadFragmentClass(classLoader, className)) {
            DeviceListFragment::class.java -> DeviceListFragment(deviceListRepository)
            else -> super.instantiate(classLoader, className)
        }
}
