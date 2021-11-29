package com.lgh.uvccamera.callback

interface PreviewCallback {
    /**
     * 预览流回调
     *
     * @param yuv yuv格式的数据流
     */
    fun onPreviewFrame(yuv: ByteArray?)
}