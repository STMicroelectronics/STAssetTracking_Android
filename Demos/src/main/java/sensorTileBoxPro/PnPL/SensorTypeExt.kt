package sensorTileBoxPro.PnPL

import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.SensorType
import com.st.demos.R

val SensorType.imageResource:Int
    get() = when(this){
        SensorType.Accelerometer -> R.drawable.sensor_type_accelerometer
        SensorType.Magnetometer -> R.drawable.sensor_type_compass
        SensorType.Gyroscope -> R.drawable.sensor_type_gyroscope
        SensorType.Temperature -> R.drawable.sensor_type_temperature
        SensorType.Humidity -> R.drawable.sensor_type_humidity
        SensorType.Pressure -> R.drawable.sensor_type_pressure
        SensorType.Microphone -> R.drawable.sensor_type_microphone
        SensorType.MLC -> R.drawable.sensor_type_mlc
        SensorType.CLASS -> R.drawable.sensor_type_class
        SensorType.STREDL -> R.drawable.sensor_type_class
        SensorType.Unknown -> R.drawable.sensor_type_unknown
    }

val SensorType.nameResource:Int
    get() = when(this){
        SensorType.Accelerometer -> R.string.subSensor_type_acc
        SensorType.Magnetometer -> R.string.subSensor_type_mag
        SensorType.Gyroscope -> R.string.subSensor_type_gyro
        SensorType.Temperature -> R.string.subSensor_type_temp
        SensorType.Humidity -> R.string.subSensor_type_hum
        SensorType.Pressure -> R.string.subSensor_type_press
        SensorType.Microphone -> R.string.subSensor_type_mic
        SensorType.MLC -> R.string.subSensor_type_mlc
        SensorType.CLASS -> R.string.subSensor_type_class
        SensorType.STREDL -> R.string.subSensor_type_stredl
        SensorType.Unknown -> R.string.subSensor_type_unknown
    }

val SensorType.previewNameResource:Int
    get() = when(this){
        SensorType.Accelerometer -> R.string.subSensor_previewType_acc
        SensorType.Magnetometer -> R.string.subSensor_previewType_mag
        SensorType.Gyroscope -> R.string.subSensor_previewType_gyro
        SensorType.Temperature -> R.string.subSensor_previewType_temp
        SensorType.Humidity -> R.string.subSensor_previewType_hum
        SensorType.Pressure -> R.string.subSensor_previewType_press
        SensorType.Microphone -> R.string.subSensor_previewType_mic
        SensorType.MLC -> R.string.subSensor_previewType_mlc
        SensorType.CLASS -> R.string.subSensor_previewType_class
        SensorType.STREDL -> R.string.subSensor_previewType_stredl
        SensorType.Unknown -> R.string.subSensor_previewType_unknown
    }