package com.cangjie.scalage.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [SubmitOrder::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun orderDao(): SubmitOrderDao

    companion object {
        private const val DATABASE_NAME = "submit_order"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context, scope: CoroutineScope): AppDatabase {

            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                return Room
                    .databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
            }
        }

        fun get(context: Context): AppDatabase {

            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                return Room
                    .databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                    .build()
            }
        }

        private class AppDatabaseCallback(val scope: CoroutineScope) : RoomDatabase.Callback() {

            override fun onOpen(db: SupportSQLiteDatabase) {
                INSTANCE?.let {
                    scope.launch {

                    }
                }
            }

        }
    }

}