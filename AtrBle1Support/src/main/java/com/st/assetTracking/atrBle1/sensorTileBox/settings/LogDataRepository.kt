package com.st.assetTracking.atrBle1.sensorTileBox.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.st.BlueSTSDK.Node
import com.st.assetTracking.atrBle1.sensorTileBox.communication.AtrBle1ConsoleDebug
import com.st.assetTracking.atrBle1.sensorTileBox.communication.waitStatus
import com.st.assetTracking.data.DataSample

internal class LogDataRepository(private val node: Node) {

    sealed class LoadingProgress {
        /**
         * initial state
         */
        object Unknown : LoadingProgress()

        /**
         * wait the board to dump the last acquired data into the SD
         */
        object DumpingData : LoadingProgress()
        /**
         * downloading the data from the SD Card
         */
        data class Ongoing(val progress: Float) : LoadingProgress()

        /**
         * loading completed
         * @param data read from the board
         */
        data class Completed(val data: List<DataSample>) : LoadingProgress()

        /**
         * error parsing the received data
         */
        object LoadingFailed : LoadingProgress()
    }

    private val mSample = MutableLiveData<LoadingProgress>(LoadingProgress.Unknown)
    val dataSample: LiveData<LoadingProgress>
        get() = mSample

    suspend fun loadData() {
        node.waitStatus(Node.State.Connected)
        val loggingConsole = AtrBle1ConsoleDebug(node.debug!!)
        mSample.postValue(LoadingProgress.DumpingData)
        val samples = loggingConsole.readLogData(onProgressUpdate = {
            mSample.postValue(LoadingProgress.Ongoing(it))
        })
        if (samples == null) {
            mSample.postValue(LoadingProgress.LoadingFailed)
        } else {
            mSample.postValue(LoadingProgress.Completed(samples))
        }
    }

    suspend fun getUID(): String{
        node.waitStatus(Node.State.Connected)
        val loggingConsole = AtrBle1ConsoleDebug(node.debug!!)
        return loggingConsole.getUID()
    }

}