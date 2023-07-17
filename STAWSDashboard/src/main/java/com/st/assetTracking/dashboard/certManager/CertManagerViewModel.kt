package com.st.assetTracking.dashboard.certManager

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultRegistry
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.st.assetTracking.dashboard.communication.DeviceManager
import com.st.assetTracking.dashboard.communication.aws.AwsAssetTrackingService
import com.st.assetTracking.dashboard.model.Device
//import com.st.assetTracking.dashboard.persistance.AssetTrackingDashboardDB
import com.st.assetTracking.dashboard.persistance.DeviceListRepository
import com.st.login.*
import com.st.login.R
import com.st.login.loginprovider.LoginProviderFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * view model to manage the Certification Registrastion
 */
internal class CertManagerViewModel(
        var deviceId: String,
        val deviceType: String,
        var certificate: String?,
        var selfSigned: Boolean
        ) : ViewModel() {

    internal sealed class Destination {
        /**
         * Certificate Manager Page
         */
        object CertificateManagerPage : Destination()

        /**
         * require the user login
         */
        object LoginPage : Destination()

        /**
         * create the device
         */
        object CreateDevice : Destination()

        /**
         * registration completed
         */
        object RegistrationCompleted : Destination()

        /**
         * initial state, check if the user is log in
         */
        object Unknown : Destination()
    }

    private val _currentView = MutableLiveData<Destination>(Destination.Unknown)

    private var mDevice : Device?=null

    /**
     * view to display
     */
    val currentView: LiveData<Destination>
        get() = _currentView

    /**
     * object used to check if the device is known
     */
    var deviceListRepository: DeviceListRepository? = null
        private set

    /**
     * object to use for Register one Certificate
     */
    var deviceManager: DeviceManager? = null
        private set

    fun onLoginComplete(loginData: AuthData, context: Context) {
        val deviceListRemote = AwsAssetTrackingService(loginData, context)
        deviceListRepository = DeviceListRepository(
            loginData,
            deviceListRemote
            )
        _currentView.postValue(Destination.CreateDevice)
    }

    fun onDeviceRegistered(context: Context, deviceId: String, device : Device?) {
        deviceManager = deviceListRepository?.buildRemoteDeviceManagerFor(context, deviceId)
        mDevice = device
        _currentView.postValue(Destination.RegistrationCompleted)
    }

    fun onCertificateRequestConfigured() {
        _currentView.postValue(Destination.CertificateManagerPage)
    }

    /**
     * Function for retriving the Device registerd
     */
    fun getDevice() : Device ? {
        return mDevice
    }

    /**
     * check if the user is already login or request the credentials
     */
    fun checkLogin(resultRegistry: ActivityResultRegistry, activity: AppCompatActivity, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {

            CoroutineScope(Dispatchers.Main).launch {
                val authData = LoginManager(
                    resultRegistry,
                    activity,
                    context,
                    LoginProviderFactory.LoginProviderType.COGNITO,
                    Configuration.getInstance(
                        context,
                        R.raw.auth_config_cognito
                    )
                ).login()
                if (authData != null) {
                    onLoginComplete(authData, context)
                }/*else{
                    _currentView.postValue(Destination.LoginPage)
                }*/
            }
        }
    }

    class Factory(
            private val deviceId: String,
            private val deviceType: String,
            private var certificate: String?,
            private var SelfSigned: Boolean
            ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CertManagerViewModel(deviceId, deviceType, certificate, SelfSigned) as T
        }
    }

}