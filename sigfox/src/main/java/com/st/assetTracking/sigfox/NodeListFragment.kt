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

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.st.BlueSTSDK.Manager
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.BlePermissionHelper
import com.st.BlueSTSDK.gui.NodeRecyclerViewAdapter

/***
 * dialog that start the ble scanning and show the list of detected nodes
 */
internal class NodeListFragment : AppCompatDialogFragment(), NodeRecyclerViewAdapter.FilterNode {

    private lateinit var nodeList: RecyclerView

    private lateinit var nodeListAdapter: NodeRecyclerViewAdapter

    private lateinit var blePermission:BlePermissionHelper

    private val mCallback = object : BlePermissionHelper.BlePermissionAcquiredCallback {
        override fun onBlePermissionAcquired() {
            startBleScan()
        }

        override fun onBlePermissionDenied() {
            Toast.makeText(this@NodeListFragment.requireContext(),
                    com.st.BlueSTSDK.R.string.LocationNotGranted,Toast.LENGTH_SHORT).show()
        }
    }

    private var onNodeSelected: NodeRecyclerViewAdapter.OnNodeSelectedListener? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_node_list, container, false)

        rootView.findViewById<View>(R.id.nodeList_refreshButton).setOnClickListener {
            startBleScan()
        }

        nodeList = rootView.findViewById(R.id.nodeList_list)
        blePermission = BlePermissionHelper(this)

        setUpDescriptionLable(rootView.findViewById(R.id.nodeList_description))

        return rootView
    }

    private fun setUpDescriptionLable(label:TextView){
        val description = extractDescriptionStringRes()
        if(description == null){
            label.visibility = View.GONE
        }else{
            label.setText(description)
        }
    }

    private fun extractDescriptionStringRes(): Int? {
        val args = arguments
        if (args?.containsKey(DESCRIPTION_KEY) == true)
            return args.getInt(DESCRIPTION_KEY)
        return null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle(R.string.nodeList_title)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val bleManager = Manager.getSharedInstance()
        nodeListAdapter = NodeRecyclerViewAdapter(bleManager.nodes,
                NodeRecyclerViewAdapter.OnNodeSelectedListener { n ->
            dismiss()
            onNodeSelected?.onNodeSelected(n)
        }, NodeRecyclerViewAdapter.FilterNode {
            true
        })
        nodeList.adapter = nodeListAdapter
        bleManager.addListener(nodeListAdapter)
        if (blePermission.checkAdapterAndPermission()) {
            mCallback.onBlePermissionAcquired()
        }

    }

    override fun onStop() {
        super.onStop()
        val bleManager = Manager.getSharedInstance()
        bleManager.removeListener(nodeListAdapter)
        bleManager.stopDiscovery()
    }

    private fun startBleScan(){
        nodeListAdapter.clear()
        Manager.getSharedInstance().apply {
            resetDiscovery()
            startDiscovery()
        }
    }

    override fun displayNode(n: Node): Boolean {
        return true
    }

    /**
     * call when we request the location permission to do a ble scan
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        blePermission.onRequestPermissionsResult(requestCode, permissions, grantResults, mCallback)
    }

    /**
     * call when we request to enable the ble connectivity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (blePermission.onActivityResult(requestCode, resultCode, data) == null) {
            super.onActivityResult(requestCode, resultCode, data)
        }

    }


    companion object {
        const val DESCRIPTION_KEY = "DESCRIPTION_KEY"

        /**
         * create an instance of the dialog
         * @param description string to display as text on the top of the dialog
         * @param onNodeSelectedListener callback called when the user select a node
         */
        fun instantiateWith(@StringRes description:Int, onNodeSelectedListener: NodeRecyclerViewAdapter.OnNodeSelectedListener? = null ): androidx.fragment.app.DialogFragment {

            val fragment = NodeListFragment()
            fragment.arguments = Bundle().apply {
                putInt(DESCRIPTION_KEY,description)
            }
            fragment.onNodeSelected = onNodeSelectedListener
            return fragment
        }
    }

}