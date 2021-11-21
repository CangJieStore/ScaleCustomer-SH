package com.cangjie.scalage.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.blankj.utilcode.util.ViewUtils.runOnUiThread
import com.cangjie.scalage.core.db.CangJie
import com.cangjie.scalage.entity.MessageEvent
import com.cangjie.scalage.kit.lib.ToastUtils
import com.cangjie.scalage.scale.ScaleModule
import com.cangjie.scalage.scale.SerialPortUtilForScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.system.exitProcess

/**
 * @author CangJie
 * @date 2021/9/10 10:53
 */
class InitService : Service(), CoroutineScope by MainScope() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        if (event.code == 501) {
            returnZero()
        }
    }

    private fun returnZero() {
        try {
            ScaleModule.Instance(this).ZeroClear()
        } catch (e: Exception) {
            runOnUiThread {
                ToastUtils.show("置零失败")
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        if (CangJie.getString("token", "").isNotEmpty()) {
            SerialPortUtilForScale.Instance().CloseSerialPort()
            exitProcess(0)
        }
    }
}