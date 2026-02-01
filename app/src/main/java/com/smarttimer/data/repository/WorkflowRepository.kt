package com.smarttimer.data.repository

import com.smarttimer.data.local.dao.WorkflowDao
import com.smarttimer.data.local.entity.WorkflowEntity
import com.smarttimer.data.local.entity.WorkflowWithTimers
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkflowRepository @Inject constructor(
    private val workflowDao: WorkflowDao
) {
    fun getAllWorkflows(): Flow<List<WorkflowEntity>> {
        return workflowDao.getAllWorkflows()
    }

    fun getWorkflowById(workflowId: Long): Flow<WorkflowEntity?> {
        return workflowDao.getWorkflowById(workflowId)
    }

    fun getWorkflowWithTimers(workflowId: Long): Flow<WorkflowWithTimers?> {
        return workflowDao.getWorkflowWithTimers(workflowId)
    }

    fun getAllWorkflowsWithTimers(): Flow<List<WorkflowWithTimers>> {
        return workflowDao.getAllWorkflowsWithTimers()
    }

    suspend fun insertWorkflow(workflow: WorkflowEntity): Long {
        return workflowDao.insertWorkflow(workflow)
    }

    suspend fun updateWorkflow(workflow: WorkflowEntity) {
        workflowDao.updateWorkflow(workflow)
    }

    suspend fun deleteWorkflow(workflow: WorkflowEntity) {
        workflowDao.deleteWorkflow(workflow)
    }
}
