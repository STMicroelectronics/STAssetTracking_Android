package com.st.assetTracking.atrBle1.communication

import com.st.assetTracking.atrBle1.sensorTileBox.communication.toDataSample
import com.st.assetTracking.data.AccelerationEvent
import com.st.assetTracking.data.EventDataSample
import com.st.assetTracking.data.Orientation
import com.st.assetTracking.data.SensorDataSample
import junit.framework.Assert.assertNotNull
import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class SensorSampleParserKtTest {

    @Test
    fun emptyStringReturnNull() {
        assertNull("".toDataSample())
    }

    @Test
    fun invalidStringReturnNull() {
        assertNull("invalid string".toDataSample())
        assertNull("invalid string\r\nwith multiple lines".toDataSample())
    }

    @Test
    fun onlyVersion1IsParsed() {
        val logV1 = """
            Version,1
            Data
            Date,Time
        """.trimIndent()
        assertNotNull(logV1.toDataSample())

        val logV2 = """
            Version,2
            Data
            Date,Time
        """.trimIndent()
        assertNull(logV2.toDataSample())
    }

    @Test
    fun thirdLineMustContainsDateAndTimeField() {
        val log = """
            Version,1
            Data
            Date,Time
        """.trimIndent()
        assertNotNull(log.toDataSample())
    }

    @Test
    fun dateAndTimeFieldIsCorrectlyParsed() {
        val dateStr = "01/02/34,05:06:07.890"
        val dateObj = SimpleDateFormat("dd/MM/yy,HH:mm:ss.SSS", Locale.US).parse(dateStr)
        val log = """
            Version,1
            Data
            Date,Time
            $dateStr
        """.trimIndent()
        val samples = log.toDataSample()
        assertNotNull(samples)
        assertTrue(samples!!.isNotEmpty())
        assertEquals(dateObj, (samples[0] as SensorDataSample).date)
    }

    @Test
    fun TemperatureFieldIsCorrectlyParsed() {
        val dateStr = "01/02/34,05:06:07.890"
        val temperatureValue = 123.4f
        val log = """
            Version,1
            Data
            Date,Time,Temperature
            $dateStr,$temperatureValue
        """.trimIndent()
        val samples = log.toDataSample()
        assertNotNull(samples)
        assertTrue(samples!!.isNotEmpty())
        assertEquals(temperatureValue, (samples[0] as SensorDataSample).temperature)
    }

    @Test
    fun pressureFieldIsCorrectlyParsed() {
        val dateStr = "01/02/34,05:06:07.890"
        val humidityValue = 23.4f
        val log = """
            Version,1
            Data
            Date,Time,Humidity
            $dateStr,$humidityValue
        """.trimIndent()
        val samples = log.toDataSample()
        assertNotNull(samples)
        assertTrue(samples!!.isNotEmpty())
        assertEquals(humidityValue, (samples[0] as SensorDataSample).humidity)
    }

    @Test
    fun humidityFieldIsCorrectlyParsed() {
        val dateStr = "01/02/34,05:06:07.890"
        val pressureValue = 987.6f
        val log = """
            Version,1
            Data
            Date,Time,Pressure
            $dateStr,$pressureValue
        """.trimIndent()
        val samples = log.toDataSample()
        assertNotNull(samples)
        assertTrue(samples!!.isNotEmpty())
        assertEquals(pressureValue, (samples[0] as SensorDataSample).pressure)
    }

    @Test
    fun fullEnvironmentalLineIsParsed() {
        val dateStr = "01/02/34,05:06:07.890"
        val pressureValue = 987.6f
        val temperatureValue = 123.4f
        val humidityValue = 23.4f
        val log = """
            Version,1
            Data
            Date,Time,Humidity,Pressure,Temperature
            $dateStr,$humidityValue,$pressureValue,$temperatureValue
        """.trimIndent()
        val samples = log.toDataSample()
        assertNotNull(samples)
        assertTrue(samples!!.isNotEmpty())
        assertEquals(pressureValue, (samples[0] as SensorDataSample).pressure)
        assertEquals(humidityValue, (samples[0] as SensorDataSample).humidity)
        assertEquals(temperatureValue, (samples[0] as SensorDataSample).temperature)
    }

    @Test
    fun emptyLineAndSpaceAreSkippedDuringParsingParsed() {
        val dateStr = "01/02/34,05:06:07.890"
        val pressureValue = 987.6f
        val temperatureValue = 123.4f
        val humidityValue = 23.4f
        val log = """
            Version,1
                
            Data
            
               
            Date,Time,Humidity,Pressure,Temperature
                
                  
            $dateStr,$humidityValue,$pressureValue,$temperatureValue
        """
        val samples = log.toDataSample()
        assertNotNull(samples)
        assertTrue(samples!!.isNotEmpty())
        assertEquals(pressureValue, (samples[0] as SensorDataSample).pressure)
        assertEquals(humidityValue, (samples[0] as SensorDataSample).humidity)
        assertEquals(temperatureValue, (samples[0] as SensorDataSample).temperature)
    }

    private fun buildAccelerationEventLog(orientation: String): String {
        return """
            Version,1
            Data
            Date,Time,HwEvent
            01/02/34,05:06:07.890,$orientation
        """.trimIndent()
    }

    @Test
    fun orientationEventIsParsed() {
        val log = buildAccelerationEventLog("TL")
        val samples = log.toDataSample()
        assertNotNull(samples)
        assertTrue(samples!!.isNotEmpty())
        assertEquals(Orientation.UP_LEFT, (samples[0] as EventDataSample).currentOrientation)
        assertTrue((samples[0] as EventDataSample).events.contains(AccelerationEvent.ORIENTATION))
    }

    @Test
    fun allTheOrientationEventIsParsed() {
        var samples = buildAccelerationEventLog("TL").toDataSample()
        assertEquals(Orientation.UP_LEFT, (samples!![0] as EventDataSample).currentOrientation)
        assertEquals(AccelerationEvent.ORIENTATION, (samples[0] as EventDataSample).events[0])

        samples = buildAccelerationEventLog("TR").toDataSample()
        assertEquals(Orientation.UP_RIGHT, (samples!![0] as EventDataSample).currentOrientation)
        assertEquals(AccelerationEvent.ORIENTATION, (samples[0] as EventDataSample).events[0])

        samples = buildAccelerationEventLog("BR").toDataSample()
        assertEquals(Orientation.DOWN_RIGHT, (samples!![0] as EventDataSample).currentOrientation)
        assertEquals(AccelerationEvent.ORIENTATION, (samples[0] as EventDataSample).events[0])

        samples = buildAccelerationEventLog("BL").toDataSample()
        assertEquals(Orientation.DOWN_LEFT, (samples!![0] as EventDataSample).currentOrientation)
        assertEquals(AccelerationEvent.ORIENTATION, (samples[0] as EventDataSample).events[0])

        samples = buildAccelerationEventLog("U").toDataSample()
        assertEquals(Orientation.TOP, (samples!![0] as EventDataSample).currentOrientation)
        assertEquals(AccelerationEvent.ORIENTATION, (samples[0] as EventDataSample).events[0])

        samples = buildAccelerationEventLog("D").toDataSample()
        assertEquals(Orientation.BOTTOM, (samples!![0] as EventDataSample).currentOrientation)
        assertEquals(AccelerationEvent.ORIENTATION, (samples[0] as EventDataSample).events[0])
    }

    @Test
    fun allTheEventIsParsed() {
        var samples = buildAccelerationEventLog("T").toDataSample()
        assertEquals(AccelerationEvent.TILT, (samples!![0] as EventDataSample).events[0])
        assertEquals(Orientation.UNKNOWN, (samples[0] as EventDataSample).currentOrientation)

        samples = buildAccelerationEventLog("WU").toDataSample()
        assertEquals(AccelerationEvent.WAKE_UP, (samples!![0] as EventDataSample).events[0])
        assertEquals(Orientation.UNKNOWN, (samples[0] as EventDataSample).currentOrientation)
    }

    @Test
    fun sensorLineAndEventLineCanBeInTheSameFile() {
        val pressureValue = 987.6f
        val temperatureValue = 123.4f
        val humidityValue = 23.4f
        val log = """
            Version,1
            Data
            Date,Time,Humidity,Pressure,Temperature,HwEvent
            01/02/34,05:06:07.890,$humidityValue,$pressureValue,$temperatureValue,
            01/02/34,05:06:07.999,,,,WU
        """.trimIndent()
        val samples = log.toDataSample()
        assertNotNull(samples)
        assertEquals(2, samples!!.size)
        assertEquals(pressureValue, (samples[0] as SensorDataSample).pressure)
        assertEquals(humidityValue, (samples[0] as SensorDataSample).humidity)
        assertEquals(temperatureValue, (samples[0] as SensorDataSample).temperature)
        assertEquals(AccelerationEvent.WAKE_UP, (samples[1] as EventDataSample).events[0])
    }

    @Test
    fun lineWithStrangeCharAreRemoved() {
        val log = """
    Version, 1
    Data
    Date [DD/MM/YY],Time [HH:MM:SS.mmm],Pressure [mB],Temperature ['C],Humidity [%],HwEvent [Type]
    12/12/12, 12:32:30.653,996.17,17.49,40.5,
    26/03/20, 16:30:43.320,996.18,17.49,40.5,
    ����
        """.trimIndent()
        val samples = log.toDataSample()
        assertNotNull(samples)
        assertEquals(2, samples!!.size)
    }


}