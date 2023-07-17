package com.st.assetTracking.atrBle1.sensorTileBox.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatDialogFragment
import com.st.assetTracking.atrBle1.R


class InfoTresholdsDialogFragment: AppCompatDialogFragment() {
    companion object {
        fun newInstance() = InfoTresholdsDialogFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        dialog?.setTitle("How to set up sensors")

        val rootView = inflater.inflate(R.layout.info_fragment_treshold, container, false)

        val btnClose = rootView.findViewById<Button>(R.id.btn_close)
        btnClose.setOnClickListener {
            dismiss()
        }

        return rootView
    }

}