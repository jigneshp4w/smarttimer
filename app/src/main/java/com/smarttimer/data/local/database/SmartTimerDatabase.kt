package com.smarttimer.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.smarttimer.data.local.dao.TimerDao
import com.smarttimer.data.local.dao.WorkflowDao
import com.smarttimer.data.local.entity.TimerEntity
import com.smarttimer.data.local.entity.WorkflowEntity

@Database(
    entities = [WorkflowEntity::class, TimerEntity::class],
    version = 1,
    exportSchema = true
)
abstract class SmartTimerDatabase : RoomDatabase() {
    abstract fun workflowDao(): WorkflowDao
    abstract fun timerDao(): TimerDao

    companion object {
        @Volatile
        private var INSTANCE: SmartTimerDatabase? = null

        fun getDatabase(context: Context): SmartTimerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartTimerDatabase::class.java,
                    "smart_timer_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
