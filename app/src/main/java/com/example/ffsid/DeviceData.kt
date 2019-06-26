package com.example.ffsid

data class DeviceData(val deviceName: String?, val deviceAddress: String) {
    override fun equals(other: Any?): Boolean {
        val deviceData = other as DeviceData
        return deviceAddress == deviceData.deviceAddress
    }

    override fun hashCode(): Int {
        return deviceAddress.hashCode()
    }
}