package com.cangjie.scalage.base.http

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/7/9 21:50
 */
interface HttpResultCallback {
    fun start()
    fun success(code: Int, result: Any?)
    fun error(errorCode: Int, errorMsg: String?)
    fun complete()
}