package com.st.nfcSmarTag.v2.samples

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.st.nfcSmarTag.R
import com.st.nfcSmarTag.v1.tagPlotData.CustomMarkerView
import com.st.nfcSmarTag.v1.tagPlotData.MyXAxisFormatter
import com.st.smartaglibrary.v2.catalog.NfcV2Firmware
import com.st.smartaglibrary.v2.catalog.VirtualSensor
import com.st.assetTracking.data.GenericDataSample
import com.st.ui.SensorDataPlotView
import kotlinx.android.synthetic.main.item_tag2_samples_plot.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Tag2SamplesPlotAdapter(private val catalog: NfcV2Firmware, val vsIds: List<Int>, val items : List<GenericDataSample>, val context: Context) : RecyclerView.Adapter<Tag2SamplesPlotViewHolder>() {

    private data class ChartExtreme(val min: Float, val max: Float)
    private var dates = ArrayList<Date>()

    override fun getItemCount(): Int {
        return vsIds.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Tag2SamplesPlotViewHolder {
        return Tag2SamplesPlotViewHolder(LayoutInflater.from(context).inflate(R.layout.item_tag2_samples_plot, parent, false))
    }

    override fun onBindViewHolder(holder: Tag2SamplesPlotViewHolder, position: Int) {
        val currentVSId = vsIds[position]
        val catalogVS = retrieveVirtualSensorFromCatalog(catalog, currentVSId)

        if(catalogVS != null){
            val dataToPlot = items.filter { vsSensorDataSample -> vsSensorDataSample.id == vsIds[position] }

            val chartView = holder.tag2SamplesChart
            val chart = holder.tag2SamplesChart.chart
            chartView.title = catalogVS.displayName
            chartView.xTitle = "Time"
            chartView.yTitle = "${catalogVS.displayName} (${catalogVS.threshold.unit})"

            if(dataToPlot.isNotEmpty()){
                val extremes = ChartExtreme(catalogVS.threshold.min?.toFloat() ?: 0f, catalogVS.threshold.min?.toFloat() ?: 100f)

                holder.tag2SamplesPlotZoomBtn.setOnClickListener {
                    resetZoom(chart)
                }

                configureChart(chart, extremes)
                addChartData(chart, dataToPlot)
            } else {
                holder.tag2SamplesCardView.visibility = View.GONE
            }
        }
    }

    private fun resetZoom(chart: LineChart){
        chart.fitScreen()
        chart.invalidate()
    }

    private fun retrieveVirtualSensorFromCatalog(catalog: NfcV2Firmware, virtualSensorId: Int): VirtualSensor?{
        catalog.virtualSensors.forEach { vSc ->
            if(vSc.id == virtualSensorId){
                return vSc
            }
        }
        return null
    }

    private fun configureChart(chart: LineChart, extreme: ChartExtreme) {

        //hide chart description
        chart.description.isEnabled = false

        // isEnable touch gestures
        chart.setTouchEnabled(true)

        // isEnable scaling and dragging
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true)

        //Set max zoom
        chart.viewPortHandler.setMaximumScaleY(5f)
        chart.viewPortHandler.setMaximumScaleX(5f)

        //add empty point set
        chart.data = LineData()

        //noLegend
        chart.legend.isEnabled = false

        val xl = chart.xAxis
        xl.textColor = ContextCompat.getColor(context, R.color.labelPlotContrast)
        chart.axisLeft.textColor = ContextCompat.getColor(context, R.color.labelPlotContrast)
        xl.position = XAxis.XAxisPosition.BOTTOM
        //xl.setDrawLabels(true)
        xl.setDrawGridLines(true)
        //xl.setAvoidFirstLastClipping(true)

        val mv = CustomMarkerView(context, R.layout.custom_marker_view, 0)
        // set the marker to the chart
        chart.marker = mv

        chart.axisRight.isEnabled = false

        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)

        //if (extreme == null)
            chart.isAutoScaleMinMaxEnabled = true
        /*else {
            chart.axisLeft.apply {
                axisMinimum = extreme.min
                axisMaximum = extreme.max
            }
        }*/

        chart.xAxis.valueFormatter = MyXAxisFormatter(0 , "0d")

    }

    private fun addChartData(chart: LineChart, dataToPlot: List<GenericDataSample>) {
        dataToPlot.forEach{
            appendSample(chart, it)
        }
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun appendSample(chart: LineChart, sensorSample: GenericDataSample) {
        val xValueManaged = preprocessingOnBLETimestamp(sensorSample.date!!)
        val xValueManagedDate = SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault()).parse(xValueManaged)
        val xValueLong = xValueManagedDate.time
        dates.add(xValueManagedDate)
        //appendOrHide(pressureView,pressureChart,xValueLong,sensorSample.value?.toFloat())
        val y = sensorSample.value?.toFloat()
        if(y != null) {
            appendValue(chart, xValueLong, sensorSample.value?.toFloat()!!)
        }
    }

    private fun appendValue(chart: LineChart, x: Long, y: Float) {
        if (chart.data.dataSetCount == 0) {

            chart.data.addDataSet(createSet())
            //fistSampleTime = x
            //minDate = x
        }

        val dataSet = chart.data.getDataSetByIndex(0)

        val entry = Entry((x - 0).toFloat(), y)

        dataSet.addEntry(entry)
        chart.syncPlotGui()
        chart.moveViewToX(entry.x)
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @SuppressLint("SimpleDateFormat")
    fun preprocessingOnBLETimestamp(dataToFormat: Date) : String{
        var formattedData : String

        try {
            val formatter = SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault())
            formattedData = formatter.format(dataToFormat)
        }catch (e: ParseException) {
            formattedData = dataToFormat.toString()
            e.printStackTrace()
        }

        return formattedData
    }

    private fun createSet(): LineDataSet {

        val set = LineDataSet(null, "NotUsed")
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.setDrawCircles(true)
        set.setDrawValues(false)
        set.setDrawHighlightIndicators(false)
        //todo add style atribute
        set.color = ContextCompat.getColor(context, R.color.colorPrimary)
        set.setCircleColor(ContextCompat.getColor(context, R.color.colorCirclePlot))
        return set
    }

    private fun LineChart.clearPlotData() {
        data.dataSets.clear()
        syncPlotGui()
    }

    private fun LineChart.syncPlotGui() {
        data.notifyDataChanged()
        notifyDataSetChanged()
    }
}

class Tag2SamplesPlotViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val tag2SamplesCardView: CardView = view.tag2_samples_plot_cv
    val tag2SamplesPlotZoomBtn: ImageButton = view.tag2_samples_plot_zoom_button
    val tag2SamplesChart: SensorDataPlotView = view.tag2_samples_plot_chart
}