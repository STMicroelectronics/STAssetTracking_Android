package com.st.assetTracking.atrBle1.sensorTileBox.communication

import android.os.Build
import com.st.BlueSTSDK.Debug
import com.st.assetTracking.atrBle1.sensorTileBox.communication.CommandResponse.Status
import com.st.assetTracking.data.DataSample
import java.text.SimpleDateFormat
import java.util.*

internal class AtrBle1ConsoleDebug(private val console: Debug) : AtrBle1Console {

    override suspend fun getLogStatus(): LogStatus {
        val cmdResult = console.sendCommand(LOG_STATUS_COMMAND, WAIT_ONE_LINE_ONE_SECONDS)
                ?: return LogStatus.Unknown
        val parseResult = CommandResponse(cmdResult)
        if (parseResult.status == Status.Done) {
            return parseResult.payload.toLogStatus()
        } else {
            return LogStatus.Unknown
        }
    }

    override suspend fun startLog(): Boolean {
        if (!setDateAndTime(Date()))
            return false
        val cmdResult = console.sendCommand(START_LOG_COMMAND, WAIT_DUMP_DATA_LOG_RESPONSE)
                ?: return false
        val parseResult = CommandResponse(cmdResult)
        return parseResult.isNotError
    }

    override suspend fun pauseLog(): Boolean {
        val cmdResult = console.sendCommand(PAUSE_LOG_COMMAND, WAIT_DUMP_DATA_LOG_RESPONSE)
                ?: return false
        val parseResult = CommandResponse(cmdResult)
        return parseResult.isNotError
    }

    override suspend fun resumeLog(): Boolean {
        val cmdResult = console.sendCommand(RESUME_LOG_COMMAND, WAIT_DUMP_DATA_LOG_RESPONSE)
                ?: return false
        val parseResult = CommandResponse(cmdResult)
        return parseResult.isNotError
    }

    override suspend fun stopLog(): Boolean {
        val cmdResult = console.sendCommand(STOP_LOG_COMMAND, WAIT_DUMP_DATA_LOG_RESPONSE)
                ?: return false
        val parseResult = CommandResponse(cmdResult)
        return parseResult.isNotError

    }

    private suspend fun setDate(date: Date): Boolean {
        var dateStr = DATE_FORMATTER.format(date)
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.N){
            val newDateStr = "0" + Calendar.getInstance().get(Calendar.DAY_OF_WEEK).toString() + "/" + dateStr
            dateStr =  newDateStr
        }
        val commandStr = String.format(SET_DATE_COMMAND_FORMAT, dateStr)
        val response = console.sendCommand(commandStr, WAIT_ONE_LINE_ONE_SECONDS)
        return response?.toLowerCase(Locale.ROOT) == SET_DATE_ANSWER
    }

    private suspend fun setTime(date: Date): Boolean {
        val dateStr = TIME_FORMATTER.format(date)
        val commandStr = String.format(SET_TIME_COMMAND_FORMAT, dateStr)
        val response = console.sendCommand(commandStr, WAIT_ONE_LINE_ONE_SECONDS)
        return response?.toLowerCase(Locale.ROOT) == SET_TIME_ANSWER
    }

    private suspend fun setDateAndTime(date: Date): Boolean {
        return setDate(date) && setTime(date)
    }

    override suspend fun getLogSize(): Int? {
        val response = console.sendCommand(LOG_SIZE_COMMAND, WAIT_DUMP_DATA_LOG_RESPONSE)
                ?: return null
        val parseResponse = CommandResponse(response)
        if (parseResponse.isNotError) {
            return parseResponse.payload.toIntOrNull()
        } else {
            return null
        }
    }

    override suspend fun readLogData(onProgressUpdate: ((Float) -> Unit)?): List<DataSample>? {
        val logStatus = getLogStatus()
        if (logStatus == LogStatus.Started)
            pauseLog()
        val data = getLogSize() ?: return null

        /**
         * function used understand when we download all the data
         * @param receivedData buffer with the received data
         * @return true when we receive $data bytes
         */
        val isComplete = { receivedData: StringBuffer ->
            //remove the  "Done - xxxx" line that is not counted as log data
            val firstLine = receivedData.indexOfFirst { it == '\n' } + 1
            val loadData = (receivedData.length - firstLine)
            onProgressUpdate?.invoke((loadData * 100.0f) / data)
            loadData == data
        }
        val logResponse = console.sendCommand(
            LOG_READ_COMMAND,
                ResponseWaitingConfig(timeout = 2000, isComplete = isComplete)
        ) ?: return null
        //remove the fist line ("Done - XXX")
        val firstLineIndex = logResponse.indexOfFirst { it == '\n' }
        val responseData = logResponse.removeRange(0, firstLineIndex)

        if (logStatus == LogStatus.Started)
            resumeLog()

        return responseData.toDataSample()
    }

    suspend fun getUID(): String {
        return console.sendCommand(UID_COMMAND, WAIT_ONE_LINE_ONE_SECONDS)!!.substringBefore("_")
    }

    suspend fun getInfo():String{
        return console.sendCommand(INFO_COMMAND, WAIT_ONE_LINE_ONE_SECONDS)!!.substringBefore(":")
    }

    companion object {
        private const val LOG_STATUS_COMMAND = "statusLog\n"

        private const val SET_DATE_COMMAND_FORMAT = "setDate %s\n"

        //private val DATE_FORMATTER = SimpleDateFormat("uu/dd/MM/yy", Locale.UK)
        private lateinit var DATE_FORMATTER : SimpleDateFormat
        private const val SET_DATE_ANSWER = "date format correct\n"

        private const val SET_TIME_COMMAND_FORMAT = "setTime %s\n"
        private val TIME_FORMATTER = SimpleDateFormat("HH:mm:ss", Locale.UK)
        private const val SET_TIME_ANSWER = "time format correct\n"

        private const val START_LOG_COMMAND = "startLog\n"
        private const val STOP_LOG_COMMAND = "stopLog\n"

        private const val PAUSE_LOG_COMMAND = "pauseLog\n"
        private const val RESUME_LOG_COMMAND = "resumeLog\n"

        private const val LOG_SIZE_COMMAND = "sizeLog\n"
        private const val LOG_READ_COMMAND = "readLog\n"

        private const val UID_COMMAND = "uid\n"

        private const val INFO_COMMAND = "info\n"

        private fun waitOneLine(data: StringBuffer) = data.endsWith("\n")
        private val WAIT_ONE_LINE_ONE_SECONDS = ResponseWaitingConfig(timeout = 1000L, isComplete = Companion::waitOneLine)
        private val WAIT_DUMP_DATA_LOG_RESPONSE = ResponseWaitingConfig(timeout = 6000L, isComplete = Companion::waitOneLine)

    }

    init {
        DATE_FORMATTER = if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.N)
            SimpleDateFormat("dd/MM/yy", Locale.UK)
        else
            SimpleDateFormat("uu/dd/MM/yy", Locale.UK)
    }

}

private fun String.toLogStatus(): LogStatus {
    return when (this) {
        "DataLog stopped" -> LogStatus.Stopped
        "Run" -> LogStatus.Started
        "Pause" -> LogStatus.Paused
        else -> LogStatus.Unknown
    }
}
