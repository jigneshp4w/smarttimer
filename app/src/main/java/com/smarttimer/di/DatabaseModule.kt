package com.smarttimer.di

import android.content.Context
import com.smarttimer.data.local.dao.TimerDao
import com.smarttimer.data.local.dao.WorkflowDao
import com.smarttimer.data.local.database.SmartTimerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SmartTimerDatabase {
        return SmartTimerDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideWorkflowDao(database: SmartTimerDatabase): WorkflowDao {
        return database.workflowDao()
    }

    @Provides
    @Singleton
    fun provideTimerDao(database: SmartTimerDatabase): TimerDao {
        return database.timerDao()
    }
}
