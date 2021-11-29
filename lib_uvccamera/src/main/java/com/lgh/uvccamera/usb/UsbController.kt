package com.lgh.uvccamera.usb

import android.hardware.usb.UsbManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.text.TextUtils
import com.lgh.uvccamera.usb.UsbController
import java.lang.StringBuilder

class UsbController(private val mUsbManager: UsbManager?, val usbDevice: UsbDevice?) {
    var connection: UsbDeviceConnection? = null
        private set

    fun open(): UsbDeviceConnection? {
        if (mUsbManager != null && usbDevice != null) {
            connection = mUsbManager.openDevice(usbDevice)
        }
        return connection
    }

    fun close() {
        if (connection != null) {
            connection!!.close()
            connection = null
        }
    }

    val vendorId: Int
        get() = usbDevice?.vendorId ?: 0
    val productId: Int
        get() = usbDevice?.productId ?: 0
    val fileDescriptor: Int
        get() = if (connection != null) connection!!.fileDescriptor else 0
    val busNum: Int
        get() {
            var busnum = 0
            if (usbDevice != null) {
                val name = usbDevice.deviceName
                val info =
                    if (!TextUtils.isEmpty(name)) name.split("/".toRegex()).toTypedArray() else null
                if (info != null && info.size > 1) {
                    busnum = info[info.size - 2].toInt()
                }
            }
            return busnum
        }
    val devNum: Int
        get() {
            var devnum = 0
            if (usbDevice != null) {
                val name = usbDevice.deviceName
                val info =
                    if (!TextUtils.isEmpty(name)) name.split("/".toRegex()).toTypedArray() else null
                if (info != null && info.isNotEmpty()) {
                    devnum = info[info.size - 1].toInt()
                }
            }
            return devnum
        }
    val uSBFSName: String?
        get() {
            var result: String? = null
            if (usbDevice != null) {
                val name = usbDevice.deviceName
                val info =
                    if (!TextUtils.isEmpty(name)) name.split("/".toRegex()).toTypedArray() else null
                if (info != null && info.size > 2) {
                    val sb = StringBuilder(info[0])
                    for (i in 1 until info.size - 2) {
                        sb.append("/").append(info[i])
                    }
                    result = sb.toString()
                }
            }
            return if (!TextUtils.isEmpty(result)) result else DEFAULT_USBFS
        }

    companion object {
        private const val DEFAULT_USBFS = "/dev/bus/usb"
    }
}