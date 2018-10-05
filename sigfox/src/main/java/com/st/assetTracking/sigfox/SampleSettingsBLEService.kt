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

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.util.Log
import com.st.BlueSTSDK.Manager
import com.st.BlueSTSDK.Node

private const val ACTION_WRITE_CONFIGURATION = "SampleSettingsBLEService.ACTION_WRITE_CONFIGURATION"
private const val ACTION_READ_CONFIGURATION = "SampleSettingsBLEService.ACTION_READ_CONFIGURATION"

private const val PARAM_NODE_TAG = "SampleSettingsBLEService.PARAM_NODE_TAG"
private const val PARAM_CONFIGURATION = "SampleSettingsBLEService.PARAM_CONFIGURATION"

/**
 * Service used to load and store the configuration from a node using a ble connection
 * the read data will be communicated back using a broadcast message
 */
internal class SampleSettingsBLEService : IntentService("SampleSettingsBLEService") {

    enum class State{
        CONNECTING,
        CONNECTED,
        TRANSFERRING_DATA,
        TRANSFERRING_DATA_ERROR,
        TRANSFERRING_DATA_COMPLETE,
        DISCONNECTING,
        CONNECTION_ERROR
    }

    private val broadcastManager = androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_WRITE_CONFIGURATION -> {
                val nodeTag = intent.getStringExtra(PARAM_NODE_TAG)
                val settings = intent.getParcelableExtra<com.st.assetTracking.sigfox.model.SamplingSettings>(PARAM_CONFIGURATION)
                val node = Manager.getSharedInstance().getNodeWithTag(nodeTag)
                if(node!=null)
                    handleWriteConfiguration(node, settings)
            }
            ACTION_READ_CONFIGURATION -> {
                val nodeTag = intent.getStringExtra(PARAM_NODE_TAG)
                val node = Manager.getSharedInstance().getNodeWithTag(nodeTag)
                if(node!=null){
                    handleReadConfiguration(node)
                }
            }
        }
    }


    /**
     * send the configuration to the node
     * @param node device where store the configuration
     * @param settings configuration to store into the device
     */
    private fun handleWriteConfiguration(node: Node, settings: com.st.assetTracking.sigfox.model.SamplingSettings) {
        val errorStates = listOf(Node.State.Lost,Node.State.Unreachable,Node.State.Dead)
        node.addNodeStateListener(object  : Node.NodeStateListener{
            override fun onStateChange(n: Node, newState: Node.State, prevState: Node.State) {
                when(newState){
                    Node.State.Connecting -> notifyNodeConnecting()
                    Node.State.Connected -> {
                        notifyNodeConnected()
                        writeConfiguration(n,settings)
                    }
                    in errorStates ->{
                        n.removeNodeStateListener(this)
                        notifyNodeConnectionError()
                    }
                    Node.State.Disconnecting->{
                        n.removeNodeStateListener(this)
                        notifyNodeDisconnection()
                    }

                    else -> {
                        Log.d("SampleSettingsBLE", "state:$newState")
                    }
                }//when
            }//onStateChange
        })//addNodeState
        node.connect(this)
    }

    private fun writeConfiguration(node: Node, settings: com.st.assetTracking.sigfox.model.SamplingSettings) {
        com.st.assetTracking.sigfox.comunication.CloudTrackerConsole.buildForNode(node)?.let { console ->
            notifySettingsUploadingStart()
            console.save(settings,object : com.st.assetTracking.sigfox.comunication.CloudTrackerConsole.SaveCallback{
                override fun onSuccess() {
                    notifySettingsUploadingDone()
                    node.disconnect()
                }

                override fun onError() {
                    notifySettingsUploadingError()
                    node.disconnect()
                }
            })
        }
    }


    /**
     * Read the settings from the [node], and communicate it back using the [READ_SETTINGS_ACTION]
     * event with the data inside the [NEW_SETTINGS_EXTRA] extra field
     */
    private fun handleReadConfiguration(node: Node) {
        val errorStates = listOf(Node.State.Lost,Node.State.Unreachable,Node.State.Dead)
        node.addNodeStateListener(object  : Node.NodeStateListener{
            override fun onStateChange(n: Node, newState: Node.State, prevState: Node.State) {
                when(newState){
                    Node.State.Connecting -> notifyNodeConnecting()
                    Node.State.Connected -> {
                        notifyNodeConnected()
                        readConfiguration(n)
                    }
                    in errorStates ->{
                        n.removeNodeStateListener(this)
                        notifyNodeConnectionError()
                    }
                    Node.State.Disconnecting->{
                        n.removeNodeStateListener(this)
                        notifyNodeDisconnection()
                    }

                    else -> {
                        Log.d("SampleSettingsBLE", "state:$newState")
                    }
                }//when
            }//onStateChange
        })//addNodeState
        node.connect(this)
    }

    private fun readConfiguration(node: Node) {
        com.st.assetTracking.sigfox.comunication.CloudTrackerConsole.buildForNode(node)?.let { console ->
            notifySettingsUploadingStart()
            console.load(object : com.st.assetTracking.sigfox.comunication.CloudTrackerConsole.LoadCallback{
                override fun onSuccess(settings: com.st.assetTracking.sigfox.model.SamplingSettings) {
                    notifySettingsRead(settings)
                    node.disconnect()
                }

                override fun onError() {
                    notifySettingsReadError()
                    node.disconnect()
                }

            })
        }
    }

    private fun notifySettingsRead(data: com.st.assetTracking.sigfox.model.SamplingSettings) {
        broadcastManager.sendBroadcast(buildIntentWithReadData(data))
    }

    private fun notifySettingsReadError() {
        broadcastManager.sendBroadcast(buildIntentWithState(State.TRANSFERRING_DATA_ERROR))
    }

    private fun notifyNodeConnectionError(){
        broadcastManager.sendBroadcast(buildIntentWithState(State.CONNECTION_ERROR))
    }
    private fun notifyNodeConnecting(){
        broadcastManager.sendBroadcast(buildIntentWithState(State.CONNECTING))
    }
    private fun notifyNodeConnected(){
        broadcastManager.sendBroadcast(buildIntentWithState(State.CONNECTED))
    }
    private fun notifyNodeDisconnection(){
        broadcastManager.sendBroadcast(buildIntentWithState(State.DISCONNECTING))
    }

    private fun notifySettingsUploadingStart(){
        broadcastManager.sendBroadcast(buildIntentWithState(State.TRANSFERRING_DATA))
    }

    private fun notifySettingsUploadingDone(){
        broadcastManager.sendBroadcast(buildIntentWithState(State.TRANSFERRING_DATA_COMPLETE))
    }

    private fun notifySettingsUploadingError(){
        broadcastManager.sendBroadcast(buildIntentWithState(State.TRANSFERRING_DATA_ERROR))
    }

    companion object {

        /**
         * Start the service to write the new [settings] into the [node]
         */
        @JvmStatic
        fun startWriteConfiguration(context: Context, node: Node,settings: com.st.assetTracking.sigfox.model.SamplingSettings) {
            val intent = Intent(context, SampleSettingsBLEService::class.java).apply {
                action = ACTION_WRITE_CONFIGURATION
                putExtra(PARAM_NODE_TAG, node.tag)
                putExtra(PARAM_CONFIGURATION, settings)
            }
            context.startService(intent)
        }


        /**
         * Start the service to read the settings from the [node]
         */
        @JvmStatic
        fun startReadConfiguration(context: Context, node: Node) {
            val intent = Intent(context, SampleSettingsBLEService::class.java).apply {
                action = ACTION_READ_CONFIGURATION
                putExtra(PARAM_NODE_TAG, node.tag)
            }
            context.startService(intent)
        }

        /** Action used to communicate the new service status*/
        val CHANGE_STATUS_ACTION= com.st.assetTracking.sigfox.model.SamplingSettings::class.java.name+".CHANGE_STATUS_ACTION"
        private val NEW_STATUS_EXTRA= com.st.assetTracking.sigfox.model.SamplingSettings::class.java.name+".NEW_STATUS_EXTRA"

        /** Action used to communicate the new settings read*/
        val READ_SETTINGS_ACTION= com.st.assetTracking.sigfox.model.SamplingSettings::class.java.name+".READ_SETTINGS_ACTION"
        private val NEW_SETTINGS_EXTRA= com.st.assetTracking.sigfox.model.SamplingSettings::class.java.name+".NEW_SETTINGS_EXTRA"

        /**
         * @return an intent filter able to caputre all the event generated by this service
         */
        fun getServiceIntentFilter(): IntentFilter {
            return IntentFilter().apply {
                addAction(CHANGE_STATUS_ACTION)
                addAction(READ_SETTINGS_ACTION)
            }
        }

        private fun buildIntentWithState(state: State):Intent{
            return Intent(CHANGE_STATUS_ACTION).apply {
                putExtra(NEW_STATUS_EXTRA,state)
            }
        }

        private fun buildIntentWithReadData(data: com.st.assetTracking.sigfox.model.SamplingSettings):Intent{
            return Intent(READ_SETTINGS_ACTION).apply {
                putExtra(NEW_SETTINGS_EXTRA,data)
            }
        }

        /**
         * extract the service [SampleSettingsBLEService.State] from a [CHANGE_STATUS_ACTION] intent
         * @return state or null if the intent action is not valid
         */
        fun extractStatus(intent: Intent) : State?{
            return if(intent.action == CHANGE_STATUS_ACTION) {
                intent.getSerializableExtra(NEW_STATUS_EXTRA) as State
            } else {
                null
            }
        }

        /**
         * extract the node [com.st.assetTracking.sigfox.model.SamplingSettings] from a [READ_SETTINGS_ACTION] intent
         * @return read settings or null if the intent action is not valid
         */
        fun extractReadSettings(intent: Intent) : com.st.assetTracking.sigfox.model.SamplingSettings?{
            return if(intent.action == READ_SETTINGS_ACTION) {
                intent.getParcelableExtra(NEW_SETTINGS_EXTRA)
            } else {
                null
            }
        }

    }
}
