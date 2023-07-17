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

package com.st.assetTracking.threshold.comunication

import com.st.BlueSTSDK.Debug
import com.st.BlueSTSDK.Node
import com.st.assetTracking.threshold.model.SamplingSettings

/**
 * interface used to save and read the SampleSettings
 */
interface CloudTrackerConsole {

    /**
     * save the [settings], call [SaveCallback.onSuccess] when the load is completed or
     * [SaveCallback.onError] when it fails
     */
    fun save(settings: SamplingSettings, callback: SaveCallback)

    /**
     * load the settings, call [LoadCallback.onSuccess] with the new settings when the read finish
     * [LoadCallback.onError] where the read fails
     */
    fun load(callback: LoadCallback)

    interface SaveCallback {
        fun onSuccess()
        fun onError()
    }

    interface LoadCallback {
        fun onSuccess(settings: SamplingSettings)
        fun onError()
    }

    companion object {
        /**
         * create a console for store/load the settings into [node] using the ble protocol
         * @return console to use to store/load the settings or null if the node is not compatible
         */
        fun buildForNode(node: Node): CloudTrackerConsole? {
            val console: Debug = node.debug ?: return null
            return CloudTrackerNucleoConsole(console)
        }
    }

}

