package com.cangjie.scalage.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import com.cangjie.scalage.base.ScaleApplication
import com.cangjie.scalage.db.AppDatabase
import com.cangjie.scalage.db.SubmitOrder
import com.cangjie.scalage.db.SubmitOrderDao
import com.cangjie.scalage.entity.UploadEvent
import com.cangjie.scalage.entity.UploadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.greenrobot.eventbus.EventBus
import java.io.File

/**
 * @author: guruohan
 * @date: 2021/11/21
 */
class UploadImgService : Service(), CoroutineScope by MainScope() {

    private var booksDao: SubmitOrderDao? = null
    private val corLife = CoroutineCycle()
    private var orders = arrayListOf<UploadTask>()


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        booksDao = AppDatabase.get(ScaleApplication.instance!!).orderDao()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        orders = intent?.getSerializableExtra("orders") as ArrayList<UploadTask>
        MultiTaskUploader.addTasks(orders)
        EventBus.getDefault().post(UploadEvent(222, orders.size, orders.size))
        MultiTaskUploader.setUploadTaskCallback(object :
            MultiTaskUploader.UploadStatusCallback {
            override fun upload(
                status: Int,
                waitSize: Int,
                totalSize: Int,
                item: UploadTask
            ) {
                EventBus.getDefault().post(UploadEvent(222, waitSize, totalSize))
                if (status == MultiTaskUploader.COMPLETED) {
                    val submitOrder =
                        SubmitOrder(item.id, item.goodsId, item.batchId, item.batchPath, 2)
                    val file = File(item.batchPath)
                    contentResolver.delete(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Images.Media.DATA + "=?",
                        arrayOf(item.batchPath)
                    )
                    file.delete()
                    corLife.launch {
                        booksDao!!.update(submitOrder)
                    }
                } else if (status == MultiTaskUploader.ERROR) {
                    val submitOrder =
                        SubmitOrder(item.id, item.goodsId, item.batchId, item.batchPath, 2)
                    corLife.launch {
                        booksDao!!.update(submitOrder)
                    }
                }
                if (waitSize == 0) {
                    stopSelf()
                }
            }
        })
        MultiTaskUploader.startAllUploadTask()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        corLife.close()
        booksDao?.let { booksDao = null }
    }
}