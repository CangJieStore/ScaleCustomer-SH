package com.cangjie.scalage.kit

import android.app.Activity
import android.app.Application
import android.os.Bundle

internal class ToastLifecycle(private var mToast: CJToast?, private var mActivity: Activity?) : Application.ActivityLifecycleCallbacks {

    /**
     * 注册监听
     */
    fun register() {
        mActivity!!.application.registerActivityLifecycleCallbacks(this)
    }

    /**
     * 取消监听
     */
    fun unregister() {
        mActivity!!.application.unregisterActivityLifecycleCallbacks(this)
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {
        // 一定要在 onPaused 方法中销毁掉，如果在 onDestroyed 方法中还是会导致内存泄露
        if (mActivity != null && mToast != null && mActivity === activity && mToast!!.isShow && mActivity!!.isFinishing) {
            mToast!!.cancel()
        }
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (mActivity === activity) {
            mActivity = null
            if (mToast != null) {
                mToast!!.recycle()
                mToast = null
            }
        }
    }

}