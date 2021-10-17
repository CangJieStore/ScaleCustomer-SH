package com.cangjie.scalage.base.http

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/7/11 10:20
 */
class ResultInfo<T>(val code: Int, val msg: String, val data: T) {
}