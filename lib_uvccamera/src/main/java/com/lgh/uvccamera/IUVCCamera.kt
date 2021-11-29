package com.lgh.uvccamera

import android.hardware.usb.UsbDevice
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import com.lgh.uvccamera.callback.ConnectCallback
import com.lgh.uvccamera.callback.PhotographCallback
import com.lgh.uvccamera.callback.PictureCallback
import com.lgh.uvccamera.callback.PreviewCallback
import com.lgh.uvccamera.config.CameraConfig
import com.serenegiant.usb.Size
import com.serenegiant.usb.UVCCamera

interface IUVCCamera {
    /**
     * 注册usb插拔监听广播
     */
    fun registerReceiver()

    /**
     * 注销usb插拔监听广播
     */
    fun unregisterReceiver()

    /**
     * 检查是否插入了usb摄像头，用于先插入设备再打开页面的场景
     */
    fun checkDevice()

    /**
     * 申请打开usb设备权限
     *
     * @param usbDevice
     */
    fun requestPermission(usbDevice: UsbDevice?)

    /**
     * 连接usb设备
     *
     * @param usbDevice
     */
    fun connectDevice(usbDevice: UsbDevice?)

    /**
     * 关闭usb设备
     */
    fun closeDevice()

    /**
     * 打开相机
     */
    fun openCamera()

    /**
     * 关闭相机
     */
    fun closeCamera()

    /**
     * 设置相机预览控件，这里封装了相关注册注销广播、检测设备、释放资源等操作
     *
     * @param surfaceView
     */
    fun setPreviewSurface(surfaceView: SurfaceView?)

    /**
     * 设置相机预览控件，这里封装了相关注册注销广播、检测设备、释放资源等操作
     */
    fun setPreviewTexture(textureView: TextureView?)

    /**
     * 设置相机预览旋转角度，有些摄像头上下反了
     *
     * @param rotation
     */
    fun setPreviewRotation(rotation: Float)

    /**
     * 设置相机预览Surface
     *
     * @param surface
     */
    fun setPreviewDisplay(surface: Surface?)

    /**
     * 设置预览尺寸
     *
     * @param width
     * @param height
     */
    fun setPreviewSize(width: Int, height: Int)

    /**
     * 获取相机预览尺寸
     *
     * @return
     */
    val previewSize: Size?

    /**
     * 获取相机支持的预览尺寸
     *
     * @return
     */
    val supportedPreviewSizes: List<Size?>?

    /**
     * 开始预览
     */
    fun startPreview()

    /**
     * 停止预览
     */
    fun stopPreview()

    /**
     * 拍照
     */
    fun takePicture()

    /**
     * 拍照
     *
     * @param pictureName 图片名称
     */
    fun takePicture(pictureName: String?)

    /**
     * 设置usb设备连接回调
     *
     * @param callback
     */
    fun setConnectCallback(callback: ConnectCallback?)

    /**
     * 设置预览回调
     *
     * @param callback
     */
    fun setPreviewCallback(callback: PreviewCallback?)

    /**
     * 设置拍照按钮点击回调
     *
     * @param callback
     */
    fun setPhotographCallback(callback: PhotographCallback?)

    /**
     * 设置拍照回调
     *
     * @param callback
     */
    fun setPictureTakenCallback(callback: PictureCallback?)

    /**
     * uvc相机实例
     *
     * @return
     */
    val uVCCamera: UVCCamera?

    /**
     * 是否已经打开相机
     *
     * @return
     */
    val isCameraOpen: Boolean

    /**
     * 是否有相机存在
     */
    fun hasCamera(): Boolean

    /**
     * 配置信息
     *
     * @return
     */
    val config: CameraConfig?

    /**
     * 删除图片缓存
     */
    fun clearCache()
}