package com.st.assetTracking.data.ui.genericDataUI

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.st.assetTracking.data.GenericDataSample
import com.st.assetTracking.data.ui.R
import com.st.smartaglibrary.v2.catalog.NfcV2Firmware
import com.st.smartaglibrary.v2.catalog.VirtualSensor
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GenericDataListAdapter(
    private val catalog: NfcV2Firmware,
    private val originalItems : List<GenericDataSample>,
    val context: Context) : RecyclerView.Adapter<GenericDataListViewHolder>() {

    private var items = originalItems.toMutableList()

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericDataListViewHolder {
        return GenericDataListViewHolder(LayoutInflater.from(context).inflate(R.layout.item_generic_samples_list, parent, false))
    }

    override fun onBindViewHolder(holder: GenericDataListViewHolder, position: Int) {
        val currentVsSample = items[position]
        val vsId = currentVsSample.id
        val catalogVS = retrieveVirtualSensorFromCatalog(catalog, vsId)
        if(catalogVS != null){
            holder.tag2SamplesSensorImage.setImageResource(retrieveSensorImage(catalogVS))
            holder.tag2SamplesSensorName.text = catalogVS.displayName
            val df = DecimalFormat("#.##")
            val sampleValue = "${df.format(currentVsSample.value)} ${catalogVS.threshold.unit}"
            holder.tag2SamplesValue.text = sampleValue
            holder.tag2SamplesTs.text = formattedDate(currentVsSample.date) ?: "NO Time"
        }
    }

    private fun retrieveVirtualSensorFromCatalog(catalog: NfcV2Firmware, virtualSensorId: Int): VirtualSensor?{
        catalog.virtualSensors.forEach { vSc ->
            if(vSc.id == virtualSensorId){
                return vSc
            }
        }
        return null
    }

    private fun retrieveSensorImage(catalogVS: VirtualSensor): Int{
        return when (catalogVS.type) {
            "battery_percentage" -> R.drawable.battery_percentage
            "battery_voltage" -> R.drawable.battery_voltage
            "tem" -> R.drawable.sensor_temperature_icon
            "pre" -> R.drawable.sensor_pressure_icon
            "hum" -> R.drawable.sensor_humidity_icon
            "imu_acc" -> R.drawable.sensor_wake_up_icon
            "6d_acc" -> R.drawable.sensor_acc_event_orientation
            "tilt_acc" -> R.drawable.sensor_acc_event_tilt
            "acc/gyro" -> R.drawable.sensor_acc_gyro
            else -> { R.drawable.sensor_generic }
        }
    }

    private fun formattedDate(date: Date?): String? {
        if (date == null) {
            return null
        }
        val dateFormatter = SimpleDateFormat("HH:mm:ss - dd/MM", Locale.getDefault())
        return dateFormatter.format(date)
    }

    fun filterSamples(vsIds: List<Int>){
        Log.d("INDEXES TO FILTER", "Virtual Sensors IDS : $vsIds")
        items = originalItems.toMutableList()

        val newSamplesList: ArrayList<GenericDataSample> = ArrayList()

        items.forEach { currentSample ->
            vsIds.forEach { idToFilter ->
                if(currentSample.id == idToFilter){
                    newSamplesList.add(currentSample)
                }
            }
        }
        items = newSamplesList

        this.notifyDataSetChanged()
    }

    fun resetFilterSample() {
        items = originalItems.toMutableList()
        this.notifyDataSetChanged()
    }

}

class GenericDataListViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val tag2SamplesCardView: CardView = view.findViewById(R.id.generic_sample_list_cv)
    val tag2SamplesSensorImage: ImageView = view.findViewById(R.id.generic_sample_list_sensor_image)
    val tag2SamplesSensorName: TextView = view.findViewById(R.id.generic_sample_list_sensor_name)
    val tag2SamplesValue: TextView = view.findViewById(R.id.generic_sample_list_value)
    val tag2SamplesTs: TextView = view.findViewById(R.id.generic_sample_list_ts)
}