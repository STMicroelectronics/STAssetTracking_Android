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

package com.st.assetTracking.data.ui

import android.os.Bundle
import androidx.annotation.DrawableRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.st.assetTracking.data.AccelerationEvent
import com.st.assetTracking.data.EventDataSample
import com.st.assetTracking.data.Orientation
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Locale


class EventDataListFragment : Fragment() {

    private lateinit var eventListView: RecyclerView

    private lateinit var eventList: List<EventDataSample>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventList = arguments!!.getParcelableArrayList(EVENT_LIST_KEY)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_tag_event_data, container, false)
        eventListView = rootView.findViewById(R.id.eventSample_ListView)
        eventListView.adapter = EventAdapter(eventList.reversed())
        return rootView
    }

    class EventAdapter(private val sampleList: List<EventDataSample>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return if (viewType == VIEW_HOLDER_EVENT_VIEW) {
                val view = inflater
                        .inflate(R.layout.item_event_data, parent, false)
                EventViewHolder(view)
            } else {
                val view = inflater
                        .inflate(R.layout.item_event_empty, parent, false)
                EmptyViewHolder(view)
            }
        }

        override fun getItemCount(): Int {
            return if (sampleList.isEmpty()) 1 else sampleList.size
        }

        override fun getItemViewType(position: Int): Int {
            return if (sampleList.isEmpty()) VIEW_HOLDER_EMPTY_VIEW else VIEW_HOLDER_EVENT_VIEW
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is EventViewHolder -> {
                    val data = sampleList[position]
                    holder.display(data)
                }
            }
        }

        companion object {

            private const val VIEW_HOLDER_EMPTY_VIEW = 1
            private const val VIEW_HOLDER_EVENT_VIEW = 2

            class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view)

            class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                private val date: TextView = itemView.findViewById(R.id.itemEvent_dateText)
                private val orientationImg: ImageView = itemView.findViewById(R.id.itemEvent_orientationImg)
                private val eventTypeImg: ImageView = itemView.findViewById(R.id.itemEvent_eventImg)
                private val eventTypeText: TextView = itemView.findViewById(R.id.itemEvent_eventText)
                private val eventVibrationText: TextView = itemView.findViewById(R.id.itemEvent_vibrationText)

                private fun setOrientationImage(currentOrientation: Orientation) {
                    if (currentOrientation == Orientation.UNKNOWN) {
                        orientationImg.visibility = View.INVISIBLE
                        return
                    }

                    orientationImg.visibility = View.VISIBLE
                    @DrawableRes val icon = when (currentOrientation) {
                        Orientation.UP_LEFT -> R.drawable.sensor_acc_event_orientation_up_left
                        Orientation.UP_RIGHT -> R.drawable.sensor_acc_event_orientation_up_right
                        Orientation.DOWN_LEFT -> R.drawable.sensor_acc_event_orientation_down_left
                        Orientation.DOWN_RIGHT -> R.drawable.sensor_acc_event_orientation_down_right
                        Orientation.TOP -> R.drawable.sensor_acc_event_orientation_top
                        Orientation.BOTTOM -> R.drawable.sensor_acc_event_orientation_bottom
                        Orientation.UNKNOWN -> R.drawable.sensor_acc_event_none
                    }
                    orientationImg.setImageResource(icon)

                }

                fun display(data: EventDataSample) {
                    date.text = DATE_FORMATTER.format(data.date)
                    setOrientationImage(data.currentOrientation)
                    setEventImage(data.events)
                    setEventString(data.events, data.currentOrientation)
                    setVibration(data.acceleration)
                }

                private fun setVibration(vibration: Int?) {
                    if (vibration != null) {
                        eventVibrationText.visibility = View.VISIBLE
                        val ctx = eventTypeText.context
                        eventVibrationText.text = ctx.getString(R.string.eventItem_vibrationFormat, vibration)
                    } else {
                        eventVibrationText.visibility = View.INVISIBLE
                    }
                }

                private fun setEventString(events: Array<AccelerationEvent>, currentOrientation: Orientation?) {
                    val ctx = eventTypeText.context
                    if(currentOrientation != null && currentOrientation != Orientation.UNKNOWN){
                        val accEvent = "${events.joinToString()} $currentOrientation"
                        eventTypeText.text = ctx.getString(R.string.eventItem_eventFormat, accEvent)
                    }else {
                        eventTypeText.text = ctx.getString(R.string.eventItem_eventFormat, events.joinToString())
                    }
                }

                private fun containsOnlyOrientationEvent(events: Array<AccelerationEvent>) =
                        (events.contains(AccelerationEvent.ORIENTATION) && events.size == 1)

                private fun setEventImage(events: Array<AccelerationEvent>) {
                    var event: AccelerationEvent? = AccelerationEvent.ORIENTATION
                    if (!containsOnlyOrientationEvent(events)) {
                        event = events.firstOrNull { it != AccelerationEvent.ORIENTATION }
                    }

                    if (event != null) {
                        @DrawableRes val icon = when (event) {
                            AccelerationEvent.ACCELERATION_WAKE_UP -> R.drawable.sensor_acc_event_wake_up
                            AccelerationEvent.ORIENTATION -> R.drawable.sensor_acc_event_orientation
                            AccelerationEvent.SINGLE_TAP -> R.drawable.sensor_acc_event_tap_single
                            AccelerationEvent.DOUBLE_TAP -> R.drawable.sensor_acc_event_tap_double
                            AccelerationEvent.FREE_FALL -> R.drawable.sensor_acc_event_free_fall
                            AccelerationEvent.ACCELERATION_TILT_35 -> R.drawable.sensor_acc_event_tilt
                        }
                        eventTypeImg.setImageResource(icon)
                        eventTypeImg.visibility = View.VISIBLE
                    } else {
                        eventTypeImg.visibility = View.INVISIBLE
                    }
                }

            }

            private val DATE_FORMATTER = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
        }

    }

    companion object {

        fun createWith(data: List<EventDataSample>): Fragment {
            val fragment = EventDataListFragment()
            val arrayList = ArrayList(data)
            fragment.arguments = Bundle().apply {
                putParcelableArrayList(EVENT_LIST_KEY, arrayList)
            }
            return fragment
        }

        private val EVENT_LIST_KEY = "EVENT_LIST_KEY"
    }
}