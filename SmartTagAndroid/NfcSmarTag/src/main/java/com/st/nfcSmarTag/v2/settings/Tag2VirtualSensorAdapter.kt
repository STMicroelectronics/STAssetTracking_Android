package com.st.nfcSmarTag.v2.settings

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.st.nfcSmarTag.R
import com.st.nfcSmarTag.util.isVisible
import com.st.nfcSmarTag.v2.util.mapSensorTypeToImage
import com.st.smartaglibrary.v2.catalog.EnumStringValues
import com.st.smartaglibrary.v2.catalog.NfcV2Firmware
import com.st.smartaglibrary.v2.catalog.VirtualSensor
import com.st.smartaglibrary.v2.model.VirtualSensorConfiguration
import kotlinx.android.synthetic.main.item_tag2_virtual_sensor.view.*
import java.lang.Double.parseDouble
import java.text.DecimalFormat

class Tag2VirtualSensorAdapter(private val catalog: NfcV2Firmware, var items : ArrayList<VirtualSensorConfiguration>, val context: Context) : RecyclerView.Adapter<ViewHolder>() {

    private var validConfiguration: Boolean = true

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return catalog.virtualSensors.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_tag2_virtual_sensor, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val catalogVS = catalog.virtualSensors[position]

        holder.tag2VsSensorImage.setImageResource(mapSensorTypeToImage(catalogVS))
        holder.tag2VsSensorName.text = catalogVS.displayName
        holder.tag2VsSensorDetailedName.text = catalogVS.sensorName

        items.forEach { vs ->
            if(vs.id == catalogVS.id) {

                holder.tag2VsTh1Unit.text = catalogVS.threshold.unit
                holder.tag2VsTh2Unit.text = catalogVS.threshold.unit

                holder.tag2VsEnabled.isChecked = vs.enabled
                if (!(vs.enabled)) {
                    setVSLayoutEnabledOrNot(holder, vs.enabled)
                }

                holder.tag2VsEnabled.setOnClickListener {
                    if (holder.tag2VsEnabled.isChecked) {
                        vs.enabled = true
                        setVSLayoutEnabledOrNot(holder, true)
                    } else {
                        vs.enabled = false
                        setVSLayoutEnabledOrNot(holder, false)
                    }
                }

                if (isEnumRow(catalogVS)) {
                    /** Build Enum Row */
                    buildEnumRow(holder, catalogVS, vs)

                    addThUsageTypeForEnumWatcher(holder, position, catalogVS)
                    addTh1EnumWatcher(holder, position, catalogVS.threshold.thLow.enumStringValues)
                    addTh2EnumWatcher(holder, position, catalogVS.threshold.thHigh?.enumStringValues)

                    configureThresholdsMod(holder, catalogVS, vs)
                } else {
                    /** Build Standard Row */
                    configureThresholdsMod(holder, catalogVS, vs)
                    configureThresholdsMinMaxValue(holder, catalogVS, vs)

                    addRecordWhenWatcher(holder, position, catalogVS)
                    addTh1Watcher(holder, position, catalogVS)
                    addTh2Watcher(holder, position, catalogVS)
                }
            }
        }
    }

    private fun isEnumRow(catalogVS: VirtualSensor): Boolean{
        return catalogVS.threshold.thLow.format.equals("enum_string")
    }

    private fun buildEnumRow(holder: ViewHolder, catalogVS: VirtualSensor, vs: VirtualSensorConfiguration) {
        holder.tag2VsTh1.visibility = View.GONE
        holder.tag2VsTh2.visibility = View.GONE

        if(catalogVS.threshold.thLow.enumStringValues != null){
            if(catalogVS.threshold.thLow.enumStringValues?.isNotEmpty() == true) {
                holder.tag2VsTh1Enum.visibility = View.VISIBLE

                val items = catalogVS.threshold.thLow.enumStringValues!!
                val adapter: ArrayAdapter<EnumStringValues> =
                    ArrayAdapter(context, android.R.layout.simple_list_item_1, items)
                holder.tag2VsTh1Enum.adapter = adapter

                val enumStringValues = catalogVS.threshold.thLow.enumStringValues!!
                for (i in enumStringValues.indices) {
                    if (enumStringValues[i].value == vs.thMin?.toInt()) {
                        holder.tag2VsTh1Enum.setSelection(i)
                    }
                }
            }
        }

        if(catalogVS.threshold.thHigh != null){
            if(catalogVS.threshold.thHigh?.enumStringValues != null){
                if (catalogVS.threshold.thHigh?.enumStringValues?.isNotEmpty() == true){
                    holder.tag2VsTh2Enum.visibility = View.VISIBLE

                    val items = catalogVS.threshold.thHigh?.enumStringValues!!
                    val adapter: ArrayAdapter<EnumStringValues> = ArrayAdapter(context, android.R.layout.simple_list_item_1, items)
                    holder.tag2VsTh2Enum.adapter = adapter

                    val enumStringValues = catalogVS.threshold.thHigh?.enumStringValues!!
                    for (i in enumStringValues.indices){
                        if(enumStringValues[i].value == vs.thMax?.toInt()){
                            holder.tag2VsTh2Enum.setSelection(i)
                        }
                    }
                }
            }
        }
    }

    private fun setVSLayoutEnabledOrNot(holder: ViewHolder, vsEnabled: Boolean) {
        holder.tag2VsSensorName.isEnabled = vsEnabled
        holder.tag2VsSensorDetailedName.isEnabled = vsEnabled
        holder.tag2VsRadioGroupTh1Th2.isEnabled = vsEnabled
        holder.tag2VsInfoRecord.isEnabled = vsEnabled
        holder.tag2VsRadioButtonOutOfRange.isEnabled = vsEnabled
        holder.tag2VsRadioButtonInRange.isEnabled = vsEnabled
        holder.tag2VsRadioButtonLess.isEnabled = vsEnabled
        holder.tag2VsRadioButtonMore.isEnabled = vsEnabled
        holder.tag2VsTh1TextInputLayout.isEnabled = vsEnabled
        holder.tag2VsTh2TextInputLayout.isEnabled = vsEnabled
        holder.tag2VsTh1.isEnabled = vsEnabled
        holder.tag2VsTh2.isEnabled = vsEnabled
        holder.tag2VsTh1Enum.isEnabled = vsEnabled
        holder.tag2VsTh2Enum.isEnabled = vsEnabled
    }

    private fun configureThresholdsMod(holder: ViewHolder, catalogVS: VirtualSensor, virtualSensor: VirtualSensorConfiguration){
        if(catalogVS.threshold.thHigh == null){
            holder.tag2VsRadioButtonOutOfRange.isVisible = false
            holder.tag2VsRadioButtonInRange.isVisible = false
        }

        when (virtualSensor.thUsageType) {
            0 -> {
                holder.tag2VsRadioButtonOutOfRange.isChecked = true
            }
            1 -> {
                holder.tag2VsRadioButtonInRange.isChecked = true
            }
            2 -> {
                holder.tag2VsRadioButtonLess.isChecked = true
                holder.tag2VsTh2.visibility = View.GONE
                holder.tag2VsTh2Enum.visibility = View.GONE
                holder.tag2VsTh2Unit.visibility = View.GONE
            }
            3 -> {
                holder.tag2VsRadioButtonMore.isChecked = true
                holder.tag2VsTh2.visibility = View.GONE
                holder.tag2VsTh2Enum.visibility = View.GONE
                holder.tag2VsTh2Unit.visibility = View.GONE
            }
        }
    }

    private fun configureThresholdsMinMaxValue(holder: ViewHolder, catalogVS: VirtualSensor, virtualSensor: VirtualSensorConfiguration){
        if(virtualSensor.thMin != null) {
            holder.tag2VsTh1.setText(formattedValue(virtualSensor.thMin!!))
        }
        if(virtualSensor.thMax != null) {
            holder.tag2VsTh2.setText(formattedValue(virtualSensor.thMax!!))
        }
    }

    private fun addRecordWhenWatcher(holder: ViewHolder, position: Int, catalogVS: VirtualSensor){
        holder.tag2VsRadioGroupTh1Th2.setOnCheckedChangeListener { group, checkedId ->
            if (items[position].id == catalogVS.id) {
                when (checkedId) {
                    holder.tag2VsRadioButtonOutOfRange.id -> {
                        items[position].thUsageType = 0
                        holder.tag2VsTh2.visibility = View.VISIBLE
                        holder.tag2VsTh2Unit.visibility = View.VISIBLE
                    }
                    holder.tag2VsRadioButtonInRange.id -> {
                        items[position].thUsageType = 1
                        holder.tag2VsTh2.visibility = View.VISIBLE
                        holder.tag2VsTh2Unit.visibility = View.VISIBLE
                    }
                    holder.tag2VsRadioButtonLess.id -> {
                        items[position].thUsageType = 2
                        holder.tag2VsTh2.visibility = View.GONE
                        holder.tag2VsTh2Unit.visibility = View.GONE
                    }
                    holder.tag2VsRadioButtonMore.id -> {
                        items[position].thUsageType = 3
                        holder.tag2VsTh2.visibility = View.GONE
                        holder.tag2VsTh2Unit.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun addThUsageTypeForEnumWatcher(holder: ViewHolder, position: Int, catalogVS: VirtualSensor){
        holder.tag2VsRadioGroupTh1Th2.setOnCheckedChangeListener { _, checkedId ->
            if (items[position].id == catalogVS.id) {
                when (checkedId) {
                    holder.tag2VsRadioButtonOutOfRange.id -> {
                        items[position].thUsageType = 0
                        holder.tag2VsTh2Enum.visibility = View.VISIBLE
                        holder.tag2VsTh2Unit.visibility = View.VISIBLE
                    }
                    holder.tag2VsRadioButtonInRange.id -> {
                        items[position].thUsageType = 1
                        holder.tag2VsTh2Enum.visibility = View.VISIBLE
                        holder.tag2VsTh2Unit.visibility = View.VISIBLE
                    }
                    holder.tag2VsRadioButtonLess.id -> {
                        items[position].thUsageType = 2
                        items[position].thMax = null
                        holder.tag2VsTh2Enum.visibility = View.GONE
                        holder.tag2VsTh2Unit.visibility = View.GONE
                    }
                    holder.tag2VsRadioButtonMore.id -> {
                        items[position].thUsageType = 3
                        items[position].thMax = null
                        holder.tag2VsTh2Enum.visibility = View.GONE
                        holder.tag2VsTh2Unit.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun addTh1Watcher(holder: ViewHolder, position: Int, catalogVS: VirtualSensor){
        holder.tag2VsTh1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(items[position].id == catalogVS.id){
                    if(catalogVS.threshold.min != null){
                        if(catalogVS.threshold.max != null){
                            if(isNumeric(s.toString())){
                                val currentValueRange = catalogVS.threshold.min!!.toFloat()..catalogVS.threshold.max!!.toFloat()
                                if (!(validateInputNum(s.toString().toFloat(), currentValueRange))){
                                    validConfiguration = false
                                    holder.tag2VsTh1InvalidMessage.visibility = View.VISIBLE
                                } else {
                                    validConfiguration = true
                                    holder.tag2VsTh1InvalidMessage.visibility = View.GONE
                                    items[position].thMin = s.toString().toDouble()
                                }
                            } else {
                                validConfiguration = false
                                holder.tag2VsTh1InvalidMessage.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        })
    }

    private fun addTh2Watcher(holder: ViewHolder, position: Int, catalogVS: VirtualSensor){
        holder.tag2VsTh2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(items[position].id == catalogVS.id){
                    if(catalogVS.threshold.min != null){
                        if(catalogVS.threshold.max != null){
                            if(isNumeric(s.toString())){
                                val currentValueRange = catalogVS.threshold.min!!.toFloat()..catalogVS.threshold.max!!.toFloat()
                                if (!(validateInputNum(s.toString().toFloat(), currentValueRange))){
                                    validConfiguration = false
                                    holder.tag2VsTh2InvalidMessage.visibility = View.VISIBLE
                                } else {
                                    validConfiguration = true
                                    holder.tag2VsTh2InvalidMessage.visibility = View.GONE
                                    items[position].thMax = s.toString().toDouble()
                                }
                            } else {
                                validConfiguration = false
                                holder.tag2VsTh2InvalidMessage.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        })
    }

    private fun addTh1EnumWatcher(holder: ViewHolder, position: Int, enumStringValues: List<EnumStringValues>?){
        holder.tag2VsTh1Enum.selected {
            validConfiguration = true
            if(enumStringValues != null) {
                if(position <= items.size - 1) {
                    items[position].thMin = (enumStringValues[it].value).toDouble()
                }
            }
        }
    }

    private fun addTh2EnumWatcher(holder: ViewHolder, position: Int, enumStringValues: List<EnumStringValues>?){
        holder.tag2VsTh2Enum.selected {
            validConfiguration = true
            if(enumStringValues != null) {
                items[position].thMax = (enumStringValues[it].value).toDouble()
            }
        }
    }

    /* Support Function to get easily spinner selection */
    fun Spinner.selected(action: (position:Int) -> Unit) {
        this.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                action(position)
            }
        }
    }

    fun isNumeric(toCheck: String): Boolean {
        var isNumeric = true
        try {
            parseDouble(toCheck)
        } catch (e: NumberFormatException) {
            isNumeric = false
        }
        return isNumeric
    }

    fun validateInputNum(input: Float, range: ClosedFloatingPointRange<Float>): Boolean {
        return range.contains(input)
    }

    fun getVirtualSensorConfiguration(): ArrayList<VirtualSensorConfiguration>? {
        return if(validConfiguration)
            items
        else
            null
    }

    private fun formattedValue(value: Double): String {
        val df = DecimalFormat("#.##")
        return df.format(value).toString()
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val tag2VsSensorImage: ImageView = view.tag2_vs_sensor_image
    val tag2VsSensorName: TextView = view.tag2_vs_sensor_name
    val tag2VsSensorDetailedName: TextView = view.tag2_vs_sensor_detailed_name
    val tag2VsEnabled: CheckBox = view.tag2_vs_sensor_enable
    val tag2VsInfoRecord: TextView = view.tag2_vs_thresholds_info_record
    val tag2VsRadioGroupTh1Th2: RadioGroup = view.tag2_vs_thresholds_rg_th1th2
    val tag2VsRadioButtonOutOfRange: RadioButton = view.tag2_vs_thresholds_rb_out
    val tag2VsRadioButtonInRange: RadioButton = view.tag2_vs_thresholds_rb_in
    val tag2VsRadioButtonLess: RadioButton = view.tag2_vs_thresholds_rb_less
    val tag2VsRadioButtonMore: RadioButton = view.tag2_vs_thresholds_rb_more
    val tag2VsTh1TextInputLayout: TextInputLayout = view.tag2_vs_th1_textinputlayout
    val tag2VsTh2TextInputLayout: TextInputLayout = view.tag2_vs_th2_textinputlayout
    val tag2VsTh1: TextInputEditText = view.tag2_vs_th1
    val tag2VsTh2: TextInputEditText = view.tag2_vs_th2
    val tag2VsTh1Enum: Spinner = view.tag2_vs_th1_enum
    val tag2VsTh2Enum: Spinner = view.tag2_vs_th2_enum
    val tag2VsTh1Unit: TextView = view.tag2_vs_th1_unit
    val tag2VsTh2Unit: TextView = view.tag2_vs_th2_unit
    val tag2VsTh1InvalidMessage: TextView = view.tag2_vs_th1_invalid_message
    val tag2VsTh2InvalidMessage: TextView = view.tag2_vs_th2_invalid_message
}