package com.smarttimer.data.local.dao

import androidx.room.*
import com.smarttimer.data.local.entity.TimerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerDao {
    @Query("SELECT * FROM timers WHERE workflow_id = :workflowId ORDER BY position ASC")
    fun getTimersByWorkflowId(workflowId: Long): Flow<List<TimerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimer(timer: TimerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimers(timers: List<TimerEntity>)

    @Update
    suspend fun updateTimer(timer: TimerEntity)

    @Delete
    suspend fun deleteTimer(timer: TimerEntity)

    @Query("DELETE FROM timers WHERE workflow_id = :workflowId")
    suspend fun deleteTimersByWorkflowId(workflowId: Long)

    @Query("SELECT * FROM timers WHERE id = :timerId")
    suspend fun getTimerById(timerId: Long): TimerEntity?
}
