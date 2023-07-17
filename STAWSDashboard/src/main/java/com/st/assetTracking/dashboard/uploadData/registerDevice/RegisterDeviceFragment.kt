package com.st.assetTracking.dashboard.uploadData.registerDevice

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputLayout
import com.st.assetTracking.dashboard.R
import com.st.assetTracking.dashboard.communication.aws.AwsErrorMessage
import com.st.assetTracking.dashboard.persistance.DeviceListRepository
import com.st.assetTracking.dashboard.uploadData.UploadDataNavigatorViewModel
import com.st.assetTracking.dashboard.uploadData.registerDevice.RegisterDeviceViewModel.RegistrationStatus

/**
 * fragment used to create a new device on the cloud
 */
internal class RegisterDeviceFragment(deviceId: String, deviceListRepository: DeviceListRepository, deviceType: String) :
        Fragment(R.layout.aws_fragment_register_device) {

    /**
     * shared view model used to notify that the task is completed
     */
    private val mNavigatorViewModel by activityViewModels<UploadDataNavigatorViewModel>()

    private val viewModel by viewModels<RegisterDeviceViewModel> {
        RegisterDeviceViewModel.Factory(requireContext(), deviceId, deviceListRepository)
    }

    private lateinit var mProgressBar: ProgressBar
    private lateinit var mRegisterButton: Button
    private lateinit var mLoadingStatus: TextView
    private lateinit var mNameTextLayout: TextInputLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().title = "Asset Tracking Cloud"

        mNameTextLayout = view.findViewById(R.id.registerDevice_deviceNameLayout)
        val nameEditText = view.findViewById<EditText>(R.id.registerDevice_deviceNameText)
        mRegisterButton = view.findViewById(R.id.registerDevice_registerButton)

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
            viewModel.registerDeviceWithName(requireContext(), nameEditText.text.toString(), mNavigatorViewModel.deviceType)
        }

        val deviceIdText = view.findViewById<TextView>(R.id.registerDevice_title)
        deviceIdText.text = getString(R.string.registerDevice_title_format, viewModel.deviceId)

        mLoadingStatus = view.findViewById(R.id.registerDevice_errorText)
        mProgressBar = view.findViewById(R.id.registerDevice_progress)

        viewModel.registrationStatus.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                RegistrationStatus.ONLINE_CHECK -> showOnlineCheck()
                RegistrationStatus.REGISTRATION_ONGOING -> showRegistrationOngoing()
                RegistrationStatus.REGISTRATION_APIKEY -> showRegistrationApiKey()
                RegistrationStatus.COMPLETE -> {
                    mProgressBar.visibility = View.INVISIBLE
                    //registraion is completed, change fragment
                    mNavigatorViewModel.onDeviceRegistered(requireContext(), viewModel.deviceId)
                }
                RegistrationStatus.FAILED -> showRegistrationFailed()
                RegistrationStatus.REGISTRATION_NEEDED -> showRegistrationNeeded()
                null -> {
                }
            }
        })
    }

    private fun enableUserInput() {
        mProgressBar.visibility = View.INVISIBLE
        mNameTextLayout.isEnabled = true
    }

    private fun disableUserInput() {
        mProgressBar.visibility = View.VISIBLE
        mNameTextLayout.isEnabled = false
    }

    private fun showOnlineCheck() {
        disableUserInput()
        mLoadingStatus.setText(R.string.registerDevice_onlineCheck)
    }

    private fun showRegistrationOngoing() {
        disableUserInput()
        mLoadingStatus.setText(R.string.registerDevice_registrationOngoing)
    }

    private fun showRegistrationApiKey() {
        disableUserInput()
        mLoadingStatus.setText(R.string.registerDevice_getApiKey)
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