package com.lgh.uvccamera.usb

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection

interface IMonitor {
    fun registerReceiver()
    fun unregisterReceiver()
    fun checkDevice()
    fun requestPermission(usbDevice: UsbDevice?)
    fun connectDevice(usbDevice: UsbDevice?)
    fun closeDevice()
    val usbController: UsbController?
    val connection: UsbDeviceConnection?
}