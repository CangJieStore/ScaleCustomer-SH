package com.lgh.uvccamera

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.usb.UsbDevice
import android.view.*
import android.view.TextureView.SurfaceTextureListener
import com.lgh.uvccamera.bean.PicturePath
import com.lgh.uvccamera.callback.ConnectCallback
import com.lgh.uvccamera.callback.PhotographCallback
import com.lgh.uvccamera.callback.PictureCallback
import com.lgh.uvccamera.callback.PreviewCallback
import com.lgh.uvccamera.config.CameraConfig
import com.lgh.uvccamera.usb.UsbMonitor
import com.lgh.uvccamera.utils.FileUtil.deleteFile
import com.lgh.uvccamera.utils.FileUtil.getCacheFile
import com.lgh.uvccamera.utils.FileUtil.getDiskCacheDir
import com.lgh.uvccamera.utils.FileUtil.getSDCardDir
import com.lgh.uvccamera.utils.FileUtil.getSDCardFile
import com.lgh.uvccamera.utils.FileUtil.saveYuv2Jpeg
import com.lgh.uvccamera.utils.LogUtil.i
import com.serenegiant.usb.IButtonCallback
import com.serenegiant.usb.IFrameCallback
import com.serenegiant.usb.Size
import com.serenegiant.usb.UVCCamera
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import java.nio.ByteBuffer
import java.util.*


class UVCCameraProxy(private val mContext: Context) : IUVCCamera {
    private val mUsbMonitor: UsbMonitor

    /**
     * uvc相机实例
     *
     * @return
     */
    override var uVCCamera: UVCCamera? = null
    private var mPreviewView // 预览view
            : View? = null
    private var mSurface: Surface? = null
    private var mPictureCallback // 拍照成功回调
            : PictureCallback? = null
    private var mPhotographCallback // 设备上的拍照按钮点击回调
            : PhotographCallback? = null
    private var mPreviewCallback // 预览回调
            : PreviewCallback? = null
    private var mConnectCallback // usb连接回调
            : ConnectCallback? = null

    /**
     * 配置信息
     *
     * @return
     */
    override val config: CameraConfig = CameraConfig() // 相机相关配置
    protected var mPreviewRotation = 0f// 相机预览旋转角度
    protected var isTakePhoto = false // 是否拍照
    private var mPictureName: String? = null // 图片名称

    /**
     * 注册usb插拔监听广播
     */
    override fun registerReceiver() {
        mUsbMonitor.registerReceiver()
    }

    /**
     * 注销usb插拔监听广播
     */
    override fun unregisterReceiver() {
        mUsbMonitor.unregisterReceiver()
    }

    /**
     * 检查是否插入了usb摄像头，用于先插入设备再打开页面的场景
     */
    override fun checkDevice() {
        mUsbMonitor.checkDevice()
    }

    /**
     * 申请打开usb设备权限
     *
     * @param usbDevice
     */
    override fun requestPermission(usbDevice: UsbDevice?) {
        mUsbMonitor.requestPermission(usbDevice)
    }

    /**
     * 连接usb设备
     *
     * @param usbDevice
     */
    override fun connectDevice(usbDevice: UsbDevice?) {
        mUsbMonitor.connectDevice(usbDevice)
    }

    /**
     * 关闭usb设备
     */
    override fun closeDevice() {
        mUsbMonitor.closeDevice()
    }

    /**
     * 打开相机
     */
    override fun openCamera() {
        try {
            uVCCamera = UVCCamera()
            mUsbMonitor.usbController?.let { uVCCamera!!.open(it) }
            i("openCamera")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (uVCCamera != null && mConnectCallback != null) {
            mConnectCallback!!.onCameraOpened()
        }
    }

    /**
     * 关闭相机
     */
    override fun closeCamera() {
        try {
            if (uVCCamera != null) {
                uVCCamera!!.destroy()
                uVCCamera = null
            }
            mUsbMonitor.closeDevice()
            i("closeCamera")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置相机预览控件，这里封装了相关注册注销广播、检测设备、释放资源等操作
     *
     * @param surfaceView
     */
    override fun setPreviewSurface(surfaceView: SurfaceView?) {
        mPreviewView = surfaceView
        if (surfaceView != null && surfaceView.holder != null) {
            surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    i("surfaceCreated")
                    mSurface = holder.surface
                    checkDevice()
                    registerReceiver()
                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                    i("surfaceChanged")
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    i("surfaceDestroyed")
                    mSurface = null
                    unregisterReceiver()
                    closeCamera()
                }
            })
        }
    }

    /**
     * 设置相机预览控件，这里封装了相关注册注销广播、检测设备、释放资源等操作
     */
    override fun setPreviewTexture(textureView: TextureView?) {
        mPreviewView = textureView
        if (textureView != null) {
            if (mPreviewRotation != 0f) {
                textureView.rotation = mPreviewRotation
            }
            textureView.surfaceTextureListener = object : SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    i("onSurfaceTextureAvailable")
                    mSurface = Surface(surface)
                    checkDevice()
                    registerReceiver()
                }

                override fun onSurfaceTextureSizeChanged(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    i("onSurfaceTextureSizeChanged")
                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                    i("onSurfaceTextureDestroyed")
                    mSurface = null
                    unregisterReceiver()
                    closeCamera()
                    return false
                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            }
        }
    }

    /**
     * 设置相机预览旋转角度，暂时只支持TextureView
     *
     * @param rotation
     */
    override fun setPreviewRotation(rotation: Float) {
        if (mPreviewView != null && mPreviewView is TextureView) {
            mPreviewRotation = rotation
            mPreviewView?.rotation = rotation
        }
    }

    /**
     * 设置相机预览Surface
     *
     * @param surface
     */
    override fun setPreviewDisplay(surface: Surface?) {
        mSurface = surface
        try {
            if (uVCCamera != null && mSurface != null) {
                uVCCamera!!.setPreviewDisplay(mSurface)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置预览尺寸
     *
     * @param width
     * @param height
     */
    override fun setPreviewSize(width: Int, height: Int) {
        try {
            if (uVCCamera != null) {
                PICTURE_WIDTH = width
                PICTURE_HEIGHT = height
                uVCCamera!!.setPreviewSize(width, height)
                i("setPreviewSize-->$width * $height")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取相机预览尺寸
     *
     * @return
     */
    override val previewSize: Size?
        get() {
            try {
                if (uVCCamera != null) {
                    return uVCCamera!!.previewSize
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

    /**
     * 获取相机支持的预览尺寸
     *
     * @return
     */
    override val supportedPreviewSizes: List<Size>
        get() {
            try {
                if (uVCCamera != null) {
                    return uVCCamera!!.supportedSizeList
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ArrayList()
        }

    /**
     * 开始预览
     */
    override fun startPreview() {
        try {
            if (uVCCamera != null) {
                i("startPreview")
                val single = Single.create { emitter: SingleEmitter<Int> ->
                    uVCCamera!!.setButtonCallback(
                        object : IButtonCallback {
                            override fun onButton(button: Int, state: Int) {
                                i("button-->$button state-->$state")
                                // button等于1表示拍照按钮，state等于1表示按下，0松开
                                if (button == 1 && state == 0) {
                                    emitter.onSuccess(state)
                                }
                            }
                        })
                }.observeOn(AndroidSchedulers.mainThread())
                single.subscribe(object : SingleObserver<Int> {
                    override fun onSubscribe(d: Disposable) {}
                    override fun onSuccess(integer: Int) {
                        if (mPhotographCallback != null) {
                            mPhotographCallback!!.onPhotographClick()
                        }
                    }

                    override fun onError(e: Throwable) {}
                })

                // 图片预览流回调
                uVCCamera!!.setFrameCallback(object : IFrameCallback {
                    override fun onFrame(frame: ByteBuffer?) {
                        val lenght = frame!!.capacity()
                        val yuv = ByteArray(lenght)
                        frame[yuv]
                        if (mPreviewCallback != null) {
                            mPreviewCallback!!.onPreviewFrame(yuv)
                        }
                        if (isTakePhoto) {
                            i("take picture")
                            isTakePhoto = false
                            savePicture(yuv, PICTURE_WIDTH, PICTURE_HEIGHT, mPreviewRotation)
                        }
                    }
                }, UVCCamera.PIXEL_FORMAT_YUV420SP)
                if (mSurface != null) {
                    uVCCamera!!.setPreviewDisplay(mSurface)
                }
                uVCCamera!!.updateCameraParams()
                uVCCamera!!.startPreview()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 保存图片
     *
     * @param yuv
     * @param width
     * @param height
     * @param rotation
     */
    fun savePicture(yuv: ByteArray?, width: Int, height: Int, rotation: Float) {
        if (mPictureCallback == null) {
            return
        }
        i("savePicture")
        val single = Single.create<String> { emitter ->
            val file = getPictureFile(mPictureName)
            val path = saveYuv2Jpeg(file, yuv, width, height, rotation)
            emitter.onSuccess(path)
        }.observeOn(AndroidSchedulers.mainThread())
        single.subscribe(object : SingleObserver<String> {
            override fun onSubscribe(d: Disposable) {}
            override fun onSuccess(s: String) {
                mPictureCallback?.onPictureTaken(s)
            }

            override fun onError(e: Throwable) {
                mPictureCallback?.onPictureTaken(null)
            }
        })
    }

    /**
     * 停止预览
     */
    override fun stopPreview() {
        try {
            if (uVCCamera != null) {
                i("stopPreview")
                uVCCamera!!.setButtonCallback(null)
                uVCCamera!!.setFrameCallback(null, 0)
                uVCCamera!!.stopPreview()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 拍照
     */
    override fun takePicture() {
        isTakePhoto = true
        mPictureName = UUID.randomUUID().toString() + ".jpg"
    }

    /**
     * 拍照
     *
     * @param pictureName 图片名称
     */
    override fun takePicture(pictureName: String?) {
        isTakePhoto = true
        mPictureName = pictureName
    }

    /**
     * 设置usb设备连接回调
     *
     * @param callback
     */
    override fun setConnectCallback(callback: ConnectCallback?) {
        mConnectCallback = callback
        mUsbMonitor.setConnectCallback(callback)
    }

    /**
     * 设置预览回调
     *
     * @param callback
     */
    override fun setPreviewCallback(callback: PreviewCallback?) {
        mPreviewCallback = callback
    }

    /**
     * 设置拍照按钮点击回调
     *
     * @param callback
     */
    override fun setPhotographCallback(callback: PhotographCallback?) {
        mPhotographCallback = callback
    }

    /**
     * 设置拍照回调
     *
     * @param callback
     */
    override fun setPictureTakenCallback(callback: PictureCallback?) {
        mPictureCallback = callback
    }

    /**
     * 是否已经打开相机
     *
     * @return
     */
    override val isCameraOpen: Boolean
        get() = uVCCamera != null

    override fun hasCamera(): Boolean {
        return mUsbMonitor.hasUsbCamera()
    }

    /**
     * 删除图片缓存目录
     */
    override fun clearCache() {
        try {
            // 删除app缓存目录里的图片
            val cacheDir = File(getDiskCacheDir(mContext, config.dirName))
            deleteFile(cacheDir)

            // 删除sdcard目录里的图片
            val sdcardDir = getSDCardDir(config.dirName)
            deleteFile(sdcardDir!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取保存的图片文件
     *
     * @param pictureName
     * @return
     */
    protected fun getPictureFile(pictureName: String?): File? {
        var file: File? = null
        file = when (config.picturePath) {
            PicturePath.APPCACHE -> getCacheFile(mContext, config.dirName, pictureName!!)
            PicturePath.SDCARD -> getSDCardFile(config.dirName, pictureName)
            else -> getCacheFile(mContext, config.dirName, pictureName!!)
        }
        return file
    }

    companion object {
        private var PICTURE_WIDTH = 640
        private var PICTURE_HEIGHT = 480
    }

    init {
        mUsbMonitor = UsbMonitor(mContext, config)
    }
}