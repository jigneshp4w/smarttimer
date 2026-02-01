package com.smarttimer.data.repository

import com.smarttimer.data.local.dao.TimerDao
import com.smarttimer.data.local.entity.TimerEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerRepository @Inject constructor(
    private val timerDao: TimerDao
) {
    fun getTimersByWorkflowId(workflowId: Long): Flow<List<TimerEntity>> {
        return timerDao.getTimersByWorkflowId(workflowId)
    }

    suspend fun insertTimer(timer: TimerEntity): Long {
        return timerDao.insertTimer(timer)
    }

    suspend fun insertTimers(timers: List<TimerEntity>) {
        timerDao.insertTimers(timers)
    }

    suspend fun updateTimer(timer: TimerEntity) {
        timerDao.updateTimer(timer)
    }

    suspend fun deleteTimer(timer: TimerEntity) {
        timerDao.deleteTimer(timer)
    }

    suspend fun deleteTimersByWorkflowId(workflowId: Long) {
        timerDao.deleteTimersByWorkflowId(workflowId)
    }

    suspend fun getTimerById(timerId: Long): TimerEntity? {
        return timerDao.getTimerById(timerId)
    }
}
