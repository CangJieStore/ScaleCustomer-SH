package com.cangjie.scalage.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import com.blankj.utilcode.util.ViewUtils.runOnUiThread
import com.cangjie.scalage.base.ScaleApplication
import com.cangjie.scalage.base.http.Url
import com.cangjie.scalage.core.db.CangJie
import com.cangjie.scalage.db.AppDatabase
import com.cangjie.scalage.db.SubmitOrder
import com.cangjie.scalage.db.SubmitOrderDao
import com.cangjie.scalage.entity.MessageEvent
import com.cangjie.scalage.kit.lib.ToastUtils
import com.cangjie.scalage.kit.show
import com.cangjie.scalage.scale.ScaleModule
import com.cangjie.scalage.scale.SerialPortUtilForScale
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rxhttp.RxHttp
import rxhttp.awaitResult
import rxhttp.toStr
import java.io.File
import java.util.*
import kotlin.concurrent.fixedRateTimer

/**
 * @author CangJie
 * @date 2021/9/10 10:53
 */
class InitService : Service(), CoroutineScope by MainScope() {

    lateinit var timer: Timer
    private val corLife = CoroutineCycle()
    private var booksDao: SubmitOrderDao? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
//        SerialPortUtilForScale.Instance().OpenSerialPort() //打开称重串口
//        try {
//            ScaleModule.Instance(this) //初始化称重模块
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//            runOnUiThread {
//                ToastUtils.show("初始化称重主板错误！")
//            }
//        }
        timer = fixedRateTimer("", false, 0, 60000) {
            val single: Single<MutableList<SubmitOrder>> = Single.create { emitter ->
                booksDao = AppDatabase.get(ScaleApplication.instance!!).orderDao()
                val orders = booksDao!!.getUpload()
                emitter.onSuccess(orders)
            }
            single.subscribe(object : SingleObserver<MutableList<SubmitOrder>> {
                override fun onSuccess(o: MutableList<SubmitOrder>) {
                    Log.e("orders", Gson().toJson(o))
                    for (item in o) {
                        upload(item)
                    }
                }

                override fun onSubscribe(d: Disposable) {

                }

                override fun onError(e: Throwable) {
                }
            })
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

    private fun upload(order: SubmitOrder) {
        corLife.launch({
            RxHttp.postForm(Url.upload)
                .add("access_token", CangJie.getString("token"))
                .add("id", order.goodsId)
                .add("batch", order.batchId)
                .addFile("file", File(order.batchPath))
                .toStr()
                .awaitResult()
            val file = File(order.batchPath)
            contentResolver.delete(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Images.Media.DATA + "=?",
                arrayOf(order.batchPath)
            )
            file.delete()
            order.isUpload = 2
            booksDao!!.update(order)
        }, {

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
//        SerialPortUtilForScale.Instance().CloseSerialPort()
        corLife.close()
        timer.cancel()
    }
}