package com.st.assetTracking.dashboard.certManager.registerDevice

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.st.assetTracking.dashboard.R
import com.st.assetTracking.dashboard.certManager.CertManagerViewModel
import com.st.assetTracking.dashboard.certManager.registerDevice.RegisterDeviceViewModel.RegistrationStatus
import com.st.assetTracking.dashboard.communication.aws.AwsErrorMessage
import com.st.assetTracking.dashboard.model.Device
import com.st.assetTracking.dashboard.persistance.DeviceListRepository


/**
 * fragment used to create a new device on the cloud
 */
internal class RegisterDeviceFragment(deviceId: String, deviceListRepository: DeviceListRepository, deviceType: String) :
        Fragment() {

    /**
     * shared view model used to notify that the task is completed
     */
    private val mNavigatorViewModel by activityViewModels<CertManagerViewModel>()

    private val viewModel by viewModels<RegisterDeviceViewModel> {
        RegisterDeviceViewModel.Factory(requireContext(), deviceId, deviceListRepository)
    }

    private lateinit var mProgressBar: ProgressBar
    private lateinit var mRegisterButton: Button
    private lateinit var mLoadingStatus: TextView
    private lateinit var nameEditText: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireActivity().title = "Asset Tracking Cloud"

        val rootView = inflater.inflate( R.layout.aws_fragment_register_device, container, false)

        nameEditText = rootView.findViewById<EditText>(R.id.registerDevice_deviceNameText)
        mRegisterButton = rootView.findViewById(R.id.registerDevice_registerButton)

        // for enabling the Register button if there is a not empty device name
        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                //Check if it's empty
                val textInput: String = nameEditText.text.toString().trim()
                mRegisterButton.isEnabled = textInput.isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        mRegisterButton.setOnClickListener {
            viewModel.registerDeviceWithName(requireContext(), nameEditText.text.toString().trim(), mNavigatorViewModel.deviceType,mNavigatorViewModel.certificate,mNavigatorViewModel.selfSigned)
        }

        val deviceIdText = rootView.findViewById<TextView>(R.id.registerDevice_title)
        deviceIdText.text = getString(R.string.registerDevice_title_format, viewModel.deviceId)

        mLoadingStatus = rootView.findViewById(R.id.registerDevice_errorText)
        mProgressBar = rootView.findViewById(R.id.registerDevice_progress)

        viewModel.registrationStatus.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                RegistrationStatus.ONLINE_CHECK -> showOnlineCheck()
                RegistrationStatus.REGISTRATION_ONGOING -> showRegistrationOngoing()
                RegistrationStatus.REGISTRATION_APIKEY -> showRegistrationApiKey(viewModel.getRegistedDevice())
                RegistrationStatus.COMPLETE -> {
                    mProgressBar.visibility = View.INVISIBLE
                    //registration is completed, change fragment
                    mNavigatorViewModel.onDeviceRegistered(requireContext(), viewModel.deviceId, viewModel.getRegistedDevice())
                }
                RegistrationStatus.FAILED -> showRegistrationFailed()
                RegistrationStatus.REGISTRATION_NEEDED -> showRegistrationNeeded()
                null -> {
                }
            }
        })
        return rootView
    }

    private fun enableUserInput() {
        mProgressBar.visibility = View.INVISIBLE
        nameEditText.isEnabled = true
    }

    private fun disableUserInput() {
        mProgressBar.visibility = View.VISIBLE
        nameEditText.isEnabled = false
    }

    private fun showOnlineCheck() {
        disableUserInput()
        mLoadingStatus.setText(R.string.registerDevice_onlineCheck)
    }

    private fun showRegistrationOngoing() {
        disableUserInput()
        mLoadingStatus.setText(R.string.registerDevice_registrationOngoing)
    }

    private fun showRegistrationApiKey(device: Device?) {
        disableUserInput()
        mLoadingStatus.setText(R.string.registerDevice_cert_manager_getApiKey)
        if(device!=null) {
            nameEditText.setText(device.name)
        }
        mProgressBar.visibility = View.VISIBLE
    }

    private fun showRegistrationNeeded() {
        enableUserInput()
        mLoadingStatus.setText(R.string.registerDevice_registrationNeeded)
    }

    private fun showRegistrationFailed() {
        enableUserInput()
        mLoadingStatus.text = AwsErrorMessage.getErrorMessage()
    }
}