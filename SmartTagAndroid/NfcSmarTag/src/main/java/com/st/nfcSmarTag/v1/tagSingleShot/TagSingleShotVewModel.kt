/*
 * Copyright (c) 2018  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *    and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *    conditions and the following disclaimer in the documentation and/or other materials provided
 *    with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *    STMicroelectronics company nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *    in a directory whose title begins with st_images may only be used for internal purposes and
 *    shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *    icons, pictures, logos and other images that are provided with the source code in a directory
 *    whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package com.st.nfcSmarTag.v1.tagSingleShot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.st.smartaglibrary.v1.model.NFCSensorDataSample


class TagSingleShotVewModel : ViewModel(){

    private val _dataSample = MutableLiveData<NFCSensorDataSample>()
    val sensorDataSample: LiveData<NFCSensorDataSample>
        get() = _dataSample

    private val _waitingAnswer = MutableLiveData<Long>()
    val waitingAnswer: LiveData<Long>
        get() = _waitingAnswer

    private val _singleShotReadFail = MutableLiveData<Boolean>()
    val singleShotReadFail: LiveData<Boolean>
        get() = _singleShotReadFail

    fun newSample(sensorData: NFCSensorDataSample) {
        _dataSample.value = sensorData
        _waitingAnswer.value=null
        _singleShotReadFail.value=false
    }

    fun readFail(){
        _dataSample.value=null
        _singleShotReadFail.value=true
    }

    fun startWaitingAnswer(timeout:Long){
        _waitingAnswer.value=timeout
    }

    companion object {
        fun create(activity: androidx.fragment.app.FragmentActivity): TagSingleShotVewModel {
            return ViewModelProviders.of(activity).get(TagSingleShotVewModel::class.java)
        }
    }

}
