package com.cangjie.scalage.db

import androidx.lifecycle.LiveData
import com.cangjie.scalage.db.SubmitOrder
import com.cangjie.scalage.db.SubmitOrderDao

class SubmitRepository(private val orderDao: SubmitOrderDao) {

    val allOrders: LiveData<MutableList<SubmitOrder>> = orderDao.getAll()

    suspend fun insert(book: SubmitOrder) = orderDao.insert(book)

    suspend fun update(book: SubmitOrder) = orderDao.update(book)

    suspend fun delete(book: SubmitOrder) = orderDao.delete(book)

}