package com.st.smartaglibrary.v2.catalog

import com.st.smartaglibrary.BuildConfig
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.net.UnknownHostException

internal interface NFCBoardCatalogServiceApi {
    @GET("catalog.json")
    suspend fun getNfcCatalogFirmwaresList(): Response<NfcV2BoardCatalog>

    @GET("chksum.json")
    suspend fun getNfcCatalogVersion(): NfcV2BoardCatalog
}

class NFCBoardCatalogService {

    suspend fun getNfcCatalog(): NfcV2BoardCatalog? {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.NFC_DB_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(NFCBoardCatalogServiceApi::class.java)

        return try{
            val response = service.getNfcCatalogFirmwaresList()
            when (response.isSuccessful) {
                true -> response.body()
                else -> null
            }
        } catch (e: HttpException) {
            return null
        } catch (e: UnknownHostException) {
            return null
        }
    }

    companion object {
        private lateinit var catalog: NfcV2BoardCatalog

        fun storeCatalog(nfcCatalog: NfcV2BoardCatalog){
            catalog = nfcCatalog
        }

        fun getCatalog(): NfcV2BoardCatalog {
            return catalog
        }

        fun getCurrentFirmware(boardID: Int, firmwareID: Int): NfcV2Firmware? {
            catalog.nfcV2FirmwareList.forEach { fw ->
                if(java.lang.Long.decode(fw.nfcDevID).toInt()== boardID && java.lang.Long.decode(fw.nfcFwID).toInt() == firmwareID){
                    return fw
                }
            }
            return null
        }

        fun extractAllVirtualSensorsIds(fw: NfcV2Firmware): List<Int> {
            val vsIdsList: ArrayList<Int> = ArrayList()
            fw.virtualSensors.forEach { vs ->
                vsIdsList.add(vs.id)
            }
            return vsIdsList.toList()
        }

        fun getCurrentThresholdIdFromName(fw: NfcV2Firmware, thType: String): Int? {
            fw.virtualSensors.forEach { vs ->
                if(vs.type == thType){
                    return vs.id
                }
            }
            return null
        }
    }

}