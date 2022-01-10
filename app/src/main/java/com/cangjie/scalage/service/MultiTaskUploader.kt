package com.cangjie.scalage.service

import androidx.lifecycle.MutableLiveData
import com.cangjie.scalage.base.http.Url
import com.cangjie.scalage.core.db.CangJie
import com.cangjie.scalage.entity.UploadTask
import com.rxjava.rxlife.RxLife
import kotlinx.coroutines.delay
import rxhttp.RxHttp
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author: guruohan
 * @date: 2021/11/20
 */
object MultiTaskUploader {
    const val IDLE = 0             //未开始，闲置状态
    const val WAITING = 1          //等待中状态
    const val UPLODING = 2      //下载中
    const val PAUSED = 3           //已暂停
    const val COMPLETED = 4        //已完成
    const val FAIL = 5             //下载失败
    const val CANCEL = 6           //取消状态，等待时被取消
    const val ERROR = 7

    private const val MAX_TASK_COUNT = 1   //最大并发数

    val allLiveTask = MutableLiveData<ArrayList<UploadTask>>() //所有下载任务
    private val waitTask = LinkedList<UploadTask>() //等待下载的任务
    private val uploadTask = LinkedList<UploadTask>() //下载中的任务
    private var taskCallback: UploadStatusCallback? = null

    fun addTasks(tasks: ArrayList<UploadTask>) {
        val allTaskList = getAllTask()
        tasks.forEach {
            allTaskList.add(it)
        }
        allLiveTask.value = allTaskList
    }

    fun startAllUploadTask() {
        val allTaskList = getAllTask()
        allTaskList.forEach {
            if (it.state != COMPLETED && it.state != UPLODING) {
                upload(it)
            }
        }
    }

    private fun upload(task: UploadTask) {
        if (uploadTask.size >= MAX_TASK_COUNT) {
            task.state = WAITING
            waitTask.offer(task)
            return
        }
        val file = File(task.batchPath)
        if (file.exists()) {
            val disposable = RxHttp.postForm(CangJie.getString("upload"))
                .add("access_token", CangJie.getString("token"))
                .add("id", task.goodsId)
                .add("batch", task.batchId.toInt() - 1)
                .addFile("file", file)
                .upload {
                    task.progress = it.progress
                    updateTask()
                }.asString()
                .doFinally {
                    updateTask()
                    uploadTask.remove(task)
                    waitTask.poll()?.let { upload(it) }
                }
                .subscribe({
                    task.state = COMPLETED
                    taskCallback?.upload(COMPLETED, waitTask.size, getAllTask().size, task)
                }, {
                    task.state = FAIL
                    taskCallback?.upload(FAIL, waitTask.size, getAllTask().size, task)
                })
            task.state = UPLODING
            task.disposable = disposable
            uploadTask.add(task)
        } else {
            updateTask()
            uploadTask.remove(task)
            waitTask.poll()?.let { upload(it) }
            taskCallback?.upload(ERROR, waitTask.size, getAllTask().size, task)
        }

    }

    interface UploadStatusCallback {
        fun upload(status: Int, waitSize: Int, totalSize: Int, item: UploadTask)
    }

    fun setUploadTaskCallback(cb: UploadStatusCallback) {
        this.taskCallback = cb
    }

    fun cancelAllTask() {
        var iterator = waitTask.iterator()
        while (iterator.hasNext()) {
            val task = iterator.next()
            task.state = CANCEL
            iterator.remove()
        }

        iterator = uploadTask.iterator()
        while (iterator.hasNext()) {
            val task = iterator.next()
            iterator.remove()
            val disposable = task.disposable
            RxLife.dispose(disposable)
            task.state = CANCEL
        }
        updateTask()
    }

    private fun updateTask() {
        val allTask = getAllTask()
        allLiveTask.postValue(allTask)
    }

    fun haveTaskExecuting(): Boolean {
        return waitTask.size > 0 || uploadTask.size > 0
    }

    fun getWaitTask(): String {
        return (getAllTask().size - waitTask.size).toString() + "/" + getAllTask().size.toString()
    }

    private fun getAllTask(): ArrayList<UploadTask> {
        return allLiveTask.value ?: ArrayList()
    }
}