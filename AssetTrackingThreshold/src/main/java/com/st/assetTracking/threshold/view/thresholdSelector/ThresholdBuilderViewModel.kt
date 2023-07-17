package com.st.assetTracking.threshold.view.thresholdSelector

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.st.assetTracking.threshold.model.Orientation
import com.st.assetTracking.threshold.model.SensorThreshold
import com.st.assetTracking.threshold.model.ThresholdComparison
import com.st.assetTracking.threshold.model.ThresholdSensorType

internal class ThresholdBuilderViewModel : ViewModel() {

    sealed class ViewState {
        /**
         * intial state, the use has to select the type of sensor
         */
        object SelectSensorType : ViewState()

        /**
         * show the view to add insert a float value as threshold
         * @param sensor where the threshold will be used, needed to customise the ui and
         * the value checks
         */
        data class SelectThreshold(val sensor: ThresholdSensorType) : ViewState()

        /**
         * the user select the orientation sensor, now it has to select the orientation type
         */
        object SelectOrientation : ViewState()

        /**
         * user select the wake up threshold now it has to insert the g acceleration
         */
        object SelectWakeUpThreshold : ViewState()
        /**
         * all data needed are collected, and the threshold is build
         * */
        data class SensorThresholdBuilt(val threshold: SensorThreshold) : ViewState()

        /**
         * all data needed are collected, and the threshold lesser than and greater than is build
         * */
        data class SensorThresholdLessAndGreaterdBuilt(val threshold1: SensorThreshold, val treshold2: SensorThreshold) : ViewState()
    }

    private val mViewState = MutableLiveData<ViewState>(ViewState.SelectSensorType)
    val viewState: LiveData<ViewState>
        get() = mViewState


    /**
     * change status view based on the selected sensor
     */
    fun selectSensor(sensor: ThresholdSensorType) {
        val nextState = when (sensor) {
            ThresholdSensorType.Temperature,
            ThresholdSensorType.Pressure,
            ThresholdSensorType.Humidity -> {
                ViewState.SelectThreshold(sensor)
            }
            ThresholdSensorType.WakeUp -> {
                ViewState.SelectWakeUpThreshold
            }
            ThresholdSensorType.Tilt -> {
                ViewState.SensorThresholdBuilt(SensorThreshold.tiltThreshold())
            }
            ThresholdSensorType.Orientation -> {
                ViewState.SelectOrientation
            }
        }
        mViewState.postValue(nextState)
    }

    fun selectThreshold(val1: Float, val2: Float) {
        val sensor = (mViewState.value as ViewState.SelectThreshold).sensor
        val th1 = SensorThreshold(sensor, ThresholdComparison.Less, val1)
        val th2 = SensorThreshold(sensor, ThresholdComparison.BiggerOrEqual, val2)
        mViewState.postValue(ViewState.SensorThresholdLessAndGreaterdBuilt(th1, th2))
    }

    fun selectWakeThreshold(thValue: Float) {
        val th = SensorThreshold.wakeUpThreshold(thValue)
        mViewState.postValue(ViewState.SensorThresholdBuilt(th))
    }

    fun selectOrientationThreshold(thValue: Orientation) {
        val th = SensorThreshold.orientationThreshold(thValue)
        mViewState.postValue(ViewState.SensorThresholdBuilt(th))
    }
}
