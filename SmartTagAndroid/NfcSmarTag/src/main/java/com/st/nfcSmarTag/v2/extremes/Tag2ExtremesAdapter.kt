package com.st.nfcSmarTag.v2.extremes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.st.nfcSmarTag.R
import com.st.nfcSmarTag.v2.util.mapSensorTypeToImage
import com.st.smartaglibrary.v2.catalog.NfcV2Firmware
import com.st.smartaglibrary.v2.catalog.VirtualSensor
import com.st.smartaglibrary.v2.model.MaxVirtualSensorValue
import com.st.smartaglibrary.v2.model.MinVirtualSensorValue
import com.st.smartaglibrary.v2.model.VirtualSensorMinMax
import kotlinx.android.synthetic.main.item_tag2_extremes.view.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class Tag2ExtremesAdapter(private val catalog: NfcV2Firmware, val items : List<VirtualSensorMinMax>, val context: Context) : RecyclerView.Adapter<ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_tag2_extremes, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentVsMinMax = items[position]
        val vsId = currentVsMinMax.id
        if(vsId != null) {
            val catalogVS = retrieveVirtualSensorFromCatalog(catalog, vsId)
            if(catalogVS != null){
                holder.tag2ExtremesSensorImage.setImageResource(mapSensorTypeToImage(catalogVS))
                holder.tag2ExtremesSensorName.text = catalogVS.displayName

                val minValue = "${minValueStr(currentVsMinMax.minValue)} ${catalogVS.threshold.unit}"
                val maxValue = "${maxValueStr(currentVsMinMax.maxValue)} ${catalogVS.threshold.unit}"

                holder.tag2ExtremesMinValue.text = minValue
                holder.tag2ExtremesMaxValue.text = maxValue
                holder.tag2ExtremesMinTs.text = minTsStr(currentVsMinMax.minValue)
                holder.tag2ExtremesMaxTs.text = maxTsStr(currentVsMinMax.maxValue)

                checkMinMaxVisibility(holder, currentVsMinMax)
            }
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

    private fun minValueStr(vsMin: MinVirtualSensorValue?): String {
        if(vsMin != null) {
            if(vsMin.value != null) {
                return formattedValue(vsMin.value!!)
            }
        }
        return "NO Value"
    }

    private fun maxValueStr(vsMax: MaxVirtualSensorValue?): String{
        if(vsMax != null) {
            if(vsMax.value != null) {
                return formattedValue(vsMax.value!!)
            }
        }
        return "NO Value"
    }

    private fun minTsStr(vsMin: MinVirtualSensorValue?): String{
        return formattedDate(vsMin?.timestamp) ?: "NO Time"
    }

    private fun maxTsStr(vsMax: MaxVirtualSensorValue?): String{
        return formattedDate(vsMax?.timestamp) ?: "NO Time"
    }

    private fun checkMinMaxVisibility(holder: ViewHolder, currentVsMinMax: VirtualSensorMinMax){
        if(currentVsMinMax.minValue == null && currentVsMinMax.maxValue == null){
            holder.tag2ExtremesCardView.visibility = View.GONE
        } else if(currentVsMinMax.minValue != null && currentVsMinMax.maxValue == null){
            holder.tag2ExtremesMaxll.visibility = View.GONE
        } else if(currentVsMinMax.minValue == null && currentVsMinMax.maxValue != null){
            holder.tag2ExtremesMinll.visibility = View.GONE
        }
    }

    private fun formattedValue(value: Double): String {
        val df = DecimalFormat("#.##")
        return df.format(value).toString()
    }

    private fun formattedDate(date: Date?): String? {
        if (date == null) {
            return null
        }
        val dateFormatter = SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault())
        return dateFormatter.format(date)
    }

}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val tag2ExtremesCardView: CardView = view.tag2_extremes_cv
    val tag2ExtremesMinll: LinearLayout = view.tag2_extremes_min_ll
    val tag2ExtremesMaxll: LinearLayout = view.tag2_extremes_max_ll
    val tag2ExtremesSensorImage: ImageView = view.tag2_extremes_sensor_image
    val tag2ExtremesSensorName: TextView = view.tag2_extremes_sensor_name
    val tag2ExtremesMinValue: TextView = view.tag2_extremes_min
    val tag2ExtremesMaxValue: TextView = view.tag2_extremes_max
    val tag2ExtremesMinTs: TextView = view.tag2_extremes_min_ts
    val tag2ExtremesMaxTs: TextView = view.tag2_extremes_max_ts
}