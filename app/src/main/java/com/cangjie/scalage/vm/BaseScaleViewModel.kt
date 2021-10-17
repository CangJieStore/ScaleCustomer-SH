package com.cangjie.scalage.vm

import androidx.collection.ArrayMap
import androidx.lifecycle.viewModelScope
import com.cangjie.scalage.base.http.HttpResultCallback
import com.cangjie.scalage.base.http.errorCode
import com.cangjie.scalage.base.http.errorMsg
import com.cangjie.scalage.core.BaseViewModel
import com.cangjie.scalage.core.db.CangJie
import kotlinx.coroutines.launch
import rxhttp.RxHttp
import rxhttp.awaitResult
import rxhttp.toResultInfo

/**
 * @author: guruohan
 * @date: 2021/9/9
 */
open class BaseScaleViewModel : BaseViewModel(), HttpResultCallback {
    inline fun <reified T : Any> post(
        url: String,
        params: MutableMap<String, Any>,
        requestCode: Int
    ) {
        viewModelScope.launch {
            start()
            RxHttp.postForm(url).addAll(params).toResultInfo<T>().awaitResult {
                success(requestCode, it)
            }.onFailure {
                error(it.errorCode, it.errorMsg)
            }
        }
    }

    inline fun <reified T : Any> postWithToken(
        url: String,
        params: MutableMap<String, String>,
        requestCode: Int
    ) {
        params["access_token"] = CangJie.getString("token")
        viewModelScope.launch {
            RxHttp.postForm(url).addAll(params).toResultInfo<T>().awaitResult {
                success(requestCode, it)
            }.onFailure {
                error(it.errorCode, it.errorMsg)
            }
        }
    }

    override fun start() {
    }

    override fun success(code: Int, result: Any?) {
    }

    override fun error(errorCode: Int, errorMsg: String?) {
    }

    override fun complete() {
    }
}