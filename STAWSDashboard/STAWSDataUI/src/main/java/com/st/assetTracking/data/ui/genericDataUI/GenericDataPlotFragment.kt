package com.st.assetTracking.data.ui.genericDataUI

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.st.assetTracking.data.GenericDataSample
import com.st.assetTracking.data.ui.R
import com.st.smartaglibrary.v2.catalog.NFCBoardCatalogService
import com.st.smartaglibrary.v2.catalog.NfcV2Firmware

class GenericDataPlotFragment(private val currentFw: NfcV2Firmware, private var genericDataSample: List<GenericDataSample>, private val timing: Int) : Fragment() {

    private lateinit var rootView: View
    private lateinit var genericDataPlotRecyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        rootView = inflater.inflate(R.layout.fragment_generic_data_plot, container, false)
        genericDataPlotRecyclerView = rootView.findViewById(R.id.generic_samples_plot_rv)
        displayData()
        return rootView
    }

    private fun displayData() {
        genericDataPlotRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        genericDataPlotRecyclerView.adapter = GenericDataPlotAdapter(currentFw, NFCBoardCatalogService.extractAllVirtualSensorsIds(currentFw), genericDataSample, requireContext(), timing)
    }
}