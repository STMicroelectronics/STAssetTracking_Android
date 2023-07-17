package com.st.assetTracking.atrBle1.sensorTileBox.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.st.BlueSTSDK.Node
import com.st.assetTracking.atrBle1.sensorTileBox.communication.AtrBle1ConsoleDebug
import com.st.assetTracking.atrBle1.sensorTileBox.communication.LogStatus
import com.st.assetTracking.atrBle1.sensorTileBox.communication.waitStatus
import com.st.assetTracking.threshold.comunication.CloudTrackerConsole
import com.st.assetTracking.threshold.comunication.CloudTrackerConsole.LoadCallback
import com.st.assetTracking.threshold.model.SamplingSettings
import com.st.assetTracking.threshold.model.SensorThreshold
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LogSettingsRepository(private val node: Node) {


    private val mThresholds = mutableListOf<SensorThreshold>()
    private val mThresholdsLiveData = MutableLiveData<List<SensorThreshold>>(mThresholds)
    val sensorThresholds: LiveData<List<SensorThreshold>>
        get() = mThresholdsLiveData

    private val mSensorReadInterval = MutableLiveData<Short>(0)
    val sensorReadInterval: LiveData<Short>
        get() = mSensorReadInterval


    private val mLogStatus = MutableLiveData(LogStatus.Unknown)
    val logStatus: LiveData<LogStatus>
        get() = mLogStatus

    private lateinit var settingsConsole: CloudTrackerConsole
    private lateinit var loggingConsole: AtrBle1ConsoleDebug

    suspend fun loadDataAsync() {
        node.waitStatus(Node.State.Connected)
        settingsConsole = CloudTrackerConsole.buildForNode(node)!!
        loggingConsole = AtrBle1ConsoleDebug(node.debug!!)
        val settings = loadDataFrom(settingsConsole)
        mThresholds.clear()
        mThresholds.addAll(settings.threshold)
        mThresholdsLiveData.postValue(mThresholds)
        mSensorReadInterval.postValue(settings.samplingInterval)
        val logStatus = loggingConsole.getLogStatus()
        mLogStatus.postValue(logStatus)
    }

    suspend fun loadSigfoxDataAsync() {
        node.waitStatus(Node.State.Connected)
        settingsConsole = CloudTrackerConsole.buildForNode(node)!!
        loggingConsole = AtrBle1ConsoleDebug(node.debug!!)
        val settings = loadDataFrom(settingsConsole)
        mThresholds.clear()
        mThresholds.addAll(settings.threshold)
        mThresholdsLiveData.postValue(mThresholds)
        mSensorReadInterval.postValue(settings.samplingInterval)
    }

    private suspend fun loadDataFrom(settingsConsole: CloudTrackerConsole): SamplingSettings {
        return suspendCancellableCoroutine { continuation ->
            settingsConsole.load(object : LoadCallback {
                override fun onSuccess(settings: SamplingSettings) {
                    continuation.resume(settings)
                }

                override fun onError() {
                    continuation.resumeWithException(IOException("Error reading the sample settings"))
                }
            })
        }
    }

    fun addThreshold(newTh: SensorThreshold) {
        mThresholds.add(newTh)
        mThresholdsLiveData.postValue(mThresholds)
    }

    fun removeThresholdAt(adapterPosition: Int) {
        mThresholds.removeAt(adapterPosition)
        mThresholdsLiveData.postValue(mThresholds)
    }

    fun clearTreshold(){
        mThresholds.clear()
    }

    suspend fun setSensorSampleValue(sensorSampleValue: Short): Boolean {
        mSensorReadInterval.postValue(sensorSampleValue)
        val settings = SamplingSettings(
                samplingInterval = sensorSampleValue,
                cloudSyncInterval = 0,
                threshold = mThresholds
        )
        return suspendCoroutine { continuation ->
            settingsConsole.save(settings, object : CloudTrackerConsole.SaveCallback {
                override fun onSuccess() {
                    continuation.resume(true)
                }

                override fun onError() {
                    continuation.resume(false)
                }
            })
        }
    }

    suspend fun setSensorSigfoxSampleValue(sensorSampleValue: Short, cloudSyncIntervalValue: Short): Boolean {
        //node.waitStatus(Node.State.Connected)
        //settingsConsole = CloudTrackerConsole.buildForNode(node)!!
        //loggingConsole = AtrBle1ConsoleDebug(node.debug!!)
        mSensorReadInterval.postValue(sensorSampleValue)
        val settings = SamplingSettings(
                samplingInterval = sensorSampleValue,
                cloudSyncInterval = cloudSyncIntervalValue,
                threshold = mThresholds
        )
        return suspendCoroutine { continuation ->
            settingsConsole.save(settings, object : CloudTrackerConsole.SaveCallback {
                override fun onSuccess() {
                    continuation.resume(true)
                    node.disconnect()
                }

                override fun onError() {
                    continuation.resume(false)
                }
            })
        }
    }

    suspend fun startLog() {
        if (logStatus.value == LogStatus.Started)
            return
        val startResult = loggingConsole.startLog()
        if (startResult) {
            mLogStatus.postValue(LogStatus.Started)
        }
    }

    suspend fun stopLog() {
        if (logStatus.value == LogStatus.Stopped)
            return
        val stopResult = loggingConsole.stopLog()
        if (stopResult) {
            mLogStatus.postValue(LogStatus.Stopped)
        }
    }

    suspend fun checkATRFirmware() : String {
        node.waitStatus(Node.State.Connected)
        loggingConsole = AtrBle1ConsoleDebug(node.debug!!)
        return loggingConsole.getInfo()
    }

    suspend fun getUID(): String{
        node.waitStatus(Node.State.Connected)
        val loggingConsole = AtrBle1ConsoleDebug(node.debug!!)
        return loggingConsole.getUID()
    }

    suspend fun getLogStatus(): LogStatus {
        val console = AtrBle1ConsoleDebug(node.debug!!)
        return console.getLogStatus()
    }


}