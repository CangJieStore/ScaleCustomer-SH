package com.lgh.uvccamera.utils

import android.content.Context
import android.os.Environment
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

object FileUtil {
    /**
     * 判断当前系统中是否存在外部存储器（一般为SD卡）
     *
     * @return 当前系统中是否存在外部存储器
     */
    fun hasExternalStorage(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * 获取外部存储器（一般为SD卡）的路径
     *
     * @return 外部存储器的绝对路径
     */
    val externalStoragePath: String
        get() = Environment.getExternalStorageDirectory().absolutePath

    /**
     * 获取SD卡目录
     *
     * @param foderName
     * @param fileName
     * @return
     */
    @JvmStatic
    fun getSDCardDir(foderName: String): File? {
        return if (!hasExternalStorage()) {
            null
        } else File(externalStoragePath + File.separator + foderName)
    }

    /**
     * 获取SD卡文件
     *
     * @param foderName
     * @param fileName
     * @return
     */
    @JvmStatic
    fun getSDCardFile(foderName: String, fileName: String?): File? {
        val foder = getSDCardDir(foderName) ?: return null
        if (!foder.exists()) {
            if (!foder.mkdirs()) {
                return null
            }
        }
        return File(foder, fileName)
    }

    /**
     * 获取缓存目录
     *
     * @param context
     * @param dirName
     * @return
     */
    @JvmStatic
    fun getDiskCacheDir(context: Context, dirName: String): String {
        val cachePath: String
        cachePath =
            if ((Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable())
                && context.externalCacheDir != null
            ) {
                context.externalCacheDir!!.path
            } else {
                context.cacheDir.path
            }
        return cachePath + File.separator + dirName
    }

    /**
     * 获取缓存目录文件
     *
     * @param context
     * @param dirName
     * @param fileName
     * @return
     */
    @JvmStatic
    fun getCacheFile(context: Context, dirName: String, fileName: String): File? {
        val dirFile = File(getDiskCacheDir(context, dirName))
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                LogUtil.d("failed to create directory")
                return null
            }
        }
        return File(dirFile.path + File.separator + fileName)
    }

    /**
     * 删除文件或文件夹
     *
     * @param dirFile
     * @return
     */
    @JvmStatic
    fun deleteFile(dirFile: File): Boolean {
        if (!dirFile.exists()) {
            return false
        }
        if (dirFile.isFile) {
            return dirFile.delete()
        } else {
            for (file in dirFile.listFiles()) {
                deleteFile(file)
            }
        }
        return dirFile.delete()
    }

    /**
     * 将yuv格式byte数组转化成jpeg图片并保存
     *
     * @param file
     * @param yuv
     * @param width
     * @param height
     */
    fun saveYuv2Jpeg(file: File?, yuv: ByteArray?, width: Int, height: Int): String? {
        return saveBitmap(file, ImageUtil.yuv2Bitmap(yuv, width, height))
    }

    /**
     * 将yuv格式byte数组转化成jpeg图片并保存
     *
     * @param file
     * @param yuv
     * @param width
     * @param height
     * @param rotation
     */
    @JvmStatic
    fun saveYuv2Jpeg(
        file: File?,
        yuv: ByteArray?,
        width: Int,
        height: Int,
        rotation: Float
    ): String? {
        return saveBitmap(file, ImageUtil.yuv2Bitmap(yuv, width, height, rotation))
    }

    /**
     * 保存bitmap
     *
     * @param file
     * @param bitmap
     */
    fun saveBitmap(file: File?, bitmap: Bitmap?): String? {
        var bitmap = bitmap
        if (file == null || bitmap == null) {
            return null
        }
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
                bitmap = null
            }
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file.absolutePath
    }
}