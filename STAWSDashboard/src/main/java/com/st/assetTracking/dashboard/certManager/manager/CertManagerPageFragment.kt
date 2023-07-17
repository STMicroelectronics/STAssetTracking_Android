package com.st.assetTracking.dashboard.certManager.manager

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.st.assetTracking.dashboard.R
import com.st.assetTracking.dashboard.certManager.CertManagerViewModel

/**
 * generic class used to sign in the user with his account
 */
internal class CertManagerPageFragment(deviceId: String, deviceType : String, certificate: String?,selfSigned: Boolean) : Fragment() {

    private val mNavigatorViewModel by activityViewModels<CertManagerViewModel>()

    private val viewModel by viewModels<CertManagerPageViewModel> {
        CertManagerPageViewModel.Factory(requireContext(), deviceId, deviceType,certificate,selfSigned)
    }

    private lateinit var textViewCertificate: TextView
    private lateinit var textViewSTM32ID: TextView
    private lateinit var buttonRegister: Button
    private lateinit var buttonShowCert: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.activity_send_receive_cert, container, false)


        textViewCertificate = rootView.findViewById(R.id.certifcate_cert_textview)
        textViewSTM32ID = rootView.findViewById(R.id.certifcate_stm32_uid_textview)
        buttonRegister = rootView.findViewById(R.id.certifcate_register)
        buttonShowCert = rootView.findViewById(R.id.certificate_show)

        if (mNavigatorViewModel.certificate != null) {
            textViewCertificate.text = mNavigatorViewModel.certificate
            buttonRegister.setText("Register")
            buttonShowCert.visibility = View.VISIBLE
        } else {
            buttonRegister.setText("Request")
        }

        textViewSTM32ID.text = mNavigatorViewModel.deviceId

        buttonRegister.setOnClickListener {
            if(textViewSTM32ID.text.isNotEmpty()) {
                mNavigatorViewModel.deviceId = textViewSTM32ID.text.toString()
            }
            mNavigatorViewModel.onCertificateRequestConfigured()
        }

        buttonShowCert.setOnClickListener {
            if (textViewCertificate.visibility == View.VISIBLE) {
                textViewCertificate.visibility = View.GONE
                buttonShowCert.setText("Show Certificate")
            } else {
                textViewCertificate.visibility = View.VISIBLE
                buttonShowCert.setText("Hide Certificate")
            }
        }
        return rootView
    }
}