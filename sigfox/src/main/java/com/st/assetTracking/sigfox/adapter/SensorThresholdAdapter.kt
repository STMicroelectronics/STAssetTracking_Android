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

package com.st.assetTracking.sigfox.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.st.assetTracking.sigfox.R
import com.st.assetTracking.sigfox.viewModel.EnvironmentalThresholdViewData
import com.st.assetTracking.sigfox.viewModel.OrientationThresholdViewData
import com.st.assetTracking.sigfox.viewModel.SensorThresholdViewData
import com.st.assetTracking.sigfox.viewModel.TiltThresholdViewData


internal class SensorThresholdAdapter(thresholdsList:List<SensorThresholdViewData> = emptyList()) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val ENVIRONMENTAL_VIEW = 0
        private const val ORIENTATION_VIEW = 1
        private const val TILT_VIEW = 2
    }

    var thresholds:List<SensorThresholdViewData> = thresholdsList
    set(newValue) {
        val diff = DiffUtil.calculateDiff(SensorThresholdDiff(field,newValue))
        field = newValue
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder {
        return when(type){
            ENVIRONMENTAL_VIEW->{
                val rootView = LayoutInflater.from(parent.context).inflate(R.layout.item_environmental_threshold,
                        parent,false)
                EnvironmentalThresholdViewHolder(rootView)
            }
            ORIENTATION_VIEW -> {
                val rootView = LayoutInflater.from(parent.context).inflate(R.layout.item_orientation_threshold,
                        parent,false)
                OrientationThresholdViewHolder(rootView)
            }
            TILT_VIEW -> {
                val rootView = LayoutInflater.from(parent.context).inflate(R.layout.item_tilt_threshold,
                        parent,false)
                TiltThresholdViewHolder(rootView)
            }
            else -> throw IllegalStateException("Illegal view type")
        }
    }

    override fun getItemCount(): Int {
        return thresholds.size
    }

    override fun getItemViewType(position: Int): Int {
        val data = thresholds[position]
        return when(data){
            is EnvironmentalThresholdViewData -> ENVIRONMENTAL_VIEW
            is OrientationThresholdViewData -> ORIENTATION_VIEW
            is TiltThresholdViewData -> TILT_VIEW
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val threshold = thresholds[position]
        when(viewHolder.itemViewType){
            ENVIRONMENTAL_VIEW -> {
                (viewHolder as EnvironmentalThresholdViewHolder)
                        .display(threshold as EnvironmentalThresholdViewData)
            }
            ORIENTATION_VIEW -> {
                (viewHolder as OrientationThresholdViewHolder)
                        .display(threshold as OrientationThresholdViewData)
            }
            TILT_VIEW -> {

            }
        }
    }


    class EnvironmentalThresholdViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val image:ImageView = view.findViewById(R.id.item_evn_sensor_icon)
        private val sensorName:TextView = view.findViewById(R.id.item_env_sensor_name)
        private val threshold:TextView = view.findViewById(R.id.item_evn_threshold_value)

        fun display(data: EnvironmentalThresholdViewData){
            image.setImageResource(data.iconId)
            sensorName.setText(data.sensorId)
            val unit = threshold.context.getString(data.unitStrId)
            threshold.text=String.format("%s %s %s",data.compareSymbolStr,data.valueStr,unit)
        }
    }

    class TiltThresholdViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class OrientationThresholdViewHolder(view:View): RecyclerView.ViewHolder(view){
        private val threshold:TextView = view.findViewById(R.id.item_orientation_value)
        private val orientationImage:ImageView = view.findViewById(R.id.item_orientation_icon)

        fun display(data:OrientationThresholdViewData){
            threshold.setText(data.orientationString)
            orientationImage.setImageResource(data.orientationIcon)
        }
    }
}

internal class SensorThresholdDiff(private val oldList: List<SensorThresholdViewData>,
                                   private val newList:List<SensorThresholdViewData>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }


    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}