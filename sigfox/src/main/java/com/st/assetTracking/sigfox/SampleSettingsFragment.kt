/*
 *  Copyright (c) 2019  STMicroelectronics – All rights reserved
 *  The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 *  Redistribution and use in source and binary forms, with or without modification,
 *  are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice, this list of conditions
 *    and the following disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright notice, this list of
 *    conditions and the following disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 *  - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *    STMicroelectronics company nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 *  - All of the icons, pictures, logos and other images that are provided with the source code
 *    in a directory whose title begins with st_images may only be used for internal purposes and
 *    shall not be redistributed to any third party or modified in any way.
 *
 *  - Any redistributions in binary form shall not include the capability to display any of the
 *    icons, pictures, logos and other images that are provided with the source code in a directory
 *    whose title begins with st_images.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 *  AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 *  OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 *  OF SUCH DAMAGE.
 */

package com.st.assetTracking.sigfox

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import com.google.android.material.textfield.TextInputLayout
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.util.Log
import android.view.*
import android.widget.*
import com.st.BlueSTSDK.gui.NodeRecyclerViewAdapter
import com.st.BlueSTSDK.gui.util.SimpleFragmentDialog
import com.st.assetTracking.sigfox.adapter.SensorThresholdAdapter
import com.st.assetTracking.sigfox.util.IntInputRangeChecker
import com.st.assetTracking.sigfox.util.shortValue
import com.st.assetTracking.sigfox.viewModel.NavigationViewModel
import com.st.assetTracking.sigfox.viewModel.SampleSettingsViewModel
import com.st.assetTracking.sigfox.viewModel.SelectSensor
import kotlin.math.abs

internal class SampleSettingsFragment : androidx.fragment.app.Fragment(){

    private lateinit var mViewModel: SampleSettingsViewModel
    private val mNavigator by lazy { ViewModelProviders.of(requireActivity()).get(NavigationViewModel::class.java) }

    private lateinit var mSamplingInterval: com.google.android.material.textfield.TextInputLayout
    private lateinit var mCloudSyncIntervalSelector: Spinner
    private lateinit var mThresholdList: androidx.recyclerview.widget.RecyclerView
    private lateinit var mThresholdListAdapter:SensorThresholdAdapter
    private lateinit var mLoadingBar:ProgressBar

    private val settingsStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent==null)
                return
            when(intent.action){
                SampleSettingsBLEService.CHANGE_STATUS_ACTION -> {
                    val state = SampleSettingsBLEService.extractStatus(intent)
                    Log.d("Sate","State:$state")
                    when(state){

                        SampleSettingsBLEService.State.TRANSFERRING_DATA_COMPLETE -> {
                            mViewModel.onTransferComplete()
                        }
                        SampleSettingsBLEService.State.CONNECTION_ERROR -> {
                            mViewModel.onConnectionError()
                        }
                        SampleSettingsBLEService.State.TRANSFERRING_DATA_ERROR -> {
                            mViewModel.onTransferError()
                        }

                        SampleSettingsBLEService.State.CONNECTING ->{
                            mViewModel.boardConnecting()
                        }
                        SampleSettingsBLEService.State.CONNECTED,
                        SampleSettingsBLEService.State.TRANSFERRING_DATA,
                        SampleSettingsBLEService.State.DISCONNECTING->{
                            mViewModel.boardDisconnect()
                        }
                        null -> return
                    }
                    //Toast.makeText(context,"newState: $state",Toast.LENGTH_SHORT).show()
                }//change status
                SampleSettingsBLEService.READ_SETTINGS_ACTION -> {
                    SampleSettingsBLEService.extractReadSettings(intent)?.let { newSettings ->
                        mViewModel.samplingSettings = newSettings
                    }//let
                }//read action
            }//when
        }//onReceive
    }//broadcast receiver


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProviders.of(requireActivity()).get(SampleSettingsViewModel::class.java)

        mViewModel.samplingInterval.observe(viewLifecycleOwner, Observer {interval  ->
            interval?.let {
                mSamplingInterval.shortValue=it
            }
        })

        mViewModel.cloudSyncInterval.observe(viewLifecycleOwner, Observer { interval ->
            interval?.let{
                //mCloudSyncInterval.shortValue=it
                @Suppress("UNCHECKED_CAST")
                val adapter = mCloudSyncIntervalSelector.adapter as ArrayAdapter<Int>
                mCloudSyncIntervalSelector.setSelection(adapter.getNearestIndex(interval.toInt()))
            }
        })

        mViewModel.sensorThresholds.observe(viewLifecycleOwner, Observer { thresholds ->
            thresholds?.let {
                mThresholdListAdapter.thresholds = it
            }
        })

        mViewModel.showProgressBar.observe(viewLifecycleOwner, Observer { show ->
            show?.let {
                mLoadingBar.visibility = if(show) View.VISIBLE else View.INVISIBLE
            }
        })

        mViewModel.dialogMessage.observe(viewLifecycleOwner, Observer { messageId ->
            if(messageId == null)
                return@Observer

            displayDialogWithMessage(messageId)

        })

        ItemTouchHelper(SwipeToDeleteThreshold(mViewModel)).attachToRecyclerView(mThresholdList)
    }

    private fun displayDialogWithMessage(@StringRes messageId: Int) {
        val dialog = SimpleFragmentDialog.newInstance(messageId)
        dialog.show(childFragmentManager,"Dialog")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_sample_settings, container, false)


        mThresholdList =view.findViewById(R.id.settings_thresholdList)
        mCloudSyncIntervalSelector = view.findViewById(R.id.cloud_sync_selector)
        mSamplingInterval = view.findViewById(R.id.settings_sampling_layout)

        view.findViewById<View>(R.id.settings_saveButton).setOnClickListener {
            mViewModel.setSamplingInterval(mSamplingInterval.shortValue)
            mViewModel.setCloudSyncInterval((mCloudSyncIntervalSelector.selectedItem as Int).toShort())
            showNodeSelector(R.string.nodeList_actionDescription_store,
                    NodeRecyclerViewAdapter.OnNodeSelectedListener {node ->
                SampleSettingsBLEService.startWriteConfiguration(requireContext(), node, mViewModel.samplingSettings)
            })
        }

        view.findViewById<View>(R.id.settings_add_threshold).setOnClickListener {
            mNavigator.moveTo(SelectSensor)
        }

        view.findViewById<View>(R.id.settings_loadButton).setOnClickListener {
            showNodeSelector(R.string.nodeList_actionDescription_load,
                    NodeRecyclerViewAdapter.OnNodeSelectedListener {node ->
                SampleSettingsBLEService.startReadConfiguration(requireContext(), node)
            })
        }

        setupCloudSyncInterval(mCloudSyncIntervalSelector)

        mThresholdListAdapter = SensorThresholdAdapter()
        mThresholdList.adapter = mThresholdListAdapter

        mSamplingInterval.editText?.
                addTextChangedListener(IntInputRangeChecker(mSamplingInterval, READ_SENSOR_RANGE_MIN))

        mLoadingBar = view.findViewById(R.id.settings_loadingBar)

        return view
    }

    private fun setupCloudSyncInterval(selector:Spinner){
        val values = resources.getIntArray(R.array.setting_cloudSyncInterval_values).toTypedArray()
        val valuesAdapter =ArrayAdapter<Int>(requireContext(),android.R.layout.simple_spinner_item,values).also {adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        selector.apply {
            adapter = valuesAdapter
            setSelection(0)
        }
    }

    private fun showNodeSelector(@StringRes description:Int, onSelect: NodeRecyclerViewAdapter.OnNodeSelectedListener){
        val dialog = NodeListFragment.instantiateWith(description,onSelect)
        dialog.show(childFragmentManager, CHOOSE_NODE_DIALOG_TAG)
    }


    override fun onResume() {
        super.onResume()
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(settingsStateReceiver,
                        SampleSettingsBLEService.getServiceIntentFilter())
    }

    override fun onPause() {
        super.onPause()
        androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(settingsStateReceiver)
    }

    companion object {

        private val CHOOSE_NODE_DIALOG_TAG = SampleSettingsFragment::class.java.name+".CHOOSE_NODE_DIALOG_TAG"
        private val READ_SENSOR_RANGE_MIN:ClosedRange<Int> = 1..60
        @JvmStatic
        fun newInstance() = SampleSettingsFragment()
    }
}

internal fun ArrayAdapter<Int>.getNearestIndex(value:Int):Int{
    var minDiff=Int.MAX_VALUE
    var minIndex=-1
    for (i in 0 until count) {
        val diff = abs(value-(getItem(i)?:Int.MAX_VALUE))
        if(diff<minDiff){
            minIndex = i
            minDiff = diff
        }
    }
    return minIndex
}

internal class SwipeToDeleteThreshold(private val viewModel: SampleSettingsViewModel) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(recyclerView: androidx.recyclerview.widget.RecyclerView, item: androidx.recyclerview.widget.RecyclerView.ViewHolder): Int {
        return makeMovementFlags(0, ItemTouchHelper.START or ItemTouchHelper.END)
    }

    override fun onMove(recyclerView: androidx.recyclerview.widget.RecyclerView, eventStart: androidx.recyclerview.widget.RecyclerView.ViewHolder, eventStop: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(item: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
        viewModel.removeSensorThresholdWithIndex(item.adapterPosition)
    }

}