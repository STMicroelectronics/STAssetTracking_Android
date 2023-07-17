package com.st.assetTracking.atrBle1.sensorTileBox.communication

import com.st.assetTracking.data.DataSample

enum class LogStatus {
    Stopped,
    Started,
    Paused,
    Unknown
}

internal interface AtrBle1Console {

    /**
     * tell if the board is logging the data or not
     */
    suspend fun getLogStatus(): LogStatus

    /**
     * start acquiring the data
     */
    suspend fun startLog(): Boolean

    /**
     * pause the acquisition and write and dump the log into the SD
     */
    suspend fun pauseLog(): Boolean
    /**
     * restart a paused log
     */
    suspend fun resumeLog(): Boolean

    /**
     * stop a log
     */
    suspend fun stopLog(): Boolean
    /**
     * get the number of byte into the last acquisition
     */
    suspend fun getLogSize(): Int?

    /**
     * read the data in the last acquisition
     * @param function called with the % of data read
     * @return data read or null if an error happen
     */
    suspend fun readLogData(onProgressUpdate: ((Float) -> Unit)? = null): List<DataSample>?
}