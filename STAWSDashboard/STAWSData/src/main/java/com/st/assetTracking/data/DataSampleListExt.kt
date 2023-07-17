package com.st.assetTracking.data

val List<DataSample>.sensorDataSamples: List<SensorDataSample>
    get() = this.filterIsInstance<SensorDataSample>()

val List<DataSample>.sensorDataSamplesSequence: Sequence<SensorDataSample>
    get() = this.asSequence()
            .filterIsInstance<SensorDataSample>()


val List<DataSample>.eventDataSamples: List<EventDataSample>
    get() = this.filterIsInstance<EventDataSample>()

val List<DataSample>.eventDataSamplesSequence: Sequence<EventDataSample>
    get() = this.asSequence()
            .filterIsInstance<EventDataSample>()