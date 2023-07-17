package com.st.assetTracking.atrBle1.sensorTileBox.communication

import com.st.assetTracking.data.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * take a csv line and extract the date
 */
private typealias DateBuilder = (List<String>) -> Date?
/**
 * take a csv line and extract a float
 */
private typealias FloatBuilder = (List<String>) -> Float?
/**
 * take a csv line and extract the hardware event data
 */
private typealias AccelerationEventBuilder = (List<String>) -> Pair<AccelerationEvent, Orientation>?

internal fun String.toDataSample(): List<DataSample>? {
    //remove strange char as \r \n ecc..
    val isNotVisibleChar = Regex("[^\\p{Graph}]")
    val lines = lines().map {
        it.trim() // remove staring and ending space
        //remove internal space
        it.replace(isNotVisibleChar, "")
    }.filter {
        //remove empty lines
        it.isNotEmpty()
    }
    if (lines.isNotEmpty() && !isSupportedVersion(lines[0])) {
        return null
    }
    //Line[1] = Data
    if (lines.size < 3)
        return null
    val dataHeader = lines[2].comaSeparated
    // build a function that will extract the date from a data line
    val dateBuilder = buildDataBuilder(dataHeader) ?: return null
    // build a function that will extract the temperature from a data line
    val temperatureBuilder = floatDataBuilder(dataHeader, "Temperature")
    // build a function that will extract the humidity from a data line
    val humidityBuilder = floatDataBuilder(dataHeader, "Humidity")
    // build a function that will extract the pressure from a data line
    val pressureBuilder = floatDataBuilder(dataHeader, "Pressure")
    // build a function that will extract the acc event from a data line
    val accEventBuilder = buildAccelerationEventBuilder(dataHeader)
    //remove the header lines and covert the other lines to DataSample
    return lines.subList(3, lines.size).map {
        val dataLine = it.comaSeparated

        val date = dateBuilder(dataLine)!!
        val accEvent = accEventBuilder?.invoke(dataLine)
        if (accEvent != null) {
            val (event, orientation) = accEvent
            EventDataSample(
                    date = date,
                    acceleration = null,
                    events = arrayOf(event),
                    currentOrientation = orientation
            )
        } else {
            SensorDataSample(date,
                    temperature = temperatureBuilder?.invoke(dataLine),
                    pressure = pressureBuilder?.invoke(dataLine),
                    humidity = humidityBuilder?.invoke(dataLine),
                    acceleration = null,
                    gyroscope = null
            )
        }
    }
}

/**
 * this variable is allocate only the fist time it is used
 */
private val strToEventMap by lazy {
    mapOf(
            "TL" to Pair(AccelerationEvent.ORIENTATION, Orientation.UP_LEFT),
            "TR" to Pair(AccelerationEvent.ORIENTATION, Orientation.UP_RIGHT),
            "BR" to Pair(AccelerationEvent.ORIENTATION, Orientation.DOWN_RIGHT),
            "BL" to Pair(AccelerationEvent.ORIENTATION, Orientation.DOWN_LEFT),
            "U" to Pair(AccelerationEvent.ORIENTATION, Orientation.TOP),
            "D" to Pair(AccelerationEvent.ORIENTATION, Orientation.BOTTOM),
            "T" to Pair(AccelerationEvent.ACCELERATION_TILT_35, Orientation.UNKNOWN),
            "WU" to Pair(AccelerationEvent.ACCELERATION_WAKE_UP, Orientation.UNKNOWN)
    )
}

private fun buildAccelerationEventBuilder(dataHeader: List<String>): AccelerationEventBuilder? {
    //find the column that contains the string "HwEvent"
    val eventIndex = dataHeader.indexOfFirst { it.contains("HwEvent") }
    //if the column doesn't exist
    if (eventIndex == -1)
        return null

    return { dataLine ->
        //get the string in the colum under the "HwEvent" string and convert it to events
        strToEventMap[dataLine[eventIndex]]
    }
}


private val DATA_PARSER = SimpleDateFormat("dd/MM/yy HH:mm:ss.SSS", Locale.US)
private fun buildDataBuilder(dataHeader: List<String>): DateBuilder? {
    val dateIndex = dataHeader.indexOfFirst { it.contains("Date") }
    val hourIndex = dataHeader.indexOfFirst { it.contains("Time") }
    //if one column is missing return null
    if (dateIndex == -1 || hourIndex == -1)
        return null

    return { dataLine ->
        val dataStr = "${dataLine[dateIndex]} ${dataLine[hourIndex]}"
        DATA_PARSER.parse(dataStr)
    }
}

private fun floatDataBuilder(dataHeader: List<String>, dataName: String): FloatBuilder? {
    //find the column that contains the string "$dataName"
    val dataIndex = dataHeader.indexOfFirst { it.contains(dataName) }

    //if the column doesn't exist
    if (dataIndex == -1)
        return null

    return { dataLine ->
        //get the string in the column under the "$dataName" string and convert it to float
        dataLine.getOrNull(dataIndex)?.toFloatOrNull()
    }
}

private fun isSupportedVersion(lineVersion: String): Boolean {
    val version = lineVersion.comaSeparated.getOrNull(1)?.toIntOrNull()
    return version == 1
}

private val String.comaSeparated: List<String>
    get() = this.split(',')