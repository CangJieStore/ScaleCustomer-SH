package com.serenegiant.usb

import android.graphics.SurfaceTexture
import android.text.TextUtils
import android.view.Surface
import android.view.SurfaceHolder
import com.lgh.uvccamera.usb.UsbController
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class UVCCamera {
    companion object {
        const val DEFAULT_PREVIEW_WIDTH = 640
        const val DEFAULT_PREVIEW_HEIGHT = 480
        const val DEFAULT_PREVIEW_MODE = 0
        const val DEFAULT_PREVIEW_MIN_FPS = 1
        const val DEFAULT_PREVIEW_MAX_FPS = 31
        const val DEFAULT_BANDWIDTH = 1.0f
        const val FRAME_FORMAT_YUYV = 0
        const val FRAME_FORMAT_MJPEG = 1
        const val PIXEL_FORMAT_RAW = 0
        const val PIXEL_FORMAT_YUV = 1
        const val PIXEL_FORMAT_RGB565 = 2
        const val PIXEL_FORMAT_RGBX = 3
        const val PIXEL_FORMAT_YUV420SP = 4 // NV12
        const val PIXEL_FORMAT_NV21 = 5 // = YVU420SemiPlanar,NV21，但是保存到jpg颜色失真

        //--------------------------------------------------------------------------------
        const val CTRL_SCANNING = 0x00000001 // D0:  Scanning Mode
        const val CTRL_AE = 0x00000002 // D1:  Auto-Exposure Mode
        const val CTRL_AE_PRIORITY = 0x00000004 // D2:  Auto-Exposure Priority
        const val CTRL_AE_ABS = 0x00000008 // D3:  Exposure Time (Absolute)
        const val CTRL_AR_REL = 0x00000010 // D4:  Exposure Time (Relative)
        const val CTRL_FOCUS_ABS = 0x00000020 // D5:  Focus (Absolute)
        const val CTRL_FOCUS_REL = 0x00000040 // D6:  Focus (Relative)
        const val CTRL_IRIS_ABS = 0x00000080 // D7:  Iris (Absolute)
        const val CTRL_IRIS_REL = 0x00000100 // D8:  Iris (Relative)
        const val CTRL_ZOOM_ABS = 0x00000200 // D9:  Zoom (Absolute)
        const val CTRL_ZOOM_REL = 0x00000400 // D10: Zoom (Relative)
        const val CTRL_PANTILT_ABS = 0x00000800 // D11: PanTilt (Absolute)
        const val CTRL_PANTILT_REL = 0x00001000 // D12: PanTilt (Relative)
        const val CTRL_ROLL_ABS = 0x00002000 // D13: Roll (Absolute)
        const val CTRL_ROLL_REL = 0x00004000 // D14: Roll (Relative)
        const val CTRL_FOCUS_AUTO = 0x00020000 // D17: Focus, Auto
        const val CTRL_PRIVACY = 0x00040000 // D18: Privacy
        const val CTRL_FOCUS_SIMPLE = 0x00080000 // D19: Focus, Simple
        const val CTRL_WINDOW = 0x00100000 // D20: Window
        const val PU_BRIGHTNESS = -0x7fffffff // D0: Brightness
        const val PU_CONTRAST = -0x7ffffffe // D1: Contrast
        const val PU_HUE = -0x7ffffffc // D2: Hue
        const val PU_SATURATION = -0x7ffffff8 // D3: Saturation
        const val PU_SHARPNESS = -0x7ffffff0 // D4: Sharpness
        const val PU_GAMMA = -0x7fffffe0 // D5: Gamma
        const val PU_WB_TEMP = -0x7fffffc0 // D6: White Balance Temperature
        const val PU_WB_COMPO = -0x7fffff80 // D7: White Balance Component
        const val PU_BACKLIGHT = -0x7fffff00 // D8: Backlight Compensation
        const val PU_GAIN = -0x7ffffe00 // D9: Gain
        const val PU_POWER_LF = -0x7ffffc00 // D10: Power Line Frequency
        const val PU_HUE_AUTO = -0x7ffff800 // D11: Hue, Auto
        const val PU_WB_TEMP_AUTO = -0x7ffff000 // D12: White Balance Temperature, Auto
        const val PU_WB_COMPO_AUTO = -0x7fffe000 // D13: White Balance Component, Auto
        const val PU_DIGITAL_MULT = -0x7fffc000 // D14: Digital Multiplier
        const val PU_DIGITAL_LIMIT = -0x7fff8000 // D15: Digital Multiplier Limit
        const val PU_AVIDEO_STD = -0x7fff0000 // D16: Analog Video Standard
        const val PU_AVIDEO_LOCK = -0x7ffe0000 // D17: Analog Video Lock Status
        const val PU_CONTRAST_AUTO = -0x7ffc0000 // D18: Contrast, Auto

        // uvc_status_class from libuvc.h
        const val STATUS_CLASS_CONTROL = 0x10
        const val STATUS_CLASS_CONTROL_CAMERA = 0x11
        const val STATUS_CLASS_CONTROL_PROCESSING = 0x12

        // uvc_status_attribute from libuvc.h
        const val STATUS_ATTRIBUTE_VALUE_CHANGE = 0x00
        const val STATUS_ATTRIBUTE_INFO_CHANGE = 0x01
        const val STATUS_ATTRIBUTE_FAILURE_CHANGE = 0x02
        const val STATUS_ATTRIBUTE_UNKNOWN = 0xff
        private var isLoaded = false
        fun getSupportedSize(type: Int, supportedSize: String?): List<Size> {
            val result: MutableList<Size> = ArrayList()
            if (!TextUtils.isEmpty(supportedSize)) try {
                val json = JSONObject(supportedSize)
                val formats = json.getJSONArray("formats")
                val formatNum = formats.length()
                for (i in 0 until formatNum) {
                    val format = formats.getJSONObject(i)
                    if (format.has("type") && format.has("size")) {
                        val formatType = format.getInt("type")
                        if (formatType == type || type == -1) {
                            addSize(format, formatType, 0, result)
                        }
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return result
        }

        @Throws(JSONException::class)
        private fun addSize(
            format: JSONObject,
            formatType: Int,
            frameType: Int,
            size_list: MutableList<Size>
        ) {
            val size = format.getJSONArray("size")
            val size_nums = size.length()
            for (j in 0 until size_nums) {
                val sz = size.getString(j).split("x".toRegex()).toTypedArray()
                try {
                    size_list.add(Size(formatType, frameType, j, sz[0].toInt(), sz[1].toInt()))
                } catch (e: Exception) {
                    break
                }
            }
        }

        private external fun nativeRelease(id_camera: Long): Int
        private external fun nativeSetStatusCallback(
            mNativePtr: Long,
            callback: IStatusCallback
        ): Int

        private external fun nativeSetButtonCallback(
            mNativePtr: Long,
            callback: IButtonCallback
        ): Int

        private external fun nativeSetPreviewSize(
            id_camera: Long,
            width: Int,
            height: Int,
            min_fps: Int,
            max_fps: Int,
            mode: Int,
            bandwidth: Float
        ): Int

        private external fun nativeGetSupportedSize(id_camera: Long): String
        private external fun nativeStartPreview(id_camera: Long): Int
        private external fun nativeStopPreview(id_camera: Long): Int
        private external fun nativeSetPreviewDisplay(id_camera: Long, surface: Surface): Int
        private external fun nativeSetFrameCallback(
            mNativePtr: Long,
            callback: IFrameCallback?,
            pixelFormat: Int
        ): Int

        private external fun nativeSetCaptureDisplay(id_camera: Long, surface: Surface?): Int
        private external fun nativeGetCtrlSupports(id_camera: Long): Long
        private external fun nativeGetProcSupports(id_camera: Long): Long
        private external fun nativeSetScanningMode(id_camera: Long, scanning_mode: Int): Int
        private external fun nativeGetScanningMode(id_camera: Long): Int
        private external fun nativeSetExposureMode(id_camera: Long, exposureMode: Int): Int
        private external fun nativeGetExposureMode(id_camera: Long): Int
        private external fun nativeSetExposurePriority(id_camera: Long, priority: Int): Int
        private external fun nativeGetExposurePriority(id_camera: Long): Int
        private external fun nativeSetExposure(id_camera: Long, exposure: Int): Int
        private external fun nativeGetExposure(id_camera: Long): Int
        private external fun nativeSetExposureRel(id_camera: Long, exposure_rel: Int): Int
        private external fun nativeGetExposureRel(id_camera: Long): Int
        private external fun nativeSetAutoFocus(id_camera: Long, autofocus: Boolean): Int
        private external fun nativeGetAutoFocus(id_camera: Long): Int
        private external fun nativeSetFocus(id_camera: Long, focus: Int): Int
        private external fun nativeGetFocus(id_camera: Long): Int
        private external fun nativeSetFocusRel(id_camera: Long, focus_rel: Int): Int
        private external fun nativeGetFocusRel(id_camera: Long): Int
        private external fun nativeSetIris(id_camera: Long, iris: Int): Int
        private external fun nativeGetIris(id_camera: Long): Int
        private external fun nativeSetIrisRel(id_camera: Long, iris_rel: Int): Int
        private external fun nativeGetIrisRel(id_camera: Long): Int
        private external fun nativeSetPan(id_camera: Long, pan: Int): Int
        private external fun nativeGetPan(id_camera: Long): Int
        private external fun nativeSetPanRel(id_camera: Long, pan_rel: Int): Int
        private external fun nativeGetPanRel(id_camera: Long): Int
        private external fun nativeSetTilt(id_camera: Long, tilt: Int): Int
        private external fun nativeGetTilt(id_camera: Long): Int
        private external fun nativeSetTiltRel(id_camera: Long, tilt_rel: Int): Int
        private external fun nativeGetTiltRel(id_camera: Long): Int
        private external fun nativeSetRoll(id_camera: Long, roll: Int): Int
        private external fun nativeGetRoll(id_camera: Long): Int
        private external fun nativeSetRollRel(id_camera: Long, roll_rel: Int): Int
        private external fun nativeGetRollRel(id_camera: Long): Int
        private external fun nativeSetAutoWhiteBlance(
            id_camera: Long,
            autoWhiteBlance: Boolean
        ): Int

        private external fun nativeGetAutoWhiteBlance(id_camera: Long): Int
        private external fun nativeSetAutoWhiteBlanceCompo(
            id_camera: Long,
            autoWhiteBlanceCompo: Boolean
        ): Int

        private external fun nativeGetAutoWhiteBlanceCompo(id_camera: Long): Int
        private external fun nativeSetWhiteBlance(id_camera: Long, whiteBlance: Int): Int
        private external fun nativeGetWhiteBlance(id_camera: Long): Int
        private external fun nativeSetWhiteBlanceCompo(id_camera: Long, whiteBlance_compo: Int): Int
        private external fun nativeGetWhiteBlanceCompo(id_camera: Long): Int
        private external fun nativeSetBacklightComp(id_camera: Long, backlight_comp: Int): Int
        private external fun nativeGetBacklightComp(id_camera: Long): Int
        private external fun nativeSetBrightness(id_camera: Long, brightness: Int): Int
        private external fun nativeGetBrightness(id_camera: Long): Int
        private external fun nativeSetContrast(id_camera: Long, contrast: Int): Int
        private external fun nativeGetContrast(id_camera: Long): Int
        private external fun nativeSetAutoContrast(id_camera: Long, autocontrast: Boolean): Int
        private external fun nativeGetAutoContrast(id_camera: Long): Int
        private external fun nativeSetSharpness(id_camera: Long, sharpness: Int): Int
        private external fun nativeGetSharpness(id_camera: Long): Int
        private external fun nativeSetGain(id_camera: Long, gain: Int): Int
        private external fun nativeGetGain(id_camera: Long): Int
        private external fun nativeSetGamma(id_camera: Long, gamma: Int): Int
        private external fun nativeGetGamma(id_camera: Long): Int
        private external fun nativeSetSaturation(id_camera: Long, saturation: Int): Int
        private external fun nativeGetSaturation(id_camera: Long): Int
        private external fun nativeSetHue(id_camera: Long, hue: Int): Int
        private external fun nativeGetHue(id_camera: Long): Int
        private external fun nativeSetAutoHue(id_camera: Long, autohue: Boolean): Int
        private external fun nativeGetAutoHue(id_camera: Long): Int
        private external fun nativeSetPowerlineFrequency(id_camera: Long, frequency: Int): Int
        private external fun nativeGetPowerlineFrequency(id_camera: Long): Int
        private external fun nativeSetZoom(id_camera: Long, zoom: Int): Int
        private external fun nativeGetZoom(id_camera: Long): Int
        private external fun nativeSetZoomRel(id_camera: Long, zoom_rel: Int): Int
        private external fun nativeGetZoomRel(id_camera: Long): Int
        private external fun nativeSetDigitalMultiplier(id_camera: Long, multiplier: Int): Int
        private external fun nativeGetDigitalMultiplier(id_camera: Long): Int
        private external fun nativeSetDigitalMultiplierLimit(
            id_camera: Long,
            multiplier_limit: Int
        ): Int

        private external fun nativeGetDigitalMultiplierLimit(id_camera: Long): Int
        private external fun nativeSetAnalogVideoStandard(id_camera: Long, standard: Int): Int
        private external fun nativeGetAnalogVideoStandard(id_camera: Long): Int
        private external fun nativeSetAnalogVideoLoackState(id_camera: Long, state: Int): Int
        private external fun nativeGetAnalogVideoLoackState(id_camera: Long): Int
        private external fun nativeSetPrivacy(id_camera: Long, privacy: Boolean): Int
        private external fun nativeGetPrivacy(id_camera: Long): Int

        init {
            if (!isLoaded) {
                System.loadLibrary("jpeg-turbo1500")
                System.loadLibrary("usb100")
                System.loadLibrary("uvc")
                System.loadLibrary("UVCCamera")
                isLoaded = true
            }
        }
    }

    private var mControlBlock: UsbController? = null
    protected var mControlSupports: Long = 0
    protected var mProcSupports: Long = 0
    protected var mCurrentFrameFormat = FRAME_FORMAT_YUYV
    protected var mCurrentWidth = DEFAULT_PREVIEW_WIDTH
    protected var mCurrentHeight = DEFAULT_PREVIEW_HEIGHT
    protected var mCurrentBandwidthFactor = DEFAULT_BANDWIDTH
    protected var mSupportedSize: String?
    protected var mCurrentSizeList: List<Size>? = null

    // these fields from here are accessed from native code and do not change name and remove
    protected var mNativePtr: Long
    protected var mScanningModeMin = 0
    protected var mScanningModeMax = 0
    protected var mScanningModeDef = 0
    protected var mExposureModeMin = 0
    protected var mExposureModeMax = 0
    protected var mExposureModeDef = 0
    protected var mExposurePriorityMin = 0
    protected var mExposurePriorityMax = 0
    protected var mExposurePriorityDef = 0
    protected var mExposureMin = 0
    protected var mExposureMax = 0
    protected var mExposureDef = 0
    protected var mAutoFocusMin = 0
    protected var mAutoFocusMax = 0
    protected var mAutoFocusDef = 0
    protected var mFocusMin = 0
    protected var mFocusMax = 0
    protected var mFocusDef = 0
    protected var mFocusRelMin = 0
    protected var mFocusRelMax = 0
    protected var mFocusRelDef = 0
    protected var mFocusSimpleMin = 0
    protected var mFocusSimpleMax = 0
    protected var mFocusSimpleDef = 0
    protected var mIrisMin = 0
    protected var mIrisMax = 0
    protected var mIrisDef = 0
    protected var mIrisRelMin = 0
    protected var mIrisRelMax = 0
    protected var mIrisRelDef = 0
    protected var mPanMin = 0
    protected var mPanMax = 0
    protected var mPanDef = 0
    protected var mTiltMin = 0
    protected var mTiltMax = 0
    protected var mTiltDef = 0
    protected var mRollMin = 0
    protected var mRollMax = 0
    protected var mRollDef = 0
    protected var mPanRelMin = 0
    protected var mPanRelMax = 0
    protected var mPanRelDef = 0
    protected var mTiltRelMin = 0
    protected var mTiltRelMax = 0
    protected var mTiltRelDef = 0
    protected var mRollRelMin = 0
    protected var mRollRelMax = 0
    protected var mRollRelDef = 0
    protected var mPrivacyMin = 0
    protected var mPrivacyMax = 0
    protected var mPrivacyDef = 0
    protected var mAutoWhiteBlanceMin = 0
    protected var mAutoWhiteBlanceMax = 0
    protected var mAutoWhiteBlanceDef = 0
    protected var mAutoWhiteBlanceCompoMin = 0
    protected var mAutoWhiteBlanceCompoMax = 0
    protected var mAutoWhiteBlanceCompoDef = 0
    protected var mWhiteBlanceMin = 0
    protected var mWhiteBlanceMax = 0
    protected var mWhiteBlanceDef = 0
    protected var mWhiteBlanceCompoMin = 0
    protected var mWhiteBlanceCompoMax = 0
    protected var mWhiteBlanceCompoDef = 0
    protected var mWhiteBlanceRelMin = 0
    protected var mWhiteBlanceRelMax = 0
    protected var mWhiteBlanceRelDef = 0
    protected var mBacklightCompMin = 0
    protected var mBacklightCompMax = 0
    protected var mBacklightCompDef = 0
    protected var mBrightnessMin = 0
    protected var mBrightnessMax = 0
    protected var mBrightnessDef = 0
    protected var mContrastMin = 0
    protected var mContrastMax = 0
    protected var mContrastDef = 0
    protected var mSharpnessMin = 0
    protected var mSharpnessMax = 0
    protected var mSharpnessDef = 0
    protected var mGainMin = 0
    protected var mGainMax = 0
    protected var mGainDef = 0
    protected var mGammaMin = 0
    protected var mGammaMax = 0
    protected var mGammaDef = 0
    protected var mSaturationMin = 0
    protected var mSaturationMax = 0
    protected var mSaturationDef = 0
    protected var mHueMin = 0
    protected var mHueMax = 0
    protected var mHueDef = 0
    protected var mZoomMin = 0
    protected var mZoomMax = 0
    protected var mZoomDef = 0
    protected var mZoomRelMin = 0
    protected var mZoomRelMax = 0
    protected var mZoomRelDef = 0
    protected var mPowerlineFrequencyMin = 0
    protected var mPowerlineFrequencyMax = 0
    protected var mPowerlineFrequencyDef = 0
    protected var mMultiplierMin = 0
    protected var mMultiplierMax = 0
    protected var mMultiplierDef = 0
    protected var mMultiplierLimitMin = 0
    protected var mMultiplierLimitMax = 0
    protected var mMultiplierLimitDef = 0
    protected var mAnalogVideoStandardMin = 0
    protected var mAnalogVideoStandardMax = 0
    protected var mAnalogVideoStandardDef = 0
    protected var mAnalogVideoLockStateMin = 0
    protected var mAnalogVideoLockStateMax = 0
    protected var mAnalogVideoLockStateDef = 0

    /**
     * connect to a UVC camera
     * USB permission is necessary before this method is called
     *
     * @param controlBlock
     */
    @Synchronized
    fun open(controlBlock: UsbController) {
        var result: Int
        try {
            mControlBlock = controlBlock
            result = nativeConnect(
                mNativePtr,
                controlBlock.vendorId,
                controlBlock.productId,
                controlBlock.fileDescriptor,
                controlBlock.busNum,
                controlBlock.devNum,
                controlBlock.uSBFSName
            )
        } catch (e: Exception) {
            e.printStackTrace()
            result = -1
        }
        if (result != 0) {
            throw UnsupportedOperationException("open failed:result=$result")
        }
        if (mNativePtr != 0L && TextUtils.isEmpty(mSupportedSize)) {
            mSupportedSize = nativeGetSupportedSize(mNativePtr)
        }
        nativeSetPreviewSize(
            mNativePtr,
            DEFAULT_PREVIEW_WIDTH,
            DEFAULT_PREVIEW_HEIGHT,
            DEFAULT_PREVIEW_MIN_FPS,
            DEFAULT_PREVIEW_MAX_FPS,
            DEFAULT_PREVIEW_MODE,
            DEFAULT_BANDWIDTH
        )
    }

    /**
     * destroy UVCCamera object
     */
    @Synchronized
    fun destroy() {
        close()
        if (mNativePtr != 0L) {
            nativeDestroy(mNativePtr)
            mNativePtr = 0
        }
    }

    /**
     * close and release UVC camera
     */
    @Synchronized
    fun close() {
        stopPreview()
        if (mNativePtr != 0L) {
            nativeRelease(mNativePtr)
            // mNativePtr = 0;
        }
        if (mControlBlock != null) {
            mControlBlock!!.close()
            mControlBlock = null
        }
        mProcSupports = 0
        mControlSupports = mProcSupports
        mCurrentFrameFormat = -1
        mCurrentBandwidthFactor = 0f
        mSupportedSize = null
        mCurrentSizeList = null
    }

    val previewSize: Size?
        get() {
            var result: Size? = null
            val list = supportedSizeList
            for (sz in list) {
                if (sz.width == mCurrentWidth
                    || sz.height == mCurrentHeight
                ) {
                    result = sz
                    break
                }
            }
            return result
        }

    /**
     * Set preview size and preview mode
     *
     * @param width
     * @param height
     */
    fun setPreviewSize(width: Int, height: Int) {
        setPreviewSize(
            width,
            height,
            DEFAULT_PREVIEW_MIN_FPS,
            DEFAULT_PREVIEW_MAX_FPS,
            mCurrentFrameFormat,
            mCurrentBandwidthFactor
        )
    }

    /**
     * Set preview size and preview mode
     *
     * @param width
     * @param height
     * @param frameFormat either FRAME_FORMAT_YUYV(0) or FRAME_FORMAT_MJPEG(1)
     */
    fun setPreviewSize(width: Int, height: Int, frameFormat: Int) {
        setPreviewSize(
            width,
            height,
            DEFAULT_PREVIEW_MIN_FPS,
            DEFAULT_PREVIEW_MAX_FPS,
            frameFormat,
            mCurrentBandwidthFactor
        )
    }

    /**
     * Set preview size and preview mode
     *
     * @param width
     * @param height
     * @param frameFormat either FRAME_FORMAT_YUYV(0) or FRAME_FORMAT_MJPEG(1)
     * @param bandwidth   [0.0f,1.0f]
     */
    fun setPreviewSize(width: Int, height: Int, frameFormat: Int, bandwidth: Float) {
        setPreviewSize(
            width,
            height,
            DEFAULT_PREVIEW_MIN_FPS,
            DEFAULT_PREVIEW_MAX_FPS,
            frameFormat,
            bandwidth
        )
    }

    /**
     * Set preview size and preview mode
     *
     * @param width
     * @param height
     * @param min_fps
     * @param max_fps
     * @param frameFormat     either FRAME_FORMAT_YUYV(0) or FRAME_FORMAT_MJPEG(1)
     * @param bandwidthFactor
     */
    fun setPreviewSize(
        width: Int,
        height: Int,
        min_fps: Int,
        max_fps: Int,
        frameFormat: Int,
        bandwidthFactor: Float
    ) {
        require(!(width == 0 || height == 0)) { "invalid preview size" }
        if (mNativePtr != 0L) {
            val result = nativeSetPreviewSize(
                mNativePtr,
                width,
                height,
                min_fps,
                max_fps,
                frameFormat,
                bandwidthFactor
            )
            require(result == 0) { "Failed to set preview size" }
            mCurrentFrameFormat = frameFormat
            mCurrentWidth = width
            mCurrentHeight = height
            mCurrentBandwidthFactor = bandwidthFactor
        }
    }

    @get:Synchronized
    val supportedSize: String?
        get() = if (!TextUtils.isEmpty(mSupportedSize)) mSupportedSize else nativeGetSupportedSize(
            mNativePtr
        ).also { mSupportedSize = it }

    //        final int type = (mCurrentFrameFormat > 0) ? 6 : 4;
    val supportedSizeList: List<Size>
        get() =//        final int type = (mCurrentFrameFormat > 0) ? 6 : 4;
            getSupportedSize(-1, mSupportedSize)

    /**
     * set preview surface with SurfaceHolder
     * you can use SurfaceHolder came from SurfaceView/GLSurfaceView
     *
     * @param holder
     */
    @Synchronized
    fun setPreviewDisplay(holder: SurfaceHolder) {
        nativeSetPreviewDisplay(mNativePtr, holder.surface)
    }

    /**
     * set preview surface with Surface
     *
     * @param surface
     */
    @Synchronized
    fun setPreviewDisplay(surface: Surface?) {
        surface?.let { nativeSetPreviewDisplay(mNativePtr, it) }

    }

    /**
     * set preview surface with SurfaceTexture.
     * this method require API >= 14
     *
     * @param texture
     */
    @Synchronized
    fun setPreviewTexture(texture: SurfaceTexture?) {    // API >= 11
        val surface = Surface(texture) // XXX API >= 14
        nativeSetPreviewDisplay(mNativePtr, surface)
    }

    /**
     * start preview
     */
    @Synchronized
    fun startPreview() {
        if (mControlBlock != null) {
            nativeStartPreview(mNativePtr)
        }
    }

    /**
     * stop preview
     */
    @Synchronized
    fun stopPreview() {
        setFrameCallback(null, 0)
        if (mControlBlock != null) {
            nativeStopPreview(mNativePtr)
        }
    }

    /**
     * wrong result may return when you call this just after camera open.it is better to wait several hundreads millseconds.
     *
     * @param flag
     * @return
     */
    fun checkSupportFlag(flag: Long): Boolean {
        updateCameraParams()
        return if (flag and -0x80000000 == -0x80000000L) mProcSupports and flag == flag and 0x7ffffffF else mControlSupports and flag == flag
    }

    @get:Synchronized
    @set:Synchronized
    var autoFocus: Boolean
        get() {
            var result = true
            if (mNativePtr != 0L) {
                result = nativeGetAutoFocus(mNativePtr) > 0
            }
            return result
        }
        set(autoFocus) {
            if (mNativePtr != 0L) {
                nativeSetAutoFocus(mNativePtr, autoFocus)
            }
        }

    @Synchronized
    fun getFocus(focus_abs: Int): Int {
        var result = 0
        if (mNativePtr != 0L) {
            nativeUpdateFocusLimit(mNativePtr)
            val range = Math.abs(mFocusMax - mFocusMin).toFloat()
            if (range > 0) {
                result = ((focus_abs - mFocusMin) * 100f / range).toInt()
            }
        }
        return result
    }

    @get:Synchronized
    @set:Synchronized
    var focus: Int
        get() = getFocus(nativeGetFocus(mNativePtr))
        set(focus) {
            if (mNativePtr != 0L) {
                val range = Math.abs(mFocusMax - mFocusMin).toFloat()
                if (range > 0) nativeSetFocus(
                    mNativePtr,
                    (focus / 100f * range).toInt() + mFocusMin
                )
            }
        }

    @Synchronized
    fun resetFocus() {
        if (mNativePtr != 0L) {
            nativeSetFocus(mNativePtr, mFocusDef)
        }
    }

    @get:Synchronized
    @set:Synchronized
    var autoWhiteBlance: Boolean
        get() {
            var result = true
            if (mNativePtr != 0L) {
                result = nativeGetAutoWhiteBlance(mNativePtr) > 0
            }
            return result
        }
        set(autoWhiteBlance) {
            if (mNativePtr != 0L) {
                nativeSetAutoWhiteBlance(mNativePtr, autoWhiteBlance)
            }
        }

    @Synchronized
    fun getWhiteBlance(whiteBlance_abs: Int): Int {
        var result = 0
        if (mNativePtr != 0L) {
            nativeUpdateWhiteBlanceLimit(mNativePtr)
            val range = Math.abs(mWhiteBlanceMax - mWhiteBlanceMin).toFloat()
            if (range > 0) {
                result = ((whiteBlance_abs - mWhiteBlanceMin) * 100f / range).toInt()
            }
        }
        return result
    }

    @get:Synchronized
    @set:Synchronized
    var whiteBlance: Int
        get() = getFocus(nativeGetWhiteBlance(mNativePtr))
        set(whiteBlance) {
            if (mNativePtr != 0L) {
                val range = Math.abs(mWhiteBlanceMax - mWhiteBlanceMin).toFloat()
                if (range > 0) nativeSetWhiteBlance(
                    mNativePtr,
                    (whiteBlance / 100f * range).toInt() + mWhiteBlanceMin
                )
            }
        }

    @Synchronized
    fun resetWhiteBlance() {
        if (mNativePtr != 0L) {
            nativeSetWhiteBlance(mNativePtr, mWhiteBlanceDef)
        }
    }

    @Synchronized
    fun getBrightness(brightness_abs: Int): Int {
        var result = 0
        if (mNativePtr != 0L) {
            nativeUpdateBrightnessLimit(mNativePtr)
            val range = Math.abs(mBrightnessMax - mBrightnessMin).toFloat()
            if (range > 0) {
                result = ((brightness_abs - mBrightnessMin) * 100f / range).toInt()
            }
        }
        return result
    }

    @get:Synchronized
    @set:Synchronized
    var brightness: Int
        get() = getBrightness(nativeGetBrightness(mNativePtr))
        set(brightness) {
            if (mNativePtr != 0L) {
                val range = Math.abs(mBrightnessMax - mBrightnessMin).toFloat()
                if (range > 0) nativeSetBrightness(
                    mNativePtr,
                    (brightness / 100f * range).toInt() + mBrightnessMin
                )
            }
        }

    @Synchronized
    fun resetBrightness() {
        if (mNativePtr != 0L) {
            nativeSetBrightness(mNativePtr, mBrightnessDef)
        }
    }

    @Synchronized
    fun getContrast(contrast_abs: Int): Int {
        var result = 0
        if (mNativePtr != 0L) {
            val range = Math.abs(mContrastMax - mContrastMin).toFloat()
            if (range > 0) {
                result = ((contrast_abs - mContrastMin) * 100f / range).toInt()
            }
        }
        return result
    }

    @get:Synchronized
    @set:Synchronized
    var contrast: Int
        get() = getContrast(nativeGetContrast(mNativePtr))
        set(contrast) {
            if (mNativePtr != 0L) {
                nativeUpdateContrastLimit(mNativePtr)
                val range = Math.abs(mContrastMax - mContrastMin).toFloat()
                if (range > 0) nativeSetContrast(
                    mNativePtr,
                    (contrast / 100f * range).toInt() + mContrastMin
                )
            }
        }

    @Synchronized
    fun resetContrast() {
        if (mNativePtr != 0L) {
            nativeSetContrast(mNativePtr, mContrastDef)
        }
    }

    @Synchronized
    fun getSharpness(sharpness_abs: Int): Int {
        var result = 0
        if (mNativePtr != 0L) {
            nativeUpdateSharpnessLimit(mNativePtr)
            val range = Math.abs(mSharpnessMax - mSharpnessMin).toFloat()
            if (range > 0) {
                result = ((sharpness_abs - mSharpnessMin) * 100f / range).toInt()
            }
        }
        return result
    }

    @get:Synchronized
    @set:Synchronized
    var sharpness: Int
        get() = getSharpness(nativeGetSharpness(mNativePtr))
        set(sharpness) {
            if (mNativePtr != 0L) {
                val range = Math.abs(mSharpnessMax - mSharpnessMin).toFloat()
                if (range > 0) nativeSetSharpness(
                    mNativePtr,
                    (sharpness / 100f * range).toInt() + mSharpnessMin
                )
            }
        }

    @Synchronized
    fun resetSharpness() {
        if (mNativePtr != 0L) {
            nativeSetSharpness(mNativePtr, mSharpnessDef)
        }
    }

    @Synchronized
    fun getGain(gain_abs: Int): Int {
        var result = 0
        if (mNativePtr != 0L) {
            nativeUpdateGainLimit(mNativePtr)
            val range = Math.abs(mGainMax - mGainMin).toFloat()
            if (range > 0) {
                result = ((gain_abs - mGainMin) * 100f / range).toInt()
            }
        }
        return result
    }

    /*final float range = Math.abs(mGainMax - mGainMin);
            if (range > 0)
                nativeSetGain(mNativePtr, (int) (gain / 100.f * range) + mGainMin);*/
    @get:Synchronized
    @set:Synchronized
    var gain: Int
        get() = getGain(nativeGetGain(mNativePtr))
        set(gain) {
            if (mNativePtr != 0L) {
                /*final float range = Math.abs(mGainMax - mGainMin);
            if (range > 0)
                nativeSetGain(mNativePtr, (int) (gain / 100.f * range) + mGainMin);*/
                nativeSetGain(mNativePtr, gain)
            }
        }

    @Synchronized
    fun resetGain() {
        if (mNativePtr != 0L) {
            nativeSetGain(mNativePtr, mGainDef)
        }
    }

    @Synchronized
    fun getGamma(gamma_abs: Int): Int {
        var result = 0
        if (mNativePtr != 0L) {
            nativeUpdateGammaLimit(mNativePtr)
            val range = Math.abs(mGammaMax - mGammaMin).toFloat()
            if (range > 0) {
                result = ((gamma_abs - mGammaMin) * 100f / range).toInt()
            }
        }
        return result
    }

    @get:Synchronized
    @set:Synchronized
    var gamma: Int
        get() = getGamma(nativeGetGamma(mNativePtr))
        set(gamma) {
            if (mNativePtr != 0L) {
                val range = Math.abs(mGammaMax - mGammaMin).toFloat()
                if (range > 0) nativeSetGamma(
                    mNativePtr,
                    (gamma / 100f * range).toInt() + mGammaMin
                )
            }
        }

    @Synchronized
    fun resetGamma() {
        if (mNativePtr != 0L) {
            nativeSetGamma(mNativePtr, mGammaDef)
        }
    }

    @Synchronized
    fun getSaturation(saturation_abs: Int): Int {
        var result = 0
        if (mNativePtr != 0L) {
            nativeUpdateSaturationLimit(mNativePtr)
            val range = Math.abs(mSaturationMax - mSaturationMin).toFloat()
            if (range > 0) {
                result = ((saturation_abs - mSaturationMin) * 100f / range).toInt()
            }
        }
        return result
    }

    @get:Synchronized
    @set:Synchronized
    var saturation: Int
        get() = getSaturation(nativeGetSaturation(mNativePtr))
        set(saturation) {
            if (mNativePtr != 0L) {
                val range = Math.abs(mSaturationMax - mSaturationMin).toFloat()
                if (range > 0) nativeSetSaturation(
                    mNativePtr,
                    (saturation / 100f * range).toInt() + mSaturationMin
                )
            }
        }

    @Synchronized
    fun resetSaturation() {
        if (mNativePtr != 0L) {
            nativeSetSaturation(mNativePtr, mSaturationDef)
        }
    }

    @Synchronized
    fun getHue(hue_abs: Int): Int {
        var result = 0
        if (mNativePtr != 0L) {
            nativeUpdateHueLimit(mNativePtr)
            val range = Math.abs(mHueMax - mHueMin).toFloat()
            if (range > 0) {
                result = ((hue_abs - mHueMin) * 100f / range).toInt()
            }
        }
        return result
    }

    @get:Synchronized
    @set:Synchronized
    var hue: Int
        get() = getHue(nativeGetHue(mNativePtr))
        set(hue) {
            if (mNativePtr != 0L) {
                val range = Math.abs(mHueMax - mHueMin).toFloat()
                if (range > 0) nativeSetHue(mNativePtr, (hue / 100f * range).toInt() + mHueMin)
            }
        }

    @Synchronized
    fun resetHue() {
        if (mNativePtr != 0L) {
            nativeSetHue(mNativePtr, mSaturationDef)
        }
    }

    var powerlineFrequency: Int
        get() = nativeGetPowerlineFrequency(mNativePtr)
        set(frequency) {
            if (mNativePtr != 0L) nativeSetPowerlineFrequency(mNativePtr, frequency)
        }

    @Synchronized
    fun getZoom(zoom_abs: Int): Int {
        var result = 0
        if (mNativePtr != 0L) {
            nativeUpdateZoomLimit(mNativePtr)
            val range = Math.abs(mZoomMax - mZoomMin).toFloat()
            if (range > 0) {
                result = ((zoom_abs - mZoomMin) * 100f / range).toInt()
            }
        }
        return result
    }// Log.d(TAG, "setZoom:zoom=" + zoom + " ,value=" + z);

    /**
     * this may not work well with some combination of camera and device
     *
     * @param zoom [%]
     */
    @get:Synchronized
    @set:Synchronized
    var zoom: Int
        get() = getZoom(nativeGetZoom(mNativePtr))
        set(zoom) {
            if (mNativePtr != 0L) {
                val range = Math.abs(mZoomMax - mZoomMin).toFloat()
                if (range > 0) {
                    val z = (zoom / 100f * range).toInt() + mZoomMin
                    // Log.d(TAG, "setZoom:zoom=" + zoom + " ,value=" + z);
                    nativeSetZoom(mNativePtr, z)
                }
            }
        }

    @Synchronized
    fun resetZoom() {
        if (mNativePtr != 0L) {
            nativeSetZoom(mNativePtr, mZoomDef)
        }
    }

    /**
     * start movie capturing(this should call while previewing)
     *
     * @param surface
     */
    fun startCapture(surface: Surface?) {
        if (mControlBlock != null && surface != null) {
            nativeSetCaptureDisplay(mNativePtr, surface)
        } else throw NullPointerException("startCapture")
    }

    /**
     * stop movie capturing
     */
    fun stopCapture() {
        if (mControlBlock != null) {
            nativeSetCaptureDisplay(mNativePtr, null)
        }
    }

    /**
     * set status callback
     *
     * @param callback
     */
    fun setStatusCallback(callback: IStatusCallback) {
        if (mNativePtr != 0L) {
            nativeSetStatusCallback(mNativePtr, callback)
        }
    }

    /**
     * set button callback
     *
     * @param callback
     */
    fun setButtonCallback(callback: IButtonCallback?) {
        if (mNativePtr != 0L) {
            callback?.let { nativeSetButtonCallback(mNativePtr, it) }
        }
    }

    /**
     * set frame callback
     *
     * @param callback
     * @param pixelFormat
     */
    fun setFrameCallback(callback: IFrameCallback?, pixelFormat: Int) {
        if (mNativePtr != 0L) {
            nativeSetFrameCallback(mNativePtr, callback, pixelFormat)
        }
    }

    @Synchronized
    fun updateCameraParams() {
        if (mNativePtr != 0L) {
            if (mControlSupports == 0L || mProcSupports == 0L) {
                if (mControlSupports == 0L) mControlSupports = nativeGetCtrlSupports(mNativePtr)
                if (mProcSupports == 0L) mProcSupports = nativeGetProcSupports(mNativePtr)
                if (mControlSupports != 0L && mProcSupports != 0L) {
                    nativeUpdateBrightnessLimit(mNativePtr)
                    nativeUpdateContrastLimit(mNativePtr)
                    nativeUpdateSharpnessLimit(mNativePtr)
                    nativeUpdateGainLimit(mNativePtr)
                    nativeUpdateGammaLimit(mNativePtr)
                    nativeUpdateSaturationLimit(mNativePtr)
                    nativeUpdateHueLimit(mNativePtr)
                    nativeUpdateZoomLimit(mNativePtr)
                    nativeUpdateWhiteBlanceLimit(mNativePtr)
                    nativeUpdateFocusLimit(mNativePtr)
                }
            }
        } else {
            mProcSupports = 0
            mControlSupports = mProcSupports
        }
    }

    // #nativeCreate and #nativeDestroy are not static methods.
    private external fun nativeCreate(): Long
    private external fun nativeDestroy(id_camera: Long)
    private external fun nativeConnect(
        id_camera: Long,
        venderId: Int,
        productId: Int,
        fileDescriptor: Int,
        busNum: Int,
        devAddr: Int,
        usbfs: String?
    ): Int

    private external fun nativeUpdateScanningModeLimit(id_camera: Long): Int
    private external fun nativeUpdateExposureModeLimit(id_camera: Long): Int
    private external fun nativeUpdateExposurePriorityLimit(id_camera: Long): Int
    private external fun nativeUpdateExposureLimit(id_camera: Long): Int
    private external fun nativeUpdateExposureRelLimit(id_camera: Long): Int
    private external fun nativeUpdateAutoFocusLimit(id_camera: Long): Int
    private external fun nativeUpdateFocusLimit(id_camera: Long): Int
    private external fun nativeUpdateFocusRelLimit(id_camera: Long): Int
    private external fun nativeUpdateIrisLimit(id_camera: Long): Int
    private external fun nativeUpdateIrisRelLimit(id_camera: Long): Int
    private external fun nativeUpdatePanLimit(id_camera: Long): Int
    private external fun nativeUpdatePanRelLimit(id_camera: Long): Int
    private external fun nativeUpdateTiltLimit(id_camera: Long): Int
    private external fun nativeUpdateTiltRelLimit(id_camera: Long): Int
    private external fun nativeUpdateRollLimit(id_camera: Long): Int
    private external fun nativeUpdateRollRelLimit(id_camera: Long): Int
    private external fun nativeUpdateAutoWhiteBlanceLimit(id_camera: Long): Int
    private external fun nativeUpdateAutoWhiteBlanceCompoLimit(id_camera: Long): Int
    private external fun nativeUpdateWhiteBlanceLimit(id_camera: Long): Int
    private external fun nativeUpdateWhiteBlanceCompoLimit(id_camera: Long): Int
    private external fun nativeUpdateBacklightCompLimit(id_camera: Long): Int
    private external fun nativeUpdateBrightnessLimit(id_camera: Long): Int
    private external fun nativeUpdateContrastLimit(id_camera: Long): Int
    private external fun nativeUpdateAutoContrastLimit(id_camera: Long): Int
    private external fun nativeUpdateSharpnessLimit(id_camera: Long): Int
    private external fun nativeUpdateGainLimit(id_camera: Long): Int
    private external fun nativeUpdateGammaLimit(id_camera: Long): Int
    private external fun nativeUpdateSaturationLimit(id_camera: Long): Int
    private external fun nativeUpdateHueLimit(id_camera: Long): Int
    private external fun nativeUpdateAutoHueLimit(id_camera: Long): Int
    private external fun nativeUpdatePowerlineFrequencyLimit(id_camera: Long): Int
    private external fun nativeUpdateZoomLimit(id_camera: Long): Int
    private external fun nativeUpdateZoomRelLimit(id_camera: Long): Int
    private external fun nativeUpdateDigitalMultiplierLimit(id_camera: Long): Int
    private external fun nativeUpdateDigitalMultiplierLimitLimit(id_camera: Long): Int
    private external fun nativeUpdateAnalogVideoStandardLimit(id_camera: Long): Int
    private external fun nativeUpdateAnalogVideoLockStateLimit(id_camera: Long): Int
    private external fun nativeUpdatePrivacyLimit(id_camera: Long): Int
    // until here
    /**
     * the sonctructor of this class should be call within the thread that has a looper
     * (UI thread or a thread that called Looper.prepare)
     */
    init {
        mNativePtr = nativeCreate()
        mSupportedSize = null
    }
}