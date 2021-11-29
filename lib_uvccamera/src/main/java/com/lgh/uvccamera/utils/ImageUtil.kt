package com.lgh.uvccamera.utils

import android.graphics.*
import java.io.ByteArrayOutputStream
import java.lang.Exception

object ImageUtil {
    /**
     * yuv数据转bitmap
     *
     * @param yuv
     * @param width
     * @param height
     * @return
     */
    fun yuv2Bitmap(yuv: ByteArray?, width: Int, height: Int): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            yuv?.let {
                val bos = ByteArrayOutputStream(it.size)
                val yuvImage = YuvImage(it, ImageFormat.NV21, width, height, null)
                val success = yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, bos)
                if (success) {
                    val buffer = bos.toByteArray()
                    bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.size)
                }
                bos.flush()
                bos.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    /**
     * yuv数据转bitmap
     *
     * @param yuv
     * @param width
     * @param height
     * @param rotation
     * @return
     */
    fun yuv2Bitmap(yuv: ByteArray?, width: Int, height: Int, rotation: Float): Bitmap? {
        val bitmap = yuv2Bitmap(yuv, width, height)
        return rotateBimap(bitmap, rotation)
    }

    /**
     * 旋转图片
     *
     * @param bitmap
     * @param rotation 旋转角度
     * @return
     */
    fun rotateBimap(bitmap: Bitmap?, rotation: Float): Bitmap? {
        var bitmap = bitmap
        if (bitmap == null || rotation == 0f) {
            return bitmap
        }
        val matrix = Matrix()
        matrix.postRotate(rotation)
        val rotateBimap =
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (bitmap != null && !bitmap.isRecycled) {
            bitmap.recycle()
            bitmap = null
        }
        return rotateBimap
    }

    /**
     * Bitmap缩放
     *
     * @param bitmap
     * @param newWidth
     * @param newHeight
     * @return
     */
    fun scaleBitmap(bitmap: Bitmap?, newWidth: Int, newHeight: Int): Bitmap? {
        var bitmap = bitmap
        if (bitmap == null) {
            return null
        }
        val width = bitmap.width
        val height = bitmap.height
        val scaleWidth = newWidth.toFloat() / width.toFloat()
        val scaleHeight = newHeight.toFloat() / height.toFloat()
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        var scaleBitmap: Bitmap? = null
        try {
            scaleBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
        } catch (e: OutOfMemoryError) {
            while (scaleBitmap == null) {
                System.gc()
                System.runFinalization()
                scaleBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
            }
        }
        if (bitmap != scaleBitmap && !bitmap.isRecycled) {
            bitmap.recycle()
            bitmap = null
        }
        return scaleBitmap
    }
}