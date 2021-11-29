package com.serenegiant.usb

interface IButtonCallback {
    fun onButton(button: Int, state: Int)
}