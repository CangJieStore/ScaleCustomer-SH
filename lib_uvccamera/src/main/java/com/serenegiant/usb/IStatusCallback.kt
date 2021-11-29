package com.serenegiant.usb

import java.nio.ByteBuffer

interface IStatusCallback {
    fun onStatus(
        statusClass: Int,
        event: Int,
        selector: Int,
        statusAttribute: Int,
        data: ByteBuffer?
    )
}