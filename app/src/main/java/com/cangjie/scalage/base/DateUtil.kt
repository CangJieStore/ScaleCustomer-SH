package com.cangjie.scalage.base

import android.annotation.SuppressLint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtil {
    const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
    const val DATE_FORMAT = "yyyy-MM-dd"

    /**
     * 将字符串转换为Date
     * @param dateStr 日期字符串
     * @param dateFormat 日期格式
     * @return date
     * @throws ParseException
     */
    fun stringToDate(dateStr: String?, dateFormat: String?): Date? {
        @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat(dateFormat)
        try {
            return sdf.parse(dateStr)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 将日期格式化为字符串
     * @param date 日期
     * @param dateFormat 日期格式
     * @return 日期字符串
     */
    fun dateToString(date: Date?, dateFormat: String?): String {
        @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat(dateFormat)
        return sdf.format(date)
    }

    /**
     * 将日期转化为时间戳
     * @param date 日期
     * @return 时间戳
     */
    fun dateToLong(date: Date): Long {
        return date.time
    }

    /**
     * 将时间戳转化为日期
     * @param timeStamp 时间戳
     * @param dateFormat
     * @return
     * @throws ParseException
     */
    fun longToDate(timeStamp: Long, dateFormat: String?): Date? {
        val date = Date(timeStamp)
        val dateStr = dateToString(date, dateFormat)
        return stringToDate(dateStr, dateFormat)
    }

    /**
     * 将时间戳转化为字符串
     * @param timeStamp 时间戳
     * @param dateFormat 日期格式
     * @return 日期字符串
     */
    fun longToString(timeStamp: Long, dateFormat: String?): String {
        @SuppressLint("SimpleDateFormat") val sdf = SimpleDateFormat(dateFormat)
        return sdf.format(timeStamp)
    }

    /**
     * 将日期字符创转化为时间戳
     * @param dateStr 日期字符串
     * @param dateFormat 日期格式
     * @return 时间戳
     */
    fun stringToLong(dateStr: String?, dateFormat: String?): Long {
        val date = stringToDate(dateStr, dateFormat)
        return date!!.time
    }
}