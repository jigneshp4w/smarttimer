package com.smarttimer.data.local.dao

import androidx.room.*
import com.smarttimer.data.local.entity.WorkflowEntity
import com.smarttimer.data.local.entity.WorkflowWithTimers
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkflowDao {
    @Query("SELECT * FROM workflows ORDER BY created_at DESC")
    fun getAllWorkflows(): Flow<List<WorkflowEntity>>

    @Query("SELECT * FROM workflows WHERE id = :workflowId")
    fun getWorkflowById(workflowId: Long): Flow<WorkflowEntity?>

    @Transaction
    @Query("SELECT * FROM workflows WHERE id = :workflowId")
    fun getWorkflowWithTimers(workflowId: Long): Flow<WorkflowWithTimers?>

    @Transaction
    @Query("SELECT * FROM workflows ORDER BY created_at DESC")
    fun getAllWorkflowsWithTimers(): Flow<List<WorkflowWithTimers>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkflow(workflow: WorkflowEntity): Long

    @Update
    suspend fun updateWorkflow(workflow: WorkflowEntity)

    @Delete
    suspend fun deleteWorkflow(workflow: WorkflowEntity)
}
