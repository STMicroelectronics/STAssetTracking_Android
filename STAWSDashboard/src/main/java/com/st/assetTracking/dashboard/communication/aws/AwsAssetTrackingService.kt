package com.st.assetTracking.dashboard.communication.aws

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.babbel.mobile.android.commons.okhttpawssigner.OkHttpAwsV4Signer
import com.beust.klaxon.Klaxon
import com.google.gson.*
import com.st.assetTracking.dashboard.BuildConfig
import com.st.assetTracking.dashboard.communication.DeviceListManager
import com.st.assetTracking.dashboard.communication.DeviceListManager.GetDeviceListResult
import com.st.assetTracking.dashboard.communication.DeviceListManager.RegisterDeviceResult
import com.st.assetTracking.dashboard.communication.DeviceManager
import com.st.assetTracking.dashboard.model.*
import com.st.assetTracking.data.*
import com.st.login.AuthData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import org.ocpsoft.prettytime.PrettyTime
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.net.URLEncoder
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


internal interface AwsDeviceListManagerRestApi {

    @POST("v1/devices/")
    @Headers("Content-Type: application/json")
    suspend fun addDevice(@Body body: RequestBody): Response<ResponseBody>

    @DELETE("v1/devices/{device}")
    suspend fun deleteDevice(@Path("device", encoded = true) idDevice: String): Response<ResponseBody>

    @GET("v1/devices/")
    suspend fun getDeviceList(): Response<JsonArray>

    @GET("v1/apikeys/")
    suspend fun getApiKey(): JsonArray

    @POST("v1/apikeys/")
    @Headers("Content-Type: application/json")
    suspend fun registerApiKey(@Body body: RequestBody): Response<ResponseBody>

    @GET("v1/deviceprofiles?default_profiles=true")
    suspend fun getDefaultDeviceProfile() : JsonArray

    @GET("v1/deviceprofiles/")
    suspend fun getDeviceProfile() : JsonArray

    @PUT("v1/devices/{device}")
    @Headers("Content-Type: application/json")
    suspend fun addMacAddressInfo(@Path("device", encoded = true) idDevice: String, @Body body: RequestBody): Response<ResponseBody>

    /**
     * TODO: Add comment
     */
    @POST("v1/telemetry")
    @Headers("Content-Type: application/json")
    suspend fun uploadTelemetryData(@Body body: RequestBody): Response<ResponseBody>


    @GET("v1/data/")
    suspend fun getDeviceData(@Query("devices") devices: String,
                              @Query("timestampEnd") timestampEnd: Long,
                              @Query("timestampStart") timestampStart: Long,
                              @Query("type") type: String
    ): Response<JsonArray>

}

class AwsAssetTrackingService(private val authData: AuthData, context: Context) : DeviceListManager, DeviceManager {
    private var remoteDeviceListRepository: AwsDeviceListManagerRestApi
    private lateinit var remoteDeviceRepository: AwsDeviceListManagerRestApi

    private val ctx = context
    private val Auth_config_token = "TokenCollection"
    private val tokenReader: SharedPreferences = context.getSharedPreferences(Auth_config_token, Context.MODE_PRIVATE)
    private val signer: OkHttpAwsV4Signer = OkHttpAwsV4Signer(BuildConfig.AWS_REGION, "execute-api")
    private val signingInterceptor = AwsSigingInterceptor(signer)

    private var apiKey : String = ""
    private var owner : String = ""

    init {


        /**
         * Retrofit initialization for request with Auth_Z token request signature
         */
        //accessKeyZ = tokenReader.getString("accessKey_Z", "")!!
        //secretKeyZ = tokenReader.getString("secretKey_Z", "")!!
        //sessionTokenZ = tokenReader.getString("sessionToken_Z", "")!!

        signingInterceptor.setCredentials(authData.accessKey, authData.secretKey, authData.token)

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .build()
                return@addInterceptor chain.proceed(request)
            }
            .addInterceptor(signingInterceptor)
            .addInterceptor(loggingInterceptor)

        val gson: Gson = GsonBuilder()
            .setLenient()
            .create()

        remoteDeviceListRepository = Retrofit.Builder()
            .baseUrl(BuildConfig.AWS_ENDPOINT)
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(ScalarsConverterFactory.create())
            //.addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AwsDeviceListManagerRestApi::class.java)
    }

    override suspend fun getDeviceProfile(): List<DeviceProfile> {
        val result = remoteDeviceListRepository.getDeviceProfile()
        return unmarshalDeviceProfile(result.toString())
    }

    override suspend fun getDefaultDeviceProfile(): List<DeviceProfile> {
        val result = remoteDeviceListRepository.getDefaultDeviceProfile()
        return unmarshalDeviceProfile(result.toString())
    }

    override suspend fun registerDevice(device: Device): RegisterDeviceResult {
        try {
            val json = JsonObject()
            val jsonTech = JsonObject()

            json.addProperty("device_id", device.id)
            json.addProperty("label", device.name)
            json.addProperty("device_type", device.type.toString())

            jsonTech.addProperty("technology", device.type.toString())

            //if we need to register the device's certificate
            if((device.selfSigned) && (device.certificate!=null)) {
                jsonTech.addProperty("certificate", device.certificate)
            }

            //if we need to register LoRa devices
            if(device.deviceProfile!=null) {
                json.addProperty("device_profile", device.deviceProfile)
            }

            if(device.mac!=null) {
                jsonTech.addProperty("device_mac", device.mac)
            }
            if(device.devEui!=null) {
                jsonTech.addProperty("device_eui", device.devEui)
            }
            if(device.boardID!=null && device.firmwareID!=null){
                jsonTech.addProperty("board_id", device.boardID)
                jsonTech.addProperty("firmware_id", device.firmwareID)
            }

            json.add(device.type.toString(), jsonTech)

            val body: RequestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val result = remoteDeviceListRepository.addDevice(body)

            val result_code = result.code()

            return when(result_code){
                201 -> {
                    if(!device.selfSigned) {
                        //if we have requested a certificate for the device
                        val certificate: String? = result.body()?.string()
                        //Log.i("AWS Certificate","Received "+ certificate?: "NULL???")
                        device.certificate = certificate
//                    } else {
//                        Log.i("AWS Certificate","Registered")
                    }
                    RegisterDeviceResult.Success
                }
                /*400 -> {
                    val objError = JSONObject(result.errorBody()!!.string())
                    showErrorDialog(objError.get("msg").toString())
                    AwsErrorMessage(objError.get("msg").toString())
                    getRegisterErrorCodeFromMessage(objError.get("msg").toString())
                }
                500 -> {
                    val objError = JSONObject(result.errorBody()!!.string())
                    showErrorDialog(objError.get("msg").toString())
                    AwsErrorMessage(objError.get("msg").toString())
                    getRegisterErrorCodeFromMessage(objError.get("msg").toString())
                }*/
                else -> {
                    val objError = JSONObject(result.errorBody()!!.string())
                    AwsErrorMessage(objError.get("msg").toString())
                    RegisterDeviceResult.UnknownError
                }
            }

        } catch (e: HttpException) {
            return RegisterDeviceResult.IOError
        } catch (e: UnknownHostException) {
            //offline
            return RegisterDeviceResult.IOError
        }

    }

    private fun getRegisterErrorCodeFromMessage(body: String): RegisterDeviceResult {
        if (body.contains("CertificateValidationException"))
            return RegisterDeviceResult.InvalidId
        if (body.contains("ResourceAlreadyExistsException"))
            return RegisterDeviceResult.AlreadyRegistered
        return RegisterDeviceResult.UnknownError
    }

    override suspend fun getDeviceList(): GetDeviceListResult {
        return try {
            val result = remoteDeviceListRepository.getDeviceList()
            when (result.code()) {
                200 -> unmarshalDeviceList(result.body().toString())
                else -> GetDeviceListResult.UnknownError
            }

        } catch (e: HttpException) {
            GetDeviceListResult.IOError
        } catch (e: UnknownHostException) {
            //offline
            GetDeviceListResult.IOError
        }
    }


    override suspend fun getApiKey(): ApiKey {
        val result = remoteDeviceListRepository.getApiKey()
        return unmarshalApiKeys(result.toString())
    }

    private fun unmarshalDeviceList(body: String): GetDeviceListResult {
        var result : List<AwsDevice>?
        result = try {
            Klaxon().parseArray<AwsDevice>(body)
        }catch (e: Exception){
            null
        }

        if(!result.isNullOrEmpty()) {
            result = filterDeviceList(result)
        }

        if (result != null) {
            return GetDeviceListResult.Success(result.map { it.toDevice() })
        } else {
            return GetDeviceListResult.IOError
        }
    }

    private fun unmarshalDeviceProfile(body: String): List<DeviceProfile>{
        val listDeviceProfile = Klaxon()
            .parseArray<AwsDeviceProfile>(body)

        val deviceProfiles = ArrayList<DeviceProfile>()

        listDeviceProfile?.forEach {
            if(it.technology == Device.Type.LORA_TTN.toString()) {
                deviceProfiles.add(it.toDeviceProfile())
            }
        }

        return deviceProfiles.toList()

    }

    private suspend fun unmarshalApiKeys(body: String): ApiKey{
        return if(body == "[]"){
            val json = JsonObject()
            json.addProperty("label", "apiKey")
            val bodyReq: RequestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            remoteDeviceListRepository.registerApiKey(bodyReq)
            manageApiKeys(remoteDeviceListRepository.getApiKey().toString())
        }else{
            manageApiKeys(body)
        }
    }

    private fun manageApiKeys(body: String): ApiKey{
        val listAwsApiKey = Klaxon()
            .parseArray<AwsApiKey>(body)
        val aK = listAwsApiKey!!.map { it.toApiKey() }
        return aK[0]
    }

    private fun filterDeviceList(deviceList: List<AwsDevice>): List<AwsDevice>{
        val resultArr = ArrayList<AwsDevice>()

        deviceList.forEach {
            if(it.attributes.technology == Device.Type.ASTRA.toString() || it.attributes.technology == Device.Type.NFCTAG2.toString()
                || it.attributes.technology == Device.Type.NFCTAG1.toString() || it.attributes.technology == Device.Type.SENSORTILEBOX.toString()
                || it.attributes.technology == Device.Type.SENSORTILEBOXPRO.toString()){
                resultArr.add(it)
            }
        }
        return resultArr.toList()
    }

    override fun buildDeviceManager(authData: AuthData, context: Context): DeviceManager = AwsAssetTrackingService(authData, context)

    override suspend fun getAllDeviceDataFor(geolocationData: JsonArray?, telemetryData: JsonArray?, eventData: JsonArray?): DeviceManager.DeviceDataResult {
        return unmarshalDeviceData(geolocationData, telemetryData, eventData)
    }

    override suspend fun getAllDeviceGenericDataFor(boardID: Int?, firmwareID: Int?, geolocationData: JsonArray?, telemetryData: JsonArray?): DeviceManager.DeviceGenericDataResult {
        return if(boardID != null && firmwareID != null) {
            unmarshalGenericDeviceData(boardID, firmwareID, geolocationData, telemetryData)
        } else {
            DeviceManager.DeviceGenericDataResult.UnknownError
        }
    }

    override suspend fun getGeolocationDataFor(deviceId: String, range: ClosedRange<Date>): Response<JsonArray> {
        val sinceTimestamp = range.start.time
        val nowTimestamp = range.endInclusive.time

        val res = remoteDeviceListRepository.getDeviceData(deviceId,
            nowTimestamp, sinceTimestamp, "geolocation")
        return res
    }
    override suspend fun getTelemetryDataFor(deviceId: String, range: ClosedRange<Date>): Response<JsonArray> {
        val sinceTimestamp = range.start.time
        val nowTimestamp = range.endInclusive.time

        return remoteDeviceListRepository.getDeviceData(deviceId,
            nowTimestamp, sinceTimestamp, "telemetry")
    }
    override suspend fun getEventDataFor(deviceId: String, range: ClosedRange<Date>): Response<JsonArray> {
        val sinceTimestamp = range.start.time
        val nowTimestamp = range.endInclusive.time

        return remoteDeviceListRepository.getDeviceData(deviceId,
            nowTimestamp, sinceTimestamp, "events")

    }

    private fun unmarshalDeviceData(bodyGeoLocation: JsonArray?, bodyTelemetry: JsonArray?, bodyEvent: JsonArray?): DeviceManager.DeviceDataResult {


        val telemetry = Klaxon().parseArray<AwsTelemetryData>(bodyTelemetry.toString())
        val event = Klaxon().parseArray<AwsEventData>(bodyEvent.toString())
        val locations = Klaxon().parseArray<AwsLocationData>(bodyGeoLocation.toString())

        val telemetryEvent = mutableListOf<DataSample>()
        val geolocationList = mutableListOf<LocationData>()

        locations?.forEach { loc ->
            val listLoc = loc.toLocationData()
            listLoc.forEach{
                geolocationList.add(it)
            }
        }

        telemetry?.forEach { tel ->
            val listSDS = tel.toTelemetryData()
            listSDS.forEach{
                telemetryEvent.add(it)
            }
        }

        event?.forEach { ev ->
            val listSDS = ev.toEventData()
            listSDS.forEach{
                telemetryEvent.add(it)
            }
        }

        val result = AwsDeviceData(telemetryEvent, geolocationList)

        return DeviceManager.DeviceDataResult.Success(result.toDeviceData())
    }

    private fun unmarshalGenericDeviceData(boardID: Int, firmwareID: Int, bodyGeoLocation: JsonArray?, bodyTelemetry: JsonArray?): DeviceManager.DeviceGenericDataResult {
        val genericSamples = Klaxon().parseArray<AwsTelemetryData>(bodyTelemetry.toString())
        val locations = Klaxon().parseArray<AwsLocationData>(bodyGeoLocation.toString())

        val genericSamplesList = mutableListOf<GenericDataSample>()
        val geolocationList = mutableListOf<LocationData>()

        locations?.forEach { loc ->
            val listLoc = loc.toLocationData()
            listLoc.forEach{
                geolocationList.add(it)
            }
        }

        genericSamples?.forEach { gen ->
            val listGenericSamples = gen.toGenericData(boardID, firmwareID)
            listGenericSamples.forEach{
                genericSamplesList.add(it)
            }
        }

        val result = AwsDeviceGenericData(genericSamplesList, geolocationList)

        return DeviceManager.DeviceGenericDataResult.Success(result.toDeviceData())
    }

    private fun initializeRetrofitApiKEy(){
        /** Retrofit initialization for POST Telemetry request with ApiKey token request signature */

        apiKey = tokenReader.getString("ApiKey", "")!!
        owner = tokenReader.getString("Owner", "")!!

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/json, text/plain, *")
                    .addHeader("Content-Type", "application/json;charset=UTF-8")
                    .addHeader("Authorization", "$owner.$apiKey")
                    .build()
                return@addInterceptor chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .build()

        remoteDeviceRepository = Retrofit.Builder()
            .baseUrl(BuildConfig.AWS_ENDPOINT)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AwsDeviceListManagerRestApi::class.java)
    }

    override suspend fun uploadNewTelemetryData(deviceId: String, technology: String, data: List<DataSample>, currentLocation: LocationData?): DeviceManager.SaveDataResult {

        initializeRetrofitApiKEy()

        val jsonBase = initializeBaeJSON(deviceId)
        val jsonArrayValues = JsonArray()



        var pointer = 0
        var i = 0
        var telemetryPackets: List<SensorDataSample>
        var eventPackets: List<EventDataSample>
        /**
         * Create JSON Telemetry section
         */
        if(data.sensorDataSamples.size > 100){
            val iteration : Int = (data.sensorDataSamples.size / 100)
            val reminder : Int = (data.sensorDataSamples.size % 100)
            while(i<iteration) {
                telemetryPackets = data.sensorDataSamples.subList(pointer, pointer + 100)
                Log.i("PACKET ", i.toString() + " - " + telemetryPackets.size.toString())
                addTelemetryValues(jsonArrayValues, technology, telemetryPackets)
                i += 1
                pointer +=100
            }
            if(reminder != 0){
                telemetryPackets = data.sensorDataSamples.subList(pointer, data.sensorDataSamples.size)
                Log.i("PACKET ", i.toString() + " - " + telemetryPackets.size.toString())
                addTelemetryValues(jsonArrayValues, technology, telemetryPackets)
            }
        }else{
            if(data.sensorDataSamples.isNotEmpty()){
                addTelemetryValues(jsonArrayValues, technology, data.sensorDataSamples)
                Log.i("TOTAL PACKET ", i.toString() + " - " + data.size.toString())
            }
        }

        i=0
        pointer = 0
        /**
         * Create JSON Events section
         */
        if(data.eventDataSamples.size > 100){
            val iteration : Int = (data.eventDataSamples.size / 100)
            val reminder : Int = (data.eventDataSamples.size % 100)
            while(i<iteration) {
                eventPackets = data.eventDataSamples.subList(pointer, pointer + 100)
                Log.i("PACKET ", i.toString() + " - " + eventPackets.size.toString())
                addEventValues(jsonArrayValues, technology, eventPackets)
                i += 1
                pointer +=100
            }
            if(reminder != 0){
                eventPackets = data.eventDataSamples.subList(pointer, data.eventDataSamples.size)
                Log.i("PACKET ", i.toString() + " - " + eventPackets.size.toString())
                addEventValues(jsonArrayValues, technology, eventPackets)
            }
        }else{
            if(data.eventDataSamples.isNotEmpty()){
                addEventValues(jsonArrayValues, technology, data.eventDataSamples)
            }
        }

        /**
         * Create JSON Geolocation section
         */
        val location = currentLocation ?: dummyLocation()
        val jsonLocation = createJSONLocation(location)
        jsonArrayValues.add(jsonLocation)

        /**
         * Upload Telemetry Data
         */
        jsonBase.add("values", jsonArrayValues)

        val uploadBody: RequestBody = jsonBase.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val result = remoteDeviceRepository.uploadTelemetryData(uploadBody)

        return when (result.code()) {
            200 -> DeviceManager.SaveDataResult.Success
            //500 -> DeviceManager.SaveDataResult.InvalidData
            //else -> DeviceManager.SaveDataResult.UnknownError
            else -> {
                val objError = JSONObject(result.errorBody()!!.string())
                AwsErrorMessage(objError.get("msg").toString())
                DeviceManager.SaveDataResult.UnknownError
            }
        }

    }

    override suspend fun uploadNewGenericData(apiKey: ApiKey, deviceId: String, technology: String, data: List<GenericDSHSample>, currentLocation: LocationData?): DeviceManager.SaveDataResult {
        initializeRetrofitWithApiKEy(apiKey)

        val jsonBase = initializeBaeJSON(deviceId)
        val jsonArrayValues = JsonArray()

        var pointer = 0
        var i = 0
        var telemetryPackets: List<GenericDSHSample>

        /* Create JSON Telemetry section */
        if(data.size > 100){
            val iteration : Int = (data.size / 100)
            val reminder : Int = (data.size % 100)
            while(i<iteration) {
                telemetryPackets = data.subList(pointer, pointer + 100)
                Log.i("PACKET ", i.toString() + " - " + telemetryPackets.size.toString())
                addGenericValues(jsonArrayValues, technology, telemetryPackets)
                i += 1
                pointer +=100
            }
            if(reminder != 0){
                telemetryPackets = data.subList(pointer, data.size)
                Log.i("PACKET ", i.toString() + " - " + telemetryPackets.size.toString())
                addGenericValues(jsonArrayValues, technology, telemetryPackets)
            }
        }else{
            if(data.isNotEmpty()){
                addGenericValues(jsonArrayValues, technology, data)
                Log.i("TOTAL PACKET ", i.toString() + " - " + data.size.toString())
            }
        }

        /* Create JSON Geolocation section */
        val location = currentLocation ?: dummyLocation()
        val jsonLocation = createJSONLocation(location)
        jsonArrayValues.add(jsonLocation)

        /* Upload Telemetry Data */
        jsonBase.add("values", jsonArrayValues)

        val uploadBody: RequestBody = jsonBase.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val result = remoteDeviceRepository.uploadTelemetryData(uploadBody)

        return when (result.code()) {
            200 -> DeviceManager.SaveDataResult.Success
            else -> {
                val objError = JSONObject(result.errorBody()!!.string())
                AwsErrorMessage(objError.get("message").toString())
                DeviceManager.SaveDataResult.UnknownError
            }
        }
    }

    /** Retrofit initialization for POST Telemetry request with ApiKey token request signature */
    private fun initializeRetrofitWithApiKEy(apiKey: ApiKey){
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/json, text/plain, *")
                    .addHeader("Content-Type", "application/json;charset=UTF-8")
                    .addHeader("Authorization", "${apiKey.owner}.${apiKey.apiKey}")
                    .build()
                return@addInterceptor chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .build()

        remoteDeviceRepository = Retrofit.Builder()
            .baseUrl(BuildConfig.AWS_ENDPOINT)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AwsDeviceListManagerRestApi::class.java)
    }

    private fun initializeBaeJSON(id: String) : JsonObject{
        val json = JsonObject()
        json.addProperty("device_id", id)
        return json
    }

    private fun addGenericValues(jsonArrayValues: JsonArray, technology: String, data: List<GenericDSHSample>){
        val jsonTech = JsonObject()
        jsonTech.addProperty("tech", technology)
        val genericSamples = data.getGenericSample()
        Log.i("TELEMETRY - ", data.size.toString())
        for (sample in genericSamples) {
            val jsonTelemetryValue = JsonObject()
            jsonTelemetryValue.addProperty("ts",sample.date?.time)
            jsonTelemetryValue.addProperty("t", sample.type)
            jsonTelemetryValue.addProperty("v", sample.value)
            jsonTelemetryValue.add("metadata", jsonTech)
            jsonArrayValues.add(jsonTelemetryValue)
        }
    }

    private fun addTelemetryValues(jsonArrayValues: JsonArray, technology: String, data: List<DataSample>){

        Log.i("TELEMETRY - ", data.size.toString())

        for (sensor in data.sensorDataSamples) {
            if(sensor.temperature != null){
                val jsonTelemetryValue = createJSONTelemtry(sensor, "temperature", technology)
                jsonArrayValues.add(jsonTelemetryValue)
            }
            if(sensor.pressure != null){
                val jsonTelemetryValue = createJSONTelemtry(sensor, "pressure", technology)
                jsonArrayValues.add(jsonTelemetryValue)
            }
            if(sensor.humidity != null){
                val jsonTelemetryValue = createJSONTelemtry(sensor, "humidity", technology)
                jsonArrayValues.add(jsonTelemetryValue)
            }
            if(sensor.acceleration != null){
                val jsonTelemetryValue = createJSONTelemtry(sensor, "acceleration", technology)
                jsonArrayValues.add(jsonTelemetryValue)
            }
            if(sensor.gyroscope != null){
                val jsonTelemetryValue = createJSONTelemtry(sensor, "gyroscope", technology)
                jsonArrayValues.add(jsonTelemetryValue)
            }
        }
    }

    private fun addEventValues(jsonArrayValues: JsonArray, technology: String, data: List<DataSample>){
        for (sensor in data.eventDataSamples) {
            val jsonEventValue = createJSONEvent(sensor, technology)
            jsonArrayValues.add(jsonEventValue)
        }
    }

    private fun createJSONLocation(location: LocationData) : JsonObject{
        val jsonLocationDetails = JsonObject()
        val jsonLocationValue = JsonObject()

        jsonLocationDetails.addProperty("ts", Date().time)
        jsonLocationDetails.addProperty("t", "gnss")

        jsonLocationValue.addProperty("lon", location.longitude)
        jsonLocationValue.addProperty("lat", location.latitude)
        jsonLocationValue.addProperty("ele", 0)

        jsonLocationDetails.add("v", jsonLocationValue)

        return jsonLocationDetails
    }

    private fun createJSONTelemtry(sensor: SensorDataSample, type: String, technology: String, ) : JsonObject{
        val jsonTelemetryValue = JsonObject()
        val jsonTech = JsonObject()
        jsonTech.addProperty("tech", technology)

        when (type) {
            "temperature" -> {
                jsonTelemetryValue.addProperty("ts", sensor.date.time)
                jsonTelemetryValue.addProperty("t", "tem")
                jsonTelemetryValue.addProperty("v", sensor.temperature)
                jsonTelemetryValue.add("metadata", jsonTech)
            }
            "pressure" -> {
                jsonTelemetryValue.addProperty("ts", sensor.date.time)
                jsonTelemetryValue.addProperty("t", "pre")
                jsonTelemetryValue.addProperty("v", sensor.pressure)
                jsonTelemetryValue.add("metadata", jsonTech)
            }
            "humidity" -> {
                jsonTelemetryValue.addProperty("ts", sensor.date.time)
                jsonTelemetryValue.addProperty("t", "hum")
                jsonTelemetryValue.addProperty("v", sensor.humidity)
                jsonTelemetryValue.add("metadata", jsonTech)
            }
            "acceleration" -> {
                jsonTelemetryValue.addProperty("ts", sensor.date.time)
                jsonTelemetryValue.addProperty("t", "acc")
                jsonTelemetryValue.addProperty("v", sensor.acceleration)
                jsonTelemetryValue.add("metadata", jsonTech)
            }
            "gyroscope" -> {
                val gyroObject = createJSONGyroObject(sensor)
                jsonTelemetryValue.addProperty("ts", sensor.date.time)
                jsonTelemetryValue.addProperty("t", "gyr")
                jsonTelemetryValue.add("v", gyroObject)
                jsonTelemetryValue.add("metadata", jsonTech)
            }
        }

        return jsonTelemetryValue
    }

    private fun createJSONEvent(sensor: EventDataSample, technology: String) : JsonObject{
        val jsonEventValues = JsonObject()
        val jsonValueObject = JsonObject()
        val jsonTech = JsonObject()
        jsonTech.addProperty("tech", technology)

        when {
            sensor.events[0] == AccelerationEvent.ACCELERATION_WAKE_UP -> {
                jsonEventValues.addProperty("ts", sensor.date.time)
                jsonEventValues.addProperty("t", "evt")

                jsonValueObject.addProperty("et", "threshold")
                jsonValueObject.addProperty("m", "wakeup")

                jsonEventValues.add("v", jsonValueObject)
                jsonEventValues.add("metadata", jsonTech)
            }
            sensor.events[0] == AccelerationEvent.ACCELERATION_TILT_35 -> {
                jsonEventValues.addProperty("ts", sensor.date.time)
                jsonEventValues.addProperty("t", "evt")

                jsonValueObject.addProperty("et", "threshold")
                jsonValueObject.addProperty("m", "tilt")

                jsonEventValues.add("v", jsonValueObject)
                jsonEventValues.add("metadata", jsonTech)
            }
            else -> {
                jsonEventValues.addProperty("ts", sensor.date.time)
                jsonEventValues.addProperty("t", "evt")

                jsonValueObject.addProperty("et", "threshold")
                jsonValueObject.addProperty("m", "orientation")
                jsonValueObject.addProperty("l", sensor.currentOrientation.toString())

                jsonEventValues.add("v", jsonValueObject)
                jsonEventValues.add("metadata", jsonTech)
            }
        }

        return jsonEventValues
    }

    private fun createJSONGyroObject(sensor: SensorDataSample) : JsonObject {
        val jsonGyroValueObject = JsonObject()

        jsonGyroValueObject.addProperty("x", sensor.gyroscope?.x ?: 0.0f)
        jsonGyroValueObject.addProperty("y", sensor.gyroscope?.y ?: 0.0f)
        jsonGyroValueObject.addProperty("z", sensor.gyroscope?.z ?: 0.0f)

        return jsonGyroValueObject
    }

    override suspend fun removeDevice(deviceId: String) : RegisterDeviceResult {
        return try {
            val result = remoteDeviceListRepository.deleteDevice(URLEncoder.encode(deviceId, "utf-8"))
            return when(result.code()){
                200 -> {
                    RegisterDeviceResult.Success
                }
                else -> {
                    /**
                     * TODO: Handle errors like others ...
                     *
                     * val objError = JSONObject(result.errorBody()!!.string())
                     * AwsErrorMessage(objError.get("msg").toString())
                     *
                     * Actually JSON error message (for 404 error) is: {"msg":"[object Object]"}
                     *
                     * To add when fixed on cloud dashboard
                     */
                    RegisterDeviceResult.UnknownError
                }
            }
        } catch (e: HttpException) {
            RegisterDeviceResult.IOError
        }
    }

    override suspend fun addMacAddressInfo(deviceId: String, macAddress: String): Boolean {

        Thread.sleep(10000)

        val json = JsonObject()

        val jsonAttributes = JsonObject()
        jsonAttributes.addProperty("mac", macAddress)

        json.add("attributes", jsonAttributes)

        val bodyReq: RequestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        return try {
            val result = remoteDeviceListRepository.addMacAddressInfo(
                URLEncoder.encode(deviceId, "utf-8"),
                bodyReq)
            return when (result.code()) {
                200 -> true
                else -> false
            }
        }catch (e: HttpException) {
            println("PUT Adding Mac Address EXCEPTION: $e")
            false
        }
    }

    override suspend fun getDevicesPositions(): List<LastDeviceLocations>? {
        return try {
            val result = remoteDeviceListRepository.getDeviceList()
            when (result.code()) {
                200 -> unmarshalDevicePositions(result.body().toString())
                else -> null
            }

        } catch (e: HttpException) {
            println("GET Devices Position EXCEPTION: $e")
            null
        } catch (e: UnknownHostException) {
            println("GET Devices Position EXCEPTION: $e")
            null
        }
    }

    private fun unmarshalDevicePositions(body: String): List<LastDeviceLocations>? {
        var result : List<AwsDevice>?
        var listLocationData: List<LastDeviceLocations> = emptyList()
        result = try {
            Klaxon().parseArray(body)
        }catch (e: Exception){
            println("Parsing Devices Position EXCEPTION: $e")
            null
        }

        if(!result.isNullOrEmpty()) {
            val newResult = result.map { it.toDevice() }
            println(newResult)
            result = filterDeviceList(result)
            listLocationData = extractLocationsData(result)
        }

        return listLocationData

    }

    //If Android version is >= O we can set x minute, hours, day, years ago instead of Date
    private fun extractLocationsData(deviceList: List<AwsDevice>): List<LastDeviceLocations>{
        val p = PrettyTime()

        var result = ArrayList<LastDeviceLocations>()
        deviceList.forEach {
            if(it.configuration_signaled!=null){
                if(it.configuration_signaled.data != null) {
                    if(it.configuration_signaled.data.geolocation != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            result.add(LastDeviceLocations(it.configuration_signaled.data.geolocation[0].v?.lat!!,
                                it.configuration_signaled.data.geolocation[0].v?.lon!!,
                                p.format(Date(it.configuration_signaled.data.geolocation[0].ts!!)),
                                it.attributes.label!!,it.attributes.technology))
                        }else{
                            result.add(LastDeviceLocations(it.configuration_signaled.data.geolocation[0].v?.lat!!,
                                it.configuration_signaled.data.geolocation[0].v?.lon!!,
                                (Date(it.configuration_signaled.data.geolocation[0].ts!!)).toString(),
                                it.attributes.label!!,it.attributes.technology))
                        }

                    }
                }
            }
        }

        print(result)

        return result.toList()
    }

    private fun dummyLocation(): LocationData {
        return LocationData(0.0f, 0.0f, Date())
    }


    /** TODO: To remove */

    data class AccForPolaris(
        val date: Date,
        val x: Float,
        val y: Float,
        val z: Float)

    data class GyroForPolaris(
        val date: Date,
        val x: Float,
        val y: Float,
        val z: Float)

    suspend fun uploadTelemetryAccForPolaris(id: String, accData: List<AccForPolaris>, gyroData: List<GyroForPolaris>){

        Log.i("ACCELEROMETER - ", accData.size.toString())
        Log.i("GYROSCOPE - ", gyroData.size.toString())

        initializeRetrofitApiKEy()

        /**
         * Upload Telemetry
         */
        val jsonAccTelemetry = initializeBaeJSON(id)
        val jsonGyroTelemetry = initializeBaeJSON(id)

        val jsonAccTelemetryArrayValues = JsonArray()
        val jsonGyroTelemetryArrayValues = JsonArray()

        val jsonAccTelemetryValue = JsonObject()
        val jsonGyroTelemetryValue = JsonObject()

        val jsonAccValueObject = JsonObject()
        val jsonGyroValueObject = JsonObject()

        for (acc in accData) {
            jsonAccTelemetryValue.addProperty("ts", acc.date.time)
            jsonAccTelemetryValue.addProperty("t", "acc")

            jsonAccValueObject.addProperty("x", acc.x)
            jsonAccValueObject.addProperty("y", acc.y)
            jsonAccValueObject.addProperty("z", acc.z)

            jsonAccTelemetryValue.add("v", jsonAccValueObject)

            jsonAccTelemetryArrayValues.add(jsonAccTelemetryValue)
        }

        for (gyro in gyroData) {
            jsonGyroTelemetryValue.addProperty("ts", gyro.date.time)
            jsonGyroTelemetryValue.addProperty("t", "gyr")

            jsonGyroValueObject.addProperty("x", gyro.x)
            jsonGyroValueObject.addProperty("y", gyro.y)
            jsonGyroValueObject.addProperty("z", gyro.z)

            jsonGyroTelemetryValue.add("v", jsonGyroValueObject)

            jsonGyroTelemetryArrayValues.add(jsonGyroTelemetryValue)
        }

        jsonAccTelemetry.add("values", jsonAccTelemetryArrayValues)
        jsonGyroTelemetry.add("values", jsonGyroTelemetryArrayValues)

        val bodyAccTelemetry: RequestBody = jsonAccTelemetry.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        remoteDeviceRepository.uploadTelemetryData(bodyAccTelemetry)

        val bodyGyroTelemetry: RequestBody = jsonGyroTelemetry.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        remoteDeviceRepository.uploadTelemetryData(bodyGyroTelemetry)
    }
}
