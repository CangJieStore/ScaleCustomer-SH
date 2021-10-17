package com.cangjie.scalage.kit

import java.lang.ref.WeakReference

internal class ToastDismissRunnable(toast: CJToast?) : WeakReference<CJToast?>(toast), Runnable {
    override fun run() {
        val toast = get()
        if (toast != null && toast.isShow) {
            toast.cancel()
        }
    }
}