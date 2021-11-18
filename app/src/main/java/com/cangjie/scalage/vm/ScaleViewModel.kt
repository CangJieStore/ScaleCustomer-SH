package com.cangjie.scalage.vm

import android.provider.MediaStore
import android.util.Log
import androidx.collection.arrayMapOf
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cangjie.scalage.base.ScaleApplication
import com.cangjie.scalage.base.http.Url
import com.cangjie.scalage.base.http.errorMsg
import com.cangjie.scalage.core.binding.BindingAction
import com.cangjie.scalage.core.binding.BindingCommand
import com.cangjie.scalage.core.db.CangJie
import com.cangjie.scalage.core.event.MsgEvent
import com.cangjie.scalage.db.AppDatabase
import com.cangjie.scalage.entity.LoginInfo
import com.cangjie.scalage.entity.OrderInfo
import com.cangjie.scalage.db.SubmitOrder
import com.cangjie.scalage.db.SubmitRepository
import com.cangjie.scalage.entity.Update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import rxhttp.RxHttp
import rxhttp.awaitResult
import rxhttp.toFlow
import rxhttp.toStr
import java.io.File

/**
 * @author: guruohan
 * @date: 2021/9/9
 */
class ScaleViewModel : BaseScaleViewModel() {
    var usernameFiled = ObservableField<String>()
    var passwordFiled = ObservableField<String>()
    var chooseDateFiled = ObservableField<String>()
    var currentOrder = MutableLiveData<OrderInfo>()
    var updateData = MutableLiveData<Update>()
    var allUploadOrders = MutableLiveData<MutableList<SubmitOrder>>()
    private val books: LiveData<MutableList<SubmitOrder>>

    var showStatusFiled = ObservableField(0)

    private val bookRepository: SubmitRepository

    private val orderLiveData = MutableLiveData<MutableList<OrderInfo>>()

    var loginCommand: BindingCommand<Any> = BindingCommand(object : BindingAction {
        override fun call() {
            if (usernameFiled.get().isNullOrEmpty() || passwordFiled.get().isNullOrEmpty()) {
                toast("请输入账户或密码")
            } else {
                loading("登录中...")
                val params =
                    arrayMapOf<String, Any>(
                        "username" to usernameFiled.get().toString(),
                        "password" to passwordFiled.get().toString()
                    )
                post<LoginInfo>(Url.login, params, 200)
            }
        }
    })

    var loginOut: BindingCommand<Any> = BindingCommand(object : BindingAction {
        override fun call() {
            CangJie.clearAll()
            action(MsgEvent(1))
        }
    })
    var chooseDate: BindingCommand<Any> = BindingCommand(object : BindingAction {
        override fun call() {
            action(MsgEvent(2))
        }
    })
    var detailClose: BindingCommand<Any> = BindingCommand(object : BindingAction {
        override fun call() {
            action(MsgEvent(3))
        }
    })
    var clearCommand: BindingCommand<Any> = BindingCommand(object : BindingAction {
        override fun call() {
            action(MsgEvent(4))
        }
    })
    var removeShellCommand: BindingCommand<Any> = BindingCommand(object : BindingAction {
        override fun call() {
            action(MsgEvent(119))
        }
    })
    var submitCommand: BindingCommand<Any> = BindingCommand(object : BindingAction {
        override fun call() {
            action(MsgEvent(5))
        }
    })
    var searchCommand: BindingCommand<Any> = BindingCommand(object : BindingAction {
        override fun call() {
            action(MsgEvent(6))
        }
    })
    var checkCommand: BindingCommand<Any> = BindingCommand(object : BindingAction {
        override fun call() {
            showStatusFiled.set(0)
        }
    })
    var unCheckCommand: BindingCommand<Any> = BindingCommand(object : BindingAction {
        override fun call() {
            showStatusFiled.set(1)
        }
    })
    var resetZeroCmd: BindingCommand<Any> = BindingCommand(object : BindingAction {
        override fun call() {
            action(MsgEvent(700))
        }
    })

    var takePhoto: BindingCommand<Any> = BindingCommand(object : BindingAction {
        override fun call() {
            action(MsgEvent(7))
        }
    })

    fun loadMain(date: String) {
        loading("获取订单...")
        val params = arrayMapOf("day" to date)
        postWithToken<MutableList<OrderInfo>>(Url.orders, params, 201)
    }

    fun loadDetail(id: String) {
        loading("获取订单...")
        val params = arrayMapOf("order_id" to id)
        postWithToken<MutableList<OrderInfo>>(Url.orders, params, 203)
    }

    fun loadUpdate() {
        postWithToken<Update>(Url.update, arrayMapOf(), 210)
    }


    fun submit(id: String, quantity: String, mode: String, submitType: String) {
        loading("提交中...")
        val params = arrayMapOf(
            "id" to id,
            "quantity" to quantity,
            "stock_mode" to mode,
            "type" to submitType
        )
        postWithToken<Any>(Url.submit, params, 202)

    }

    fun clear(id: String) {
        loading("请求中...")
        val params = arrayMapOf("id" to id)
        postWithToken<Any>(Url.clear, params, 223)
    }

    fun again(id: String) {
        loading("请求中...")
        val params = arrayMapOf("id" to id)
        postWithToken<Any>(Url.again, params, 204)
    }

    override fun success(code: Int, result: Any?) {
        super.success(code, result)
        dismissLoading()
        when (code) {
            200 -> {
                val loginInfo = result as LoginInfo
                CangJie.put("token", loginInfo.access_token)
                toast("登录成功")
                action(MsgEvent(0))
            }
            201 -> {
                val orderInfo = result as MutableList<OrderInfo>
                orderLiveData.postValue(orderInfo)
            }
            202 -> {
                action(MsgEvent(200))
            }
            223 -> {
                action(MsgEvent(223))
            }
            203 -> {
                val currentOrderInfo = result as MutableList<OrderInfo>
                if (currentOrderInfo.size > 0) {
                    currentOrder.postValue(currentOrderInfo[0])
                }
            }
            204 -> {
                action(MsgEvent(300))
            }
            210 -> {
                val up = result as Update
                updateData.postValue(up)
            }

        }
    }

    fun getOrderInfo(): MutableLiveData<MutableList<OrderInfo>> = orderLiveData
    fun getUpdate(): MutableLiveData<Update> = updateData

    override fun error(errorCode: Int, errorMsg: String?) {
        super.error(errorCode, errorMsg)
        dismissLoading()
        toast(errorMsg!!)
    }

    init {
        val orderDao = AppDatabase.get(ScaleApplication.instance!!, viewModelScope).orderDao()
        bookRepository = SubmitRepository(orderDao)
        books = bookRepository.allOrders
    }

    fun add(book: SubmitOrder) = viewModelScope.launch(Dispatchers.IO) {
        bookRepository.insert(book)
    }

    fun getUpload() {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                val booksDao = AppDatabase.get(ScaleApplication.instance!!).orderDao()
                val orders = booksDao.getUpload()
                emit(orders)
            }.catch {
                Log.e("info", it.errorMsg)
            }.collect {
                allUploadOrders.postValue(it)
            }
        }
    }

    fun uploadImg(order: SubmitOrder) {
        viewModelScope.launch {
            RxHttp.postForm(Url.upload)
                .add("access_token", CangJie.getString("token"))
                .add("id", order.goodsId)
                .add("batch", order.batchId)
                .addFile("file", File(order.batchPath))
                .upload {

                }
                .toStr().awaitResult()
        }
    }

    fun update(book: SubmitOrder) = viewModelScope.launch(Dispatchers.IO) {
        bookRepository.update(book)
    }

    fun delete(book: SubmitOrder) = viewModelScope.launch(Dispatchers.IO) {
        bookRepository.delete(book)
    }
}