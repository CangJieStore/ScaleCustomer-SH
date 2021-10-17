package com.cangjie.scalage.base.http

import rxhttp.wrapper.annotation.Parser
import rxhttp.wrapper.entity.ParameterizedTypeImpl
import rxhttp.wrapper.exception.ParseException
import rxhttp.wrapper.parse.TypeParser
import rxhttp.wrapper.utils.convertTo
import java.io.IOException
import java.lang.reflect.Type

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/7/11 16:48
 */
@Parser(name = "ResultInfo", wrappers = [MutableList::class])
open class ResultParser<T> : TypeParser<T> {

    protected constructor() : super()
    constructor(type: Type) : super(type)

    @Throws(IOException::class)
    override fun onParse(response: okhttp3.Response): T {
        val data: ResultInfo<T> = response.convertTo(ResultInfo::class, *types)
        var t = data.data
        if (t == null && types[0] === String::class.java) {
            @Suppress("UNCHECKED_CAST")
            t = data.msg as T
        }
        if (data.code != 0) {
            throw ParseException(data.code.toString(), data.msg, response)
        }
        if (t == null) {
            t = 0 as T
        }
        return t
    }
}