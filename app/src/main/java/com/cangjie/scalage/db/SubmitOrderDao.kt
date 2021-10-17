package com.cangjie.scalage.db

import androidx.lifecycle.LiveData
import androidx.room.*
import org.jetbrains.annotations.NotNull

@Dao
interface SubmitOrderDao {

    @Query(value = "SELECT * FROM orders_table WHERE isUpload==1")
    fun getAll(): LiveData<MutableList<SubmitOrder>>

    @Query(value = "SELECT * FROM orders_table WHERE isUpload==1")
    fun getUpload(): MutableList<SubmitOrder>


    @Insert(entity = SubmitOrder::class, onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(@NotNull order: SubmitOrder)

    @Update
    suspend fun update(order: SubmitOrder)

    @Delete
    suspend fun delete(order: SubmitOrder)

}