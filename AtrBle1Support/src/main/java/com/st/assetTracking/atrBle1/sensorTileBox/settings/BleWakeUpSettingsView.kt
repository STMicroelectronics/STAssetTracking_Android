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

package com.st.assetTracking.atrBle1.sensorTileBox.settings

import android.content.Context
import com.google.android.material.textfield.TextInputLayout
import android.util.AttributeSet
import android.view.View
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.st.assetTracking.atrBle1.R
import com.st.assetTracking.atrBle1.sensorTileBox.util.InputChecker
import com.st.assetTracking.atrBle1.sensorTileBox.util.floatValue

class BleWakeUpSettingsView : FrameLayout {

    private val enableCheckBox by lazy { findViewById<CompoundButton>(R.id.confSensor_enableCheckBox) }

    private lateinit var maxTextLayout: TextInputLayout
    private lateinit var tvThInfo: TextView

    var isSensorEnabled: Boolean
        get() = enableCheckBox.isChecked
        set(value) {
            enableCheckBox.isChecked=value
        }

    var maxThreshold : Float?
        get() = maxTextLayout.floatValue
        set(value) {
            maxTextLayout.floatValue = value
        }

    var showThreshold: Boolean = false
        set(value) {
            field=value
            updateThresholdVisibility()
        }

    private var hideMaxTh:Boolean = false

    var validRange:ClosedRange<Float>?=null

    private fun updateThresholdVisibility(){
        val thresholdAreVisible = showThreshold and isSensorEnabled
        updateThresholdVisibility(maxTextLayout, tvThInfo, thresholdAreVisible,hideMaxTh)
    }


    private fun updateThresholdVisibility(text: TextInputLayout, tvInfo: TextView, thVisible: Boolean, hideTh:Boolean){
        if(thVisible){
            if(hideTh){
                text.visibility = View.INVISIBLE
                tvInfo.visibility = View.GONE
            }else {
                text.visibility = View.VISIBLE
                tvInfo.visibility = View.VISIBLE
            }
        }else{
            text.visibility = View.GONE
            tvInfo.visibility = View.GONE
        }
    }


    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs, defStyle)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        inflate(context, R.layout.view_configure_wakeup, this)

        tvThInfo = findViewById(R.id.wakeUpCaption)

        val a = context.obtainStyledAttributes(
                attrs, R.styleable.SensorSettingsView, defStyle, 0)

        if (a.hasValue(R.styleable.SensorSettingsView_configureSensorName)) {
            val sensorName = findViewById<TextView>(R.id.confSensor_name)
            sensorName.text = a.getText(R.styleable.SensorSettingsView_configureSensorName)
        }

        if (a.hasValue(R.styleable.SensorSettingsView_configureSensorImg)) {
            val img = findViewById<ImageView>(R.id.confSensor_image)
            img.setImageResource(a.getResourceId(R.styleable.SensorSettingsView_configureSensorImg, 0))
        }

        setUpMaxValueChecker()

        hideMaxTh = a.getBoolean(R.styleable.SensorSettingsView_configureHideMaxTh,false)
        //hideMinTh = a.getBoolean(R.styleable.SensorSettingsView_configureHideMinTh,false)

        val sensorUnit = a.getString(R.styleable.SensorSettingsView_configureSensorUnit)

        maxTextLayout.hint = context.getString(R.string.confSensor_thHint_format,sensorUnit)

        enableCheckBox.setOnCheckedChangeListener { _, _ ->
            updateThresholdVisibility()
        }

        a.recycle()
    }

    private fun setUpMaxValueChecker() {
        maxTextLayout = findViewById(R.id.confSensor_wakeUpTextLayout)
        maxTextLayout.editText?.addTextChangedListener(RangeInputChecker(maxTextLayout))
    }

    inner class RangeInputChecker (val text: TextInputLayout) : InputChecker(text){

        private fun checkBiggerThanMin():Boolean{
            val min = validRange?.start ?: return true // no range = valid input
            return text.floatValue ?: Float.MIN_VALUE >= min
        }

        private fun checkSmallerThanMax():Boolean{
            val max = validRange?.endInclusive ?: return true// no range = valid input
            return text.floatValue ?: Float.MIN_VALUE <= max
        }

        override fun validate(input: String): Boolean {
            return checkBiggerThanMin() && checkSmallerThanMax()
        }

        override fun getErrorString(): String {
            if(!checkBiggerThanMin())
                return context.getString(R.string.confSensor_minThError_Format,validRange?.start)
            else
                return context.getString(R.string.confSensor_maxThError_Format,validRange?.endInclusive)
        }

    }


}
