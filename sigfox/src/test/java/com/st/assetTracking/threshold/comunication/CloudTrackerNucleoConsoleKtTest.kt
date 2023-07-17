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

import org.junit.Assert
import org.junit.Test

class CloudTrackerNucleoConsoleKtTest{

    @Test
    fun decode0Th(){
        val data = byteArrayOf(0x01,0x02,0x00)
        val parsed = data.toSamplingSettings()
        Assert.assertNotNull(parsed)
        Assert.assertEquals(1.toShort(),parsed!!.cloudSyncInterval)
        Assert.assertEquals(2.toShort(),parsed.samplingInterval)
        Assert.assertEquals(0,parsed.threshold.size)
    }

    @Test
    fun decode1Th(){
        val data = byteArrayOf(0x01,0x02,0x01,0x01,0x01,0x00,0x00)
        val parsed = data.toSamplingSettings()
        Assert.assertNotNull(parsed)
        Assert.assertEquals(1,parsed!!.threshold.size)
        Assert.assertEquals(com.st.assetTracking.threshold.model.ThresholdSensorType.Temperature,parsed.threshold[0].sensor)
        Assert.assertEquals(com.st.assetTracking.threshold.model.ThresholdComparison.BiggerOrEqual,parsed.threshold[0].comparison)
        Assert.assertEquals(0.0.toFloat(),parsed.threshold[0].threshold)
    }

    @Test
    fun decode1ThMin(){
        //val data = byteArrayOf(0x01,0x02,0x01,0x01,0xFF.toByte(),0x00,0x00)
        val data = byteArrayOf(1,15, 1, 1, 0, 0xE8.toByte(), 3)
        val parsed = data.toSamplingSettings()
        Assert.assertNotNull(parsed)
        Assert.assertEquals(1,parsed!!.threshold.size)
        Assert.assertEquals(com.st.assetTracking.threshold.model.ThresholdSensorType.Temperature,parsed.threshold[0].sensor)
        Assert.assertEquals(com.st.assetTracking.threshold.model.ThresholdComparison.Equal,parsed.threshold[0].comparison)
        Assert.assertEquals(100.0.toFloat(),parsed.threshold[0].threshold)
    }

}