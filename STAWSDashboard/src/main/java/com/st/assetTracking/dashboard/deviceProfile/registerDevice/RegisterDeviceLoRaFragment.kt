package com.st.assetTracking.dashboard.deviceProfile.registerDevice

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
import com.st.assetTracking.dashboard.communication.aws.AwsErrorMessage
import com.st.assetTracking.dashboard.deviceProfile.DeviceProfileViewModel
import com.st.assetTracking.dashboard.persistance.DeviceListRepository


/**
 * fragment used to create a new device on the cloud
 */
internal class RegisterDeviceLoRaFragment(deviceId: String, deviceListRepository: DeviceListRepository) :
        Fragment() {

    /**
     * shared view model used to notify that the task is completed
     */
    private val mNavigatorViewModel by activityViewModels<DeviceProfileViewModel>()

    private val viewModel by viewModels<RegisterDeviceLoRaViewModel> {
        RegisterDeviceLoRaViewModel.Factory(requireContext(), deviceId, deviceListRepository)
    }

    private lateinit var mProgressBar: ProgressBar
    private lateinit var mRegisterButton: Button
    private lateinit var mLoadingStatus: TextView
    private lateinit var nameEditText: EditText

    private lateinit var spDeviceProfiles: Spinner
    private var spinnerArray: MutableList<String> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireActivity().title = "Asset Tracking Cloud"

        val rootView = inflater.inflate( R.layout.lora_fragment_register_device, container, false)

        nameEditText = rootView.findViewById<EditText>(R.id.registerDevice_deviceNameText)
        mRegisterButton = rootView.findViewById(R.id.registerDevice_registerButton)

        spDeviceProfiles = rootView.findViewById(R.id.spDeviceProfiles)

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

        val deviceIdText = rootView.findViewById<TextView>(R.id.registerDevice_title)
        deviceIdText.text = getString(R.string.registerDevice_title_format, viewModel.deviceId)

        mLoadingStatus = rootView.findViewById(R.id.registerDevice_errorText)
        mProgressBar = rootView.findViewById(R.id.registerDevice_progress)

        /**
         * Spinner Device Profile
         */
        viewModel.deviceProfiles.observe(viewLifecycleOwner, Observer { dP ->
            dP.forEach{
                spinnerArray.add(it.id)
            }
            val spinnerArrayAdapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, spinnerArray)
            spDeviceProfiles.adapter = spinnerArrayAdapter
        })

        /**
         * Registration Button
         */
        mRegisterButton.setOnClickListener {
            viewModel.deviceProfiles.observe(viewLifecycleOwner, Observer { dP ->
                dP.forEach{
                    if(it.id == spDeviceProfiles.selectedItem.toString()){
                        viewModel.registerDeviceWithName(requireContext(), nameEditText.text.toString().trim(), mNavigatorViewModel.deviceType, it)
                    }
                }
            })
        }

        /**
         * Registration Status
         */
        viewModel.registrationStatus.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                RegisterDeviceLoRaViewModel.RegistrationStatus.GET_DEVICE_PROFILE -> getDeviceProfile()
                RegisterDeviceLoRaViewModel.RegistrationStatus.ONLINE_CHECK -> checkDeviceOnline()
                RegisterDeviceLoRaViewModel.RegistrationStatus.REGISTRATION_ONGOING -> showRegistrationOngoing()

                RegisterDeviceLoRaViewModel.RegistrationStatus.COMPLETE -> {
                    mProgressBar.visibility = View.INVISIBLE
                    //registration is completed, change fragment
                    mNavigatorViewModel.onDeviceRegistered(requireContext(), viewModel.deviceId)
                }
                RegisterDeviceLoRaViewModel.RegistrationStatus.FAILED -> showRegistrationFailed()
                RegisterDeviceLoRaViewModel.RegistrationStatus.REGISTRATION_NEEDED -> showRegistrationNeeded()
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

    private fun checkDeviceOnline() {
        disableUserInput()
        mLoadingStatus.setText(R.string.registerDevice_onlineCheck)
    }

    private fun getDeviceProfile() {
        disableUserInput()
        mLoadingStatus.setText(R.string.registerDevice_getDeviceProfile)
    }

    private fun showRegistrationOngoing() {
        disableUserInput()
        mLoadingStatus.setText(R.string.registerDevice_registrationOngoing)
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