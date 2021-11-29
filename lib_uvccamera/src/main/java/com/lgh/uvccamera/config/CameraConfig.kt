package com.lgh.uvccamera.config

import com.lgh.uvccamera.bean.PicturePath
import com.lgh.uvccamera.config.CameraConfig
import com.lgh.uvccamera.utils.LogUtil


class CameraConfig {
    var picturePath = PicturePath.APPCACHE // 图片保存路径
    var dirName = "uvccamera" // 图片保存目录名称
    var vendorId = 0 // 需要根据供应商id过滤则设置，不需要过滤设为0
    var productId = 0 // 需要根据产品id过滤则设置，不需要过滤设为0

    fun isDebug(debug: Boolean): CameraConfig {
        LogUtil.allowD = debug
        LogUtil.allowE = debug
        LogUtil.allowI = debug
        LogUtil.allowV = debug
        LogUtil.allowW = debug
        LogUtil.allowWtf = debug
        return this
    }

    fun setPicturePath(mPicturePath: PicturePath): CameraConfig {
        picturePath = mPicturePath
        return this
    }

    fun setDirName(mDirName: String): CameraConfig {
        dirName = mDirName
        return this
    }

    fun setVendorId(mVendorId: Int): CameraConfig {
        vendorId = mVendorId
        return this
    }

    fun setProductId(mProductId: Int): CameraConfig {
        productId = mProductId
        return this
    }

    override fun toString(): String {
        return "CameraConfig{" +
                "mPicturePath=" + picturePath +
                ", mDirName='" + dirName + '\'' +
                ", mVendorId=" + vendorId +
                ", mProductId=" + productId +
                '}'
    }
}