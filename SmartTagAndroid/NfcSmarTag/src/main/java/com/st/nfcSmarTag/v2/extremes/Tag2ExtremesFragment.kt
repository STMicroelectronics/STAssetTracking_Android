package com.st.nfcSmarTag.v2.extremes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.st.nfcSmarTag.R
import com.st.nfcSmarTag.v2.NfcTag2ViewModel
import com.st.smartaglibrary.util.getTypeSerializableExtra
import com.st.smartaglibrary.v2.SmarTag2Service
import com.st.smartaglibrary.v2.catalog.NFCTag2CurrentFw
import com.st.smartaglibrary.v2.model.SmarTag2Extremes

class Tag2ExtremesFragment : Fragment() {

    private val nfcServiceResponse = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                SmarTag2Service.READ_TAG_EXTREME_DATA_ACTION -> {
                    val smarTag2Extremes: SmarTag2Extremes = intent.getTypeSerializableExtra(
                        SmarTag2Service.EXTRA_TAG_EXTREME_DATA
                    )
                    tag2ExtremesViewModel.newExtremeData(smarTag2Extremes)
                }
                SmarTag2Service.READ_TAG_ERROR_ACTION -> {
                    val msg = intent.getStringExtra(SmarTag2Service.EXTRA_ERROR_STR)
                    nfcTag2ViewModel.nfcTagError(msg ?: "Error.")
                }
            }
        }
    }

    private lateinit var rootView: View
    private lateinit var tag2ExtremesRecyclerView: RecyclerView
    private lateinit var tag2ExtremesViewModel: Tag2ExtremesViewModel
    private lateinit var nfcTag2ViewModel: NfcTag2ViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_tag2_extremes, container, false)
        initializeView()
        activity?.title = "Extremes Data"
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nfcTag2ViewModel = NfcTag2ViewModel.create(requireActivity())
        tag2ExtremesViewModel = Tag2ExtremesViewModel.create(requireActivity())
        initializeTagExtremesObserver()
        initializeNfcTagObserver()
    }

    private fun initializeView() {
        tag2ExtremesRecyclerView = rootView.findViewById(R.id.tag2_extremes_rv)
    }

    /*private fun showSnackMessage(msg: String) {
        val rootView = activity?.findViewById<View>(android.R.id.content)
        if (rootView != null)
            Snackbar.make(rootView, msg, Snackbar.LENGTH_SHORT).show()
    }*/

    private fun initializeTagExtremesObserver() {
        tag2ExtremesViewModel.dataExtreme.observe(viewLifecycleOwner, Observer {
            updateExtremeData(it)
        })
    }

    private fun initializeNfcTagObserver() {
        nfcTag2ViewModel.nfcTag.observe(viewLifecycleOwner, Observer {
            if (it != null)
                SmarTag2Service.startReadingDataExtreme(requireContext(), it)
        })
    }

    private fun updateExtremeData(extremes: SmarTag2Extremes) {
        tag2ExtremesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        tag2ExtremesRecyclerView.adapter =
            Tag2ExtremesAdapter(NFCTag2CurrentFw.getCurrentFw(), extremes.virtualSensorsMinMax, requireContext())
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(nfcServiceResponse, SmarTag2Service.getReadDataExtremeFilter())
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(nfcServiceResponse)
    }
}