package com.st.assetTracking.dashboard.communication

import android.content.Context
import com.st.assetTracking.dashboard.model.*
import com.st.login.AuthData

interface DeviceListManager {

    enum class RegisterDeviceResult {
        Success,
        AlreadyRegistered,
        InvalidId,
        IOError,
        UnknownError
    }

    sealed class GetDeviceListResult {
        data class Success(val devices: List<Device>) : GetDeviceListResult()
        object IOError : GetDeviceListResult()
        object UnknownError : GetDeviceListResult()
    }

    fun buildDeviceManager(authData: AuthData, context: Context): DeviceManager

    suspend fun registerDevice(device: Device): RegisterDeviceResult
    suspend fun getDeviceList(): GetDeviceListResult

    suspend fun getApiKey(): ApiKey

    suspend fun getDeviceProfile(): List<DeviceProfile>

    suspend fun getDefaultDeviceProfile(): List<DeviceProfile>

    suspend fun removeDevice(deviceId: String) : RegisterDeviceResult

    suspend fun getDevicesPositions(): List<LastDeviceLocations>?

    suspend fun addMacAddressInfo(deviceId: String, macAddress: String): Boolean

}