package com.lgh.uvccamera.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.lgh.uvccamera.callback.ConnectCallback
import com.lgh.uvccamera.config.CameraConfig
import com.lgh.uvccamera.utils.LogUtil.i

/**
 * 描述：usb插拔监听、连接工具类
 * 作者：liugh
 * 日期：2018/9/17
 * 版本：v2.0.0
 */
class UsbMonitor(private val mContext: Context, private val mConfig: CameraConfig?) : IMonitor {
    private val mUsbManager: UsbManager =
        mContext.getSystemService(Context.USB_SERVICE) as UsbManager
    private var mUsbReceiver: USBReceiver? = null
    override var usbController: UsbController? = null
    private var mConnectCallback: ConnectCallback? = null

    /**
     * 注册usb插拔监听广播
     */
    override fun registerReceiver() {
        i("registerReceiver")
        if (mUsbReceiver == null) {
            val filter = IntentFilter()
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            filter.addAction(ACTION_USB_DEVICE_PERMISSION)
            mUsbReceiver = USBReceiver()
            mContext.registerReceiver(mUsbReceiver, filter)
        }
    }

    /**
     * 注销usb插拔监听广播
     */
    override fun unregisterReceiver() {
        i("unregisterReceiver")
        if (mUsbReceiver != null) {
            mContext.unregisterReceiver(mUsbReceiver)
            mUsbReceiver = null
        }
    }

    override fun checkDevice() {
        i("checkDevice")
        val usbDevice = usbCameraDevice
        mConnectCallback?.let {
            if (isTargetDevice(usbDevice)) {
                it.onAttached(usbDevice)
            } else {
                it.onHasCamera(false)
            }
        }
    }

    override fun requestPermission(usbDevice: UsbDevice?) {
        i("requestPermission-->$usbDevice")
        if (mUsbManager.hasPermission(usbDevice)) {
            mConnectCallback?.onGranted(usbDevice, true)
        } else {
            val pendingIntent = PendingIntent.getBroadcast(
                mContext, 0, Intent(
                    ACTION_USB_DEVICE_PERMISSION
                ), 0
            )
            mUsbManager.requestPermission(usbDevice, pendingIntent)
        }
    }

    override fun connectDevice(usbDevice: UsbDevice?) {
        i("connectDevice-->$usbDevice")
        usbController = UsbController(mUsbManager, usbDevice)
        if (usbController!!.open() != null) {
            mConnectCallback?.onConnected(usbDevice)
        }
    }

    override fun closeDevice() {
        i("closeDevice")
        if (usbController != null) {
            usbController!!.close()
            usbController = null
        }
    }

    override val connection: UsbDeviceConnection?
        get() = if (usbController != null) {
            usbController!!.connection
        } else null

    fun setConnectCallback(callback: ConnectCallback?) {
        mConnectCallback = callback
    }

    /**
     * 是否存在usb摄像头
     *
     * @return
     */
    fun hasUsbCamera(): Boolean {
        return usbCameraDevice != null
    }

    /**
     * 获取usb摄像头设备
     *
     * @return
     */
    val usbCameraDevice: UsbDevice?
        get() {
            val deviceMap = mUsbManager.deviceList
            if (deviceMap != null) {
                for (usbDevice in deviceMap.values) {
                    if (isUsbCamera(usbDevice)) {
                        return usbDevice
                    }
                }
            }
            return null
        }

    /**
     * 判断某usb设备是否摄像头，usb摄像头的大小类是239-2
     *
     * @param usbDevice
     * @return
     */
    fun isUsbCamera(usbDevice: UsbDevice?): Boolean {
        return usbDevice != null && 239 == usbDevice.deviceClass && 2 == usbDevice.deviceSubclass
    }

    /**
     * 是否目标设备，是相机并且产品id和供应商id跟配置的一致
     *
     * @param usbDevice
     * @return
     */
    fun isTargetDevice(usbDevice: UsbDevice?): Boolean {
        if (!isUsbCamera(usbDevice)
            || mConfig == null || mConfig.productId != 0 && mConfig.productId != usbDevice!!.productId
            || mConfig.vendorId != 0 && mConfig.vendorId != usbDevice!!.vendorId
        ) {
            i("No target camera device")
            return false
        }
        i("Find target camera device")
        return true
    }

    /**
     * usb插拔广播监听类
     */
    private inner class USBReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val usbDevice = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
            i("usbDevice-->$usbDevice")
            if (!isTargetDevice(usbDevice) || mConnectCallback == null) {
                return
            }
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    i("onAttached")
                    mConnectCallback!!.onAttached(usbDevice)
                }
                ACTION_USB_DEVICE_PERMISSION -> {
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    mConnectCallback!!.onGranted(usbDevice, granted)
                    i("onGranted-->$granted")
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    i("onDetached")
                    mConnectCallback!!.onDetached(usbDevice)
                }
                else -> {}
            }
        }
    }

    companion object {
        private const val ACTION_USB_DEVICE_PERMISSION = "ACTION_USB_DEVICE_PERMISSION"
    }

}