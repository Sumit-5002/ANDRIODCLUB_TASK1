package com.task_one.notifyme.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Task::class],
    version = 1,
    exportSchema = true
)
abstract class NotifyMeDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile private var instance: NotifyMeDatabase? = null

        fun get(context: Context): NotifyMeDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NotifyMeDatabase::class.java,
                    "notifyme.db"
                ).build().also { instance = it }
            }
    }
}


