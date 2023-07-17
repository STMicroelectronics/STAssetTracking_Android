package com.st.assetTracking.data.ui.genericDataUI

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
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.st.assetTracking.data.GenericDataSample
import com.st.assetTracking.data.ui.*
import com.st.smartaglibrary.v2.catalog.NfcV2Firmware
import com.st.smartaglibrary.v2.catalog.VirtualSensor
import com.st.ui.R
import com.st.ui.SensorDataPlotView
import org.joda.time.DateTime
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class GenericDataPlotAdapter(private val catalog: NfcV2Firmware, private val vsIds: List<Int>, val items : List<GenericDataSample>, val context: Context, private val timing: Int) : RecyclerView.Adapter<GenericDataPlotViewHolder>() {

    private data class ChartExtreme(val min: Float, val max: Float)
    private var dates = ArrayList<Date>()
    private var fistSampleTime: Long = 0

    override fun getItemCount(): Int {
        return vsIds.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericDataPlotViewHolder {
        return GenericDataPlotViewHolder(LayoutInflater.from(context).inflate(R.layout.item_generic_samples_plot, parent, false))
    }

    override fun onBindViewHolder(holder: GenericDataPlotViewHolder, position: Int) {
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

                chart.xAxis.valueFormatter = GeneralXAxisFormatter(fistSampleTime , timing)

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

        /* hide chart description */
        chart.description.isEnabled = false

        /* isEnable touch gestures */
        chart.setTouchEnabled(true)

        /* isEnable scaling and dragging */
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        /* if disabled, scaling can be done on x- and y-axis separately */
        chart.setPinchZoom(true)

        /* Set max zoom */
        chart.viewPortHandler.setMaximumScaleY(5f)
        chart.viewPortHandler.setMaximumScaleX(5f)

        /* add empty point set */
        chart.data = LineData()

        /* noLegend */
        chart.legend.isEnabled = false

        val xl = chart.xAxis
        xl.textColor = ContextCompat.getColor(context, com.st.assetTracking.data.ui.R.color.labelPlotContrast)
        chart.axisLeft.textColor = ContextCompat.getColor(context, R.color.labelPlotContrast)
        xl.position = XAxis.XAxisPosition.BOTTOM
        //xl.setDrawLabels(true)
        xl.setDrawGridLines(true)
        //xl.setAvoidFirstLastClipping(true)

        val mv = CustomMarkerView(context, com.st.assetTracking.data.ui.R.layout.custom_marker_view, 0)
        // set the marker to the chart
        chart.marker = mv

        /** Set Extreme values on xAxis */
        if(timing == 7) {
            xl.labelCount = 7

            var sevenDayAgo = DateTime.now().minusDays(7).millis
            fistSampleTime = sevenDayAgo
            sevenDayAgo -= fistSampleTime
            val today = DateTime.now().millis - fistSampleTime

            xl.axisMinimum = sevenDayAgo.toFloat()
            xl.axisMaximum = today.toFloat()

        }else if(timing == 2){
            xl.labelCount = 2

            var twoDayAgo = DateTime.now().minusDays(2).millis
            fistSampleTime = twoDayAgo
            twoDayAgo -= fistSampleTime
            val today = DateTime.now().millis - fistSampleTime

            xl.axisMinimum = twoDayAgo.toFloat()
            xl.axisMaximum = today.toFloat()

        }else if(timing == 1){
            xl.labelCount = 2

            var oneDayAgo = DateTime.now().minusHours(12).millis
            fistSampleTime = oneDayAgo
            oneDayAgo -= fistSampleTime
            val today = DateTime.now().millis - fistSampleTime

            xl.axisMinimum = oneDayAgo.toFloat()
            xl.axisMaximum = today.toFloat()

        }else if(timing == 6){
            xl.labelCount = 4

            var sixHoursAgo = DateTime.now().minusHours(6).millis
            fistSampleTime = sixHoursAgo
            sixHoursAgo -= fistSampleTime
            val today = DateTime.now().millis - fistSampleTime

            xl.axisMinimum = sixHoursAgo.toFloat()
            xl.axisMaximum = today.toFloat()

        }else if(timing == 3){
            xl.labelCount = 4

            var threeHoursAgo = DateTime.now().minusHours(3).millis
            fistSampleTime = threeHoursAgo
            threeHoursAgo -= fistSampleTime
            val today = DateTime.now().millis - fistSampleTime

            xl.axisMinimum = threeHoursAgo.toFloat()
            xl.axisMaximum = today.toFloat()
        }

        chart.axisRight.isEnabled = false

        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)

        /*if (extreme == null)
            chart.isAutoScaleMinMaxEnabled = true
        else {
            chart.axisLeft.apply {
                axisMinimum = extreme.min
                axisMaximum = extreme.max
            }
        }*/
    }

    private fun addChartData(chart: LineChart, dataToPlot: List<GenericDataSample>) {
        dataToPlot.forEach{
            appendSample(chart, it)
        }
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun appendSample(chart: LineChart, sensorSample: GenericDataSample) {
        val x = sensorSample.date
        val y = sensorSample.value
        if(x != null && y != null) {
            dates.add(sensorSample.date!!)
            appendValue(chart, x.time, y.toFloat())
        }
    }

    private fun appendValue(chart: LineChart, x: Long, y: Float) {
        if (chart.data.dataSetCount == 0) {

            chart.data.addDataSet(createSet())
            //fistSampleTime = x
            //minDate = x
        }

        val dataSet = chart.data.getDataSetByIndex(0)

        val entry = Entry((x - fistSampleTime).toFloat(), y)

        dataSet.addEntry(entry)
        chart.syncPlotGui()
        chart.moveViewToX(entry.x)
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

class GeneralXAxisFormatter(private val firstSampleTime: Long, private val timing: Int) : ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val formattedData : String

        val data = Date((firstSampleTime + value.toLong()))

        formattedData = when (timing) {
            7 -> dayMonthFormatter(data)
            2 -> dayMonthFormatter(data)
            1 -> hourMinuteFormatter(data)
            5 -> hourMinuteFormatter(data)
            3 -> hourMinuteFormatter(data)
            else -> hourMinuteFormatter(data)
        }
        return formattedData
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @SuppressLint("SimpleDateFormat")
    fun dayMonthFormatter(dataToFormat: Date) : String{
        var formattedData : String

        try {
            val formatter = SimpleDateFormat("dd/MM", Locale.getDefault())
            formattedData = formatter.format(dataToFormat)
        }catch (e: ParseException) {
            formattedData = dataToFormat.toString()
            e.printStackTrace()
        }

        return formattedData
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @SuppressLint("SimpleDateFormat")
    fun hourMinuteFormatter(dataToFormat: Date) : String{
        var formattedData : String

        try {
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            formattedData = formatter.format(dataToFormat)
        }catch (e: ParseException) {
            formattedData = dataToFormat.toString()
            e.printStackTrace()
        }

        return formattedData
    }
}

class GenericDataPlotViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val tag2SamplesCardView: CardView = view.findViewById(R.id.generic_samples_plot_cv)
    val tag2SamplesPlotZoomBtn: ImageButton = view.findViewById(R.id.generic_samples_plot_zoom_button)
    val tag2SamplesChart: SensorDataPlotView = view.findViewById(R.id.generic_samples_plot_chart)
}