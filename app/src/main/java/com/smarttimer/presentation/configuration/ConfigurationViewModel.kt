package com.smarttimer.presentation.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttimer.data.local.entity.TimerEntity
import com.smarttimer.data.local.entity.WorkflowEntity
import com.smarttimer.data.local.entity.WorkflowWithTimers
import com.smarttimer.data.repository.TimerRepository
import com.smarttimer.data.repository.WorkflowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    private val workflowRepository: WorkflowRepository,
    private val timerRepository: TimerRepository
) : ViewModel() {

    val allWorkflows: StateFlow<List<WorkflowWithTimers>> =
        workflowRepository.getAllWorkflowsWithTimers()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun createWorkflow(name: String, description: String?, alertDurationSeconds: Int = 3) {
        viewModelScope.launch {
            workflowRepository.insertWorkflow(
                WorkflowEntity(
                    name = name,
                    description = description,
                    alertDurationSeconds = alertDurationSeconds
                )
            )
        }
    }

    fun updateWorkflow(workflow: WorkflowEntity) {
        viewModelScope.launch {
            workflowRepository.updateWorkflow(workflow.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteWorkflow(workflow: WorkflowEntity) {
        viewModelScope.launch {
            workflowRepository.deleteWorkflow(workflow)
        }
    }

    fun addTimerToWorkflow(workflowId: Long, label: String, durationSeconds: Int, position: Int) {
        viewModelScope.launch {
            timerRepository.insertTimer(
                TimerEntity(
                    workflowId = workflowId,
                    label = label,
                    durationSeconds = durationSeconds,
                    position = position
                )
            )
        }
    }

    fun updateTimer(timer: TimerEntity) {
        viewModelScope.launch {
            timerRepository.updateTimer(timer)
        }
    }

    fun deleteTimer(timer: TimerEntity) {
        viewModelScope.launch {
            timerRepository.deleteTimer(timer)
        }
    }
}
