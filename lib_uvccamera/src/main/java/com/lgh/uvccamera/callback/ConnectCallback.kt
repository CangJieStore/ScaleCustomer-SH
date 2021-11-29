package com.lgh.uvccamera.callback

import android.hardware.usb.UsbDevice

interface ConnectCallback {
    /**
     * 插入设备
     *
     * @param usbDevice
     */
    fun onAttached(usbDevice: UsbDevice?)

    /**
     * USB设备授权回调
     *
     * @param usbDevice
     * @param granted   是否授权成功
     */
    fun onGranted(usbDevice: UsbDevice?, granted: Boolean)

    /**
     * 设备连接成功
     *
     * @param usbDevice
     */
    fun onConnected(usbDevice: UsbDevice?)

    /**
     * 相机打开成功
     */
    fun onCameraOpened()

    /**
     * 设备拔出
     *
     * @param usbDevice
     */
    fun onDetached(usbDevice: UsbDevice?)

    /**
     * 是否存在usb摄像头
     */
    fun onHasCamera(hasCamera: Boolean)
}