/*
 * Copyright (c) 2020  STMicroelectronics – All rights reserved
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

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.st.assetTracking.data.SensorDataSample
import com.st.assetTracking.data.ui.SensorDataDetailsFragment.Companion.Sample
import com.st.ui.SensorDataPlotView
import org.joda.time.DateTime
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SensorDataPlotFragment : androidx.fragment.app.Fragment() {

    private lateinit var pressureView: View
    private lateinit var pressureChart: LineChart

    private lateinit var temperatureView: View
    private lateinit var temperatureChart: LineChart

    private lateinit var humidityView: View
    private lateinit var humidityChart: LineChart

    private lateinit var vibrationView: View
    private lateinit var vibrationChart: LineChart

    private lateinit var tvNoData: TextView

    private var fistSampleTime: Long = 0

    private var dates = ArrayList<Date>()
    private var charts = ArrayList<LineChart>()

    private lateinit var timingEditor: SharedPreferences.Editor
    private lateinit var timingReader: SharedPreferences
    private var timing: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_plot_data, container, false)

        if(localPlot) {
            timing = "0d"
        }else{
            timingReader = requireContext().getSharedPreferences("Timing", Context.MODE_PRIVATE)
            timing = timingReader.getString("time", "")!!
        }

        tvNoData = rootView.findViewById(R.id.tvNoData)

        pressureView = rootView.findViewById(R.id.plot_pressureView)
        pressureChart = rootView.findViewById<SensorDataPlotView>(R.id.plot_pressurePlot).chart
        configureChart(pressureChart, PRESSURE_EXTREME)
        rootView.findViewById<Button>(R.id.plot_pressureDetailsButton).setOnClickListener {
            showDetails(pressureChart.data.getDataSetByIndex(0), getString(R.string.data_pressure_format),
                    getString(R.string.data_pressure_unit)
            )
        }
        rootView.findViewById<ImageButton>(R.id.ibZoomOutPressure).setOnClickListener{
            resetZoom(pressureChart)
        }

        temperatureView = rootView.findViewById(R.id.plot_temperatureView)
        temperatureChart = rootView.findViewById<SensorDataPlotView>(R.id.plot_temperaturePlot).chart
        configureChart(temperatureChart, TEMPERATURE_EXTREME)
        rootView.findViewById<Button>(R.id.plot_temperatureDetailsButton).setOnClickListener {
            showDetails(temperatureChart.data.getDataSetByIndex(0), getString(R.string.data_temperature_format),
                    getString(R.string.data_temperature_unit))
        }
        rootView.findViewById<ImageButton>(R.id.ibZoomOutTemperature).setOnClickListener{
            resetZoom(temperatureChart)
        }

        humidityView = rootView.findViewById(R.id.plot_humidityView)
        humidityChart = rootView.findViewById<SensorDataPlotView>(R.id.plot_humidityPlot).chart
        configureChart(humidityChart, HUMIDITY_EXTREME)
        rootView.findViewById<Button>(R.id.plot_humidityDetailsButton).setOnClickListener {
            showDetails(humidityChart.data.getDataSetByIndex(0), getString(R.string.data_humidity_format),
                    getString(R.string.data_humidity_unit))
        }
        rootView.findViewById<ImageButton>(R.id.ibZoomOutHumidity).setOnClickListener{
            resetZoom(humidityChart)
        }

        vibrationView = rootView.findViewById(R.id.plot_vibrationView)
        vibrationChart = rootView.findViewById<SensorDataPlotView>(R.id.plot_vibrationPlot).chart
        configureChart(vibrationChart, VIBRATION_EXTREME)
        rootView.findViewById<Button>(R.id.plot_vibrationDetailsButton).setOnClickListener {
            showDetails(vibrationChart.data.getDataSetByIndex(0), getString(R.string.data_vibration_format),
                    getString(R.string.data_acceleration_unit))
        }
        rootView.findViewById<ImageButton>(R.id.ibZoomOutVibration).setOnClickListener{
            resetZoom(vibrationChart)
        }

        if(!localPlot){
            timingEditor = requireContext().getSharedPreferences("Timing", Context.MODE_PRIVATE).edit()
            timingEditor.putString("time", "1d")
            timingEditor.apply()
        }

        addChartData()

        return rootView
    }

    private fun resetZoom(chart: LineChart){
        chart.fitScreen()
        chart.invalidate()
    }

    private fun addChartData() {

        val sensorDataList = arguments!!.getParcelableArrayList<SensorDataSample>(SENSOR_DATA_LIST_KEY)!!

        if(sensorDataList.size == 0){
            pressureView.visibility = View.GONE
            temperatureView.visibility = View.GONE
            humidityView.visibility = View.GONE
            vibrationView.visibility = View.GONE
            tvNoData.visibility = View.VISIBLE
        }else{
            tvNoData.visibility = View.GONE
            sensorDataList.forEach {
                appendSample(it)
            }
            charts.forEach {
                it.xAxis.apply {
                    it.xAxis.valueFormatter = MyXAxisFormatter(fistSampleTime, timing)
                }
            }
        }

    }

    private fun configureChart(chart: LineChart, extreme: ChartExtreme?) {

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
        xl.textColor = ContextCompat.getColor(requireContext(), R.color.labelPlotContrast)
        chart.axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.labelPlotContrast)
        xl.position = XAxis.XAxisPosition.BOTTOM
        //xl.setDrawLabels(true)
        xl.setDrawGridLines(true)
        //xl.setAvoidFirstLastClipping(true)

        val mv = CustomMarkerView(requireContext(), R.layout.custom_marker_view, fistSampleTime)
        // set the marker to the chart
        chart.marker = mv

        /**
         * Set Extreme values on xAxis
         */
        if(timing == "7d") {
            xl.labelCount = 7

            var sevenDayAgo = DateTime.now().minusDays(7).millis
            fistSampleTime = sevenDayAgo
            sevenDayAgo -= fistSampleTime
            val today = DateTime.now().millis - fistSampleTime

            xl.axisMinimum = sevenDayAgo.toFloat()
            xl.axisMaximum = today.toFloat()

        }else if(timing == "2d"){
            xl.labelCount = 2

            var twoDayAgo = DateTime.now().minusDays(2).millis
            fistSampleTime = twoDayAgo
            twoDayAgo -= fistSampleTime
            val today = DateTime.now().millis - fistSampleTime

            xl.axisMinimum = twoDayAgo.toFloat()
            xl.axisMaximum = today.toFloat()

        }else if(timing == "1d"){
            xl.labelCount = 2

            var oneDayAgo = DateTime.now().minusHours(12).millis
            fistSampleTime = oneDayAgo
            oneDayAgo -= fistSampleTime
            val today = DateTime.now().millis - fistSampleTime

            xl.axisMinimum = oneDayAgo.toFloat()
            xl.axisMaximum = today.toFloat()

        }else if(timing == "6h"){
            xl.labelCount = 4

            var sixHoursAgo = DateTime.now().minusHours(6).millis
            fistSampleTime = sixHoursAgo
            sixHoursAgo -= fistSampleTime
            val today = DateTime.now().millis - fistSampleTime

            xl.axisMinimum = sixHoursAgo.toFloat()
            xl.axisMaximum = today.toFloat()

        }else if(timing == "3h"){
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

        if (extreme == null)
            chart.isAutoScaleMinMaxEnabled = true
        else {
            chart.axisLeft.apply {
                axisMinimum = extreme.min
                axisMaximum = extreme.max
            }
        }

    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun appendSample(sensorSample: SensorDataSample) {
        if(localPlot){
            val xValueManaged = preprocessingOnBLETimestamp(sensorSample.date)
            val xValueManagedDate = SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault()).parse(xValueManaged)
            val xValueLong = xValueManagedDate.time
            dates.add(xValueManagedDate)
            appendOrHide(pressureView,pressureChart,xValueLong,sensorSample.pressure)
            appendOrHide(humidityView,humidityChart,xValueLong,sensorSample.humidity)
            appendOrHide(vibrationView,vibrationChart,xValueLong,sensorSample.acceleration)
            appendOrHide(temperatureView,temperatureChart,xValueLong,sensorSample.temperature)
        }else{
            val x = sensorSample.date.time
            dates.add(sensorSample.date)
            appendOrHide(pressureView, pressureChart, x, sensorSample.pressure)
            appendOrHide(humidityView, humidityChart, x, sensorSample.humidity)
            appendOrHide(vibrationView, vibrationChart, x, sensorSample.acceleration)
            appendOrHide(temperatureView, temperatureChart, x, sensorSample.temperature)
        }
    }

    private fun appendOrHide(view: View, chart: LineChart, x: Long, y: Float?) {
        if (y != null) {
            appendValue(chart, x, y)
            view.visibility = View.VISIBLE
        }else {
            if (chart.data.dataSetCount == 0) //hide the plot only if it empty
                view.visibility = View.GONE
        }
    }

    private fun appendValue(chart: LineChart, x: Long, y: Float) {
        if (chart.data.dataSetCount == 0) {
            charts.add(chart)
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
        set.color = ContextCompat.getColor(context!!, R.color.colorPrimary)
        set.setCircleColor(ContextCompat.getColor(context!!, R.color.colorCirclePlot))
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

    /**
     * Dialog structure data of data plotted into graph
     */
    private fun createdetailsDataSet(plotData: ILineDataSet): ArrayList<Sample> {
        val dataSet = ArrayList<Sample>(plotData.entryCount)
        for (i in 0 until plotData.entryCount) {
            val plotEntry = plotData.getEntryForIndex(i)
            val date = Date(fistSampleTime + plotEntry.x.toLong())
            dataSet.add(Sample(date, plotEntry.y))
        }
        return ArrayList(dataSet.reversed())
    }

    private fun showDetails(plotData: ILineDataSet?, dataFormat: String, unit: String) {
        if (plotData == null || plotData.entryCount == 0) //no data no details to show
            return
        val dialog = SensorDataDetailsFragment.newInstance(createdetailsDataSet(plotData), dataFormat, unit)
        dialog.show(childFragmentManager, DETAILS_DIALOG_TAG)
    }

    companion object {

        private const val SENSOR_DATA_LIST_KEY = "SENSOR_DATA_LIST_KEY"
        private var localPlot = false

        fun createWith(data: List<SensorDataSample>, plotLocalValues: Boolean?): Fragment {
            localPlot = plotLocalValues != null
            val fragment = SensorDataPlotFragment()
            val arrayList = ArrayList(data)
            fragment.arguments = Bundle().apply {
                putParcelableArrayList(SENSOR_DATA_LIST_KEY, arrayList)
            }
            return fragment
        }

        private val DETAILS_DIALOG_TAG = SensorDataPlotFragment::class.java.simpleName + ".DETAILS_DIALOG_TAG"

        private data class ChartExtreme(val min: Float, val max: Float)

        private val TEMPERATURE_EXTREME = ChartExtreme(min = -5.0f, max = 45.0f)
        private val PRESSURE_EXTREME = ChartExtreme(min = 950f, max = 1150f)
        private val HUMIDITY_EXTREME = ChartExtreme(min = 0f, max = 100f)
        private val VIBRATION_EXTREME = ChartExtreme(min = 600f, max = 63f * 256f)
    }

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

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SuppressLint("SimpleDateFormat")
fun hourMinutedayMonthFormatter(dataToFormat: Date) : String{
    var formattedData : String

    try {
        val formatter = SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
        formattedData = formatter.format(dataToFormat)
    }catch (e: ParseException) {
        formattedData = dataToFormat.toString()
        e.printStackTrace()
    }

    return formattedData
}

class MyXAxisFormatter(private val firstSampleTime: Long, private val timing: String) : ValueFormatter() {

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val formattedData : String

        val data = Date((firstSampleTime + value.toLong()))

        formattedData = when (timing) {
            "7d" -> dayMonthFormatter(data)
            "2d" -> dayMonthFormatter(data)
            "1d" -> hourMinuteFormatter(data)
            "6h" -> hourMinuteFormatter(data)
            "3h" -> hourMinuteFormatter(data)
            else -> hourMinuteFormatter(data)
        }
        return formattedData
    }
}

class CustomMarkerView(context: Context?, layoutResource: Int, private val firstSampleTime: Long) : MarkerView(context, layoutResource) {

    private val uiScreenWidth = resources.displayMetrics.widthPixels;
    private val tvContent: TextView = findViewById<View>(R.id.tvContent) as TextView

    // callbacks everytime the MarkerView is redrawn, can be used to update the content (user-interface)
    @SuppressLint("SetTextI18n")
    override fun refreshContent(e: Entry, highlight: Highlight) {
        val formattedData : String

        val data = Date((firstSampleTime + e.x.toLong()))

        formattedData = hourMinutedayMonthFormatter(data)

        tvContent.text = "" + formattedData + ": " + "[" + e.y + "]"

        // this will perform necessary layouting
        super.refreshContent(e, highlight)
    }

    override fun draw(canvas: Canvas, posx: Float, posy: Float) {
        // Check marker position and update offsets.
        var posX = posx
        val w = width
        if (uiScreenWidth - posX - w < w) {
            posX -= w.toFloat()
        }

        // translate to the correct position and draw
        canvas.translate(posX, posy)
        draw(canvas)
        canvas.translate(-posX, -posy)
    }
}