package com.st.nfcSmarTag.v2.samples

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.st.nfcSmarTag.R
import com.st.smartaglibrary.v2.catalog.NfcV2Firmware
import com.st.smartaglibrary.v2.catalog.VirtualSensor
import com.st.assetTracking.data.GenericDataSample
import com.st.nfcSmarTag.v2.util.mapSensorTypeToImage
import kotlinx.android.synthetic.main.item_tag2_samples_listed.view.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Tag2SamplesListedAdapter(private val catalog: NfcV2Firmware, private val originalItems : List<GenericDataSample>, val context: Context) : RecyclerView.Adapter<Tag2SamplesListedViewHolder>() {

    private var items = originalItems.toMutableList()

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Tag2SamplesListedViewHolder {
        return Tag2SamplesListedViewHolder(LayoutInflater.from(context).inflate(R.layout.item_tag2_samples_listed, parent, false))
    }

    override fun onBindViewHolder(holder: Tag2SamplesListedViewHolder, position: Int) {
        val currentVsSample = items[position]
        val vsId = currentVsSample.id
        val catalogVS = retrieveVirtualSensorFromCatalog(catalog, vsId)
        if(catalogVS != null){
            holder.tag2SamplesSensorImage.setImageResource(mapSensorTypeToImage(catalogVS))
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

class Tag2SamplesListedViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val tag2SamplesCardView: CardView = view.tag2_samples_cv
    val tag2SamplesSensorImage: ImageView = view.tag2_samples_sensor_image
    val tag2SamplesSensorName: TextView = view.tag2_samples_sensor_name
    val tag2SamplesValue: TextView = view.tag2_samples_value
    val tag2SamplesTs: TextView = view.tag2_samples_ts
}