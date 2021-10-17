package com.cangjie.scalage.core.db

import android.content.Context
import android.os.Parcelable
import com.tencent.mmkv.MMKV

/**
 * @author nvwa@cangjie
 * Create by AS at 2020/7/14 09:14
 */
object CangJie {

    internal val config = Config()

    @JvmStatic
    fun init(context: Context) {
        MMKV.initialize(context)
    }

    @JvmStatic
    fun config(block: Config.() -> Unit) {
        config.block()
    }

    @JvmStatic
    fun bucket(name: String, block: (CangJieImpl.() -> CangJieImpl)? = null): CangJieImpl {
        val hereImpl = CangJieImpl.bucket(name, config)
        return block?.invoke(hereImpl) ?: hereImpl
    }

    @JvmStatic
    fun global() = bucket(BucketFactory.Global.name)

    /**
     * 存储列表对象
     */
    @JvmStatic
    fun <E : Parcelable> put(key: String, elementList: List<E>): CangJieImpl {
        return global().put(key, elementList)
    }

    @JvmStatic
    fun <E : Parcelable> put(key: String, map: Map<String, E>): CangJieImpl {
        return global().put(key, map)
    }

    /**
     * 存储Parcelable对象
     */
    @JvmStatic
    fun <E : Parcelable> put(key: String, element: E): CangJieImpl {
        return global().put(key, element)
    }

    /**
     * 存储Boolean值
     */
    @JvmStatic
    fun put(key: String, boolean: Boolean): CangJieImpl {
        return global().put(key, boolean)
    }

    /**
     * 存储Int值
     */
    @JvmStatic
    fun put(key: String, int: Int): CangJieImpl {
        return global().put(key, int)
    }

    /**
     * 存储Long值
     */
    @JvmStatic
    fun put(key: String, long: Long): CangJieImpl {
        return global().put(key, long)
    }

    /**
     * 存储Float值
     */
    @JvmStatic
    fun put(key: String, float: Float): CangJieImpl {
        return global().put(key, float)
    }

    /**
     * 存储Double值
     */
    @JvmStatic
    fun put(key: String, double: Double): CangJieImpl {
        return global().put(key, double)
    }

    /**
     * 存储String值
     */
    @JvmStatic
    fun put(key: String, string: String): CangJieImpl {
        return global().put(key, string)
    }

    /**
     * 存储字节数组
     */
    @JvmStatic
    fun put(key: String, array: ByteArray): CangJieImpl {
        return global().put(key, array)
    }

    /**
     * 存储字符串集合
     */
    @JvmStatic
    fun put(key: String, set: Set<String>): CangJieImpl {
        return global().put(key, set)
    }

    /**
     * 获取列表对象
     */
    @JvmStatic
    fun <E : Parcelable> getList(key: String, creator: Parcelable.Creator<E>): List<E>? {
        return global().getList(key, creator)
    }


    @JvmStatic
    fun <E : Parcelable> getMap(key: String): Map<String, E?> {
        return global().getMap(key)
    }

    /**
     * 获取Parcelable对象
     */
    @JvmStatic
    fun <E : Parcelable> getParcelable(key: String, clazz: Class<E>): E? {
        return global().getParcelable(key, clazz)
    }

    /**
     * 获取Boolean值
     */
    @JvmStatic
    fun getBool(key: String): Boolean {
        return getBool(key, false)
    }

    @JvmStatic
    fun getBool(key: String, defaultValue: Boolean): Boolean {
        return global().getBool(key, defaultValue)
    }

    /**
     * 获取Int值
     */
    @JvmStatic
    fun getInt(key: String): Int {
        return getInt(key, 0)
    }

    @JvmStatic
    fun getInt(key: String, defaultValue: Int): Int {
        return global().getInt(key, defaultValue)
    }

    /**
     * 获取Long值
     */
    @JvmStatic
    fun getLong(key: String): Long {
        return getLong(key, 0L)
    }

    @JvmStatic
    fun getLong(key: String, defaultValue: Long): Long {
        return global().getLong(key, defaultValue)
    }

    /**
     * 获取String值
     */
    @JvmStatic
    fun getString(key: String): String {
        return getString(key, "")
    }

    @JvmStatic
    fun getString(key: String, defaultValue: String): String {
        return global().getString(key, defaultValue)
    }

    /**
     * 获取Double值
     */
    @JvmStatic
    fun getDouble(key: String): Double {
        return getDouble(key, 0.0)
    }

    @JvmStatic
    fun getDouble(key: String, defaultValue: Double): Double {
        return global().getDouble(key, defaultValue)
    }

    /**
     * 获取Float值
     */
    @JvmStatic
    fun getFloat(key: String): Float {
        return getFloat(key, 0.0F)
    }

    @JvmStatic
    fun getFloat(key: String, defaultValue: Float): Float {
        return global().getFloat(key, defaultValue)
    }

    /**
     * 获取字节数组
     */
    @JvmStatic
    fun getByteArray(key: String): ByteArray? {
        return global().getByteArray(key)
    }

    @JvmStatic
    fun getByteArray(key: String, defaultValue: ByteArray): ByteArray {
        return global().getByteArray(key, defaultValue)
    }

    /**
     * 获取字符串Set
     */
    @JvmStatic
    fun getStringSet(key: String): Set<String>? {
        return getStringSet(key, null)
    }

    @JvmStatic
    fun getStringSet(key: String, defaultValue: Set<String>?): Set<String>? {
        return global().getStringSet(key, defaultValue)
    }

    @JvmStatic
    fun clearAll() {
        global().clearAll()
    }

    /**
     * 删除指定key的值
     */
    @JvmStatic
    fun removeValueForKey(key: String) {
        global().removeValueForKey(key)
    }

    /**
     * 查询Here是否包含指定key的值
     */
    @JvmStatic
    fun containsKey(key: String): Boolean {
        return global().containsKey(key)
    }
}