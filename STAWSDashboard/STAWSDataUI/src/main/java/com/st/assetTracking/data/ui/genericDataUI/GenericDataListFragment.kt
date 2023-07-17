package com.st.assetTracking.data.ui.genericDataUI

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.st.assetTracking.data.GenericDataSample
import com.st.assetTracking.data.GenericSample
import com.st.assetTracking.data.getSensorDataSample
import com.st.assetTracking.data.ui.R
import com.st.smartaglibrary.v2.catalog.NFCTag2CurrentFw
import com.st.smartaglibrary.v2.catalog.NfcV2Firmware

class GenericDataListFragment(
    private var currentFw: NfcV2Firmware,
    private var genericDataSample: List<GenericDataSample>) : Fragment() {

    private lateinit var rootView: View
    private lateinit var tag2SamplesRecyclerView: RecyclerView
    private lateinit var tag2SamplesListedAdapter: GenericDataListAdapter

    private lateinit var tag2FilterTv: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_generic_list, container, false)

        tag2SamplesRecyclerView = rootView.findViewById(R.id.generic_list_samples_listed_rv)

        rootView.findViewById<ImageButton>(R.id.generic_listt_filter_IB).setOnClickListener {
            showFilterDataChoice()
        }
        rootView.findViewById<ImageButton>(R.id.generic_list_reset_filter_IB).setOnClickListener {
            cancelFilterDataChoice()
        }

        tag2FilterTv = rootView.findViewById(R.id.generic_list_filter_tv)

        displayData(genericDataSample)
        return rootView
    }

    private fun displayData(data: List<GenericSample>) {
        tag2SamplesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        tag2SamplesListedAdapter = GenericDataListAdapter(currentFw, data.getSensorDataSample(), requireContext())
        tag2SamplesRecyclerView.adapter = tag2SamplesListedAdapter
    }

    private fun showFilterDataChoice() {
        if (genericDataSample.isNotEmpty()) {
            VirtualSensorFilterDialog(
                tag2SamplesListedAdapter,
                tag2FilterTv
            ).show(requireActivity().supportFragmentManager, "virtualSensorFilterDialog")
        }
    }
    private fun cancelFilterDataChoice() {
        tag2SamplesListedAdapter.resetFilterSample()
        tag2FilterTv.text = "Filter by: no filter selected"
    }
}


class VirtualSensorFilterDialog(private val adapter: GenericDataListAdapter, private val filterTv: TextView) : DialogFragment() {

    private fun retrieveVSNamesList(): Array<CharSequence> {
        val currentFw = NFCTag2CurrentFw.getCurrentFw()
        val vsNames: ArrayList<CharSequence> = ArrayList()
        currentFw.virtualSensors.forEach{ vs ->
            vsNames.add(vs.displayName)
        }
        val vsArrayNames = Array(currentFw.virtualSensors.size) { arrayPos ->
            vsNames[arrayPos]
        }
        return vsArrayNames
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val vsNames = retrieveVSNamesList()

        return activity?.let {
            val checkedItems = ArrayList<Int>()
            val alertBuilder = AlertDialog.Builder(it)

            alertBuilder.setTitle("Select an option")

            alertBuilder.setMultiChoiceItems(vsNames, null) { _, index, checked ->
                if (checked) {
                    checkedItems.add(index)
                } else if (checkedItems.contains(index)) {
                    checkedItems.remove(index)
                }
            }
            alertBuilder.setPositiveButton("OK") { _, _ ->
                var filterStrToDisplay = "Filter by: "
                checkedItems.forEach { selectedFilterIndex ->
                    filterStrToDisplay += "${vsNames[selectedFilterIndex]}; "
                }
                filterTv.text = filterStrToDisplay
                adapter.filterSamples(checkedItems.toList())
            }
            alertBuilder.create()
        } ?: throw IllegalStateException("Exception !! Activity is null !!")
    }
}