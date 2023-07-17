package com.st.assetTracking.dashboard.communication.aws

import com.google.gson.*
import com.st.assetTracking.dashboard.model.LocationData
import com.st.assetTracking.data.EventDataSample
import com.st.assetTracking.data.Orientation
import com.st.assetTracking.data.SensorDataSample
import java.lang.reflect.Type
import java.util.*


private const val DATE = "date"
private const val ACCELERATION = "acc"
private const val PRESSURE = "pres"
private const val TEMPERATURE = "temp"
private const val HUMIDITY = "hum"
private const val EVENTS = "evn"
private const val ORIENTATION = "ori"

internal object DateJsonSerializer : JsonSerializer<Date> {
    override fun serialize(src: Date?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val timeStamp = src?.time
        return JsonPrimitive(timeStamp)
    }
}

internal object SensorDataSampleJsonAdapter : JsonSerializer<SensorDataSample> {
    override fun serialize(src: SensorDataSample, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {

        val obj = JsonObject()

        obj.add(DATE, context.serialize(src.date))

        src.acceleration?.let { acc ->
            obj.addProperty(ACCELERATION, acc)
        }

        src.pressure?.let { pres ->
            obj.addProperty(PRESSURE, pres)
        }

        src.temperature?.let { temp ->
            obj.addProperty(TEMPERATURE, temp)
        }

        src.humidity?.let { hum ->
            obj.addProperty(HUMIDITY, hum)
        }

        return obj
    }
}

internal object EventDataSampleJsonAdapter : JsonSerializer<EventDataSample> {
    override fun serialize(src: EventDataSample, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {

        val obj = JsonObject()

        obj.add(DATE, context.serialize(src.date))
        obj.add(EVENTS, context.serialize(src.events))

        src.acceleration?.let { acc ->
            obj.addProperty(ACCELERATION, acc)
        }

        if (src.currentOrientation != Orientation.UNKNOWN) {
            obj.add(ORIENTATION, context.serialize(src.currentOrientation))
        }

        return obj
    }
}

internal object LocationDataJsonSerializer : JsonSerializer<LocationData> {
    override fun serialize(src: LocationData, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {

        val root = JsonObject()
        root.addProperty("lat", src.latitude)
        root.addProperty("long", src.longitude)
        root.addProperty("date", src.date.time)
        return root
    }
}