package com.lgh.uvccamera.callback

interface PictureCallback {
    /**
     * 拍照成功图片回调
     *
     * @param path 图片保存路径
     */
    fun onPictureTaken(path: String?)
}