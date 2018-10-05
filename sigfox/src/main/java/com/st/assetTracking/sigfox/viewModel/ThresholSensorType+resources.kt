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

package com.st.assetTracking.sigfox.viewModel

import com.st.assetTracking.sigfox.R
import com.st.assetTracking.sigfox.model.Orientation
import com.st.assetTracking.sigfox.model.ThresholdComparison
import com.st.assetTracking.sigfox.model.ThresholdSensorType


internal val ThresholdSensorType.resourceString:Int
    get() {
        return when(this){
            ThresholdSensorType.Temperature -> R.string.sensor_temperature_name
            ThresholdSensorType.Pressure -> R.string.sensor_pressure_name
            ThresholdSensorType.Humidity -> R.string.sensor_humidity_name
            ThresholdSensorType.WakeUp -> R.string.sensor_wakeUp_name
            ThresholdSensorType.Tilt -> R.string.sensor_tilt_name
            ThresholdSensorType.Orientation -> R.string.sensor_orientation_name
        }
    }

internal val ThresholdSensorType.unitResourceString:Int
    get() {
        return when(this){
            ThresholdSensorType.Temperature -> R.string.sensor_temperature_unit
            ThresholdSensorType.Pressure -> R.string.sensor_pressure_unit
            ThresholdSensorType.Humidity -> R.string.sensor_humidity_unit
            ThresholdSensorType.WakeUp -> R.string.sensor_wakeup_unit
            ThresholdSensorType.Tilt -> R.string.sensor_without_unit
            ThresholdSensorType.Orientation -> R.string.sensor_without_unit
        }
    }


internal val ThresholdSensorType.resourceImage:Int
    get() {
        return  when(this){
            ThresholdSensorType.Temperature -> R.drawable.temperature_icon
            ThresholdSensorType.Pressure -> R.drawable.pressure_icon
            ThresholdSensorType.Humidity -> R.drawable.humidity_icon
            ThresholdSensorType.WakeUp -> R.drawable.wake_up_icon
            ThresholdSensorType.Tilt -> R.drawable.acc_event_tilt
            ThresholdSensorType.Orientation -> R.drawable.orientation_icon
        }
    }
