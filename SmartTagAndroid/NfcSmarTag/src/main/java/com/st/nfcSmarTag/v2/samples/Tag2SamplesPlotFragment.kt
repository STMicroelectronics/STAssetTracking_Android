package com.st.nfcSmarTag.v2.samples

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.st.nfcSmarTag.R
import com.st.smartaglibrary.v2.catalog.NFCBoardCatalogService
import com.st.smartaglibrary.v2.catalog.NFCTag2CurrentFw
import com.st.assetTracking.data.GenericSample
import com.st.assetTracking.data.getSensorDataSample

class Tag2SamplesPlotFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var tag2SamplesRecyclerView: RecyclerView
    private lateinit var tag2SamplesViewModel: Tag2SamplesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_tag2_samples_plot, container, false)
        tag2SamplesRecyclerView = rootView.findViewById(R.id.tag2_samples_plot_rv)
        tag2SamplesViewModel = Tag2SamplesViewModel.create(requireActivity())
        initializeDataSamplesObsverver()
        return rootView
    }

    private fun initializeDataSamplesObsverver() {
        tag2SamplesViewModel.isUpdating.observe(viewLifecycleOwner, Observer {
            it?.let { isReading ->
                if(!isReading){
                    val samplesToDisplay = tag2SamplesViewModel.allSampleList.value
                    if(samplesToDisplay!=null){
                        val sortedSamples = samplesToDisplay.getSensorDataSample().sortedBy { genericDataSample -> genericDataSample.date?.time }
                        displayData(sortedSamples)
                    }
                }
            }
        })
    }

    private fun displayData(data: List<GenericSample>) {
        tag2SamplesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        tag2SamplesRecyclerView.adapter = Tag2SamplesPlotAdapter(NFCTag2CurrentFw.getCurrentFw(), NFCBoardCatalogService.extractAllVirtualSensorsIds(NFCTag2CurrentFw.getCurrentFw()), data.getSensorDataSample(), requireContext())
    }
}