package com.smarttimer.presentation.execution

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttimer.data.local.entity.WorkflowWithTimers
import com.smarttimer.data.repository.WorkflowRepository
import com.smarttimer.service.TimerService
import com.smarttimer.service.TimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExecutionViewModel @Inject constructor(
    private val workflowRepository: WorkflowRepository
) : ViewModel() {

    private val _selectedWorkflow = MutableStateFlow<WorkflowWithTimers?>(null)
    val selectedWorkflow: StateFlow<WorkflowWithTimers?> = _selectedWorkflow.asStateFlow()

    private var timerService: TimerService? = null
    private var serviceBound = false

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.TimerServiceBinder
            timerService = binder.getService()
            serviceBound = true

            // Collect timer state from service (now always available)
            viewModelScope.launch {
                timerService?.getTimerState()?.collect { state ->
                    _timerState.value = state
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            serviceBound = false
        }
    }

    val allWorkflows: StateFlow<List<WorkflowWithTimers>> =
        workflowRepository.getAllWorkflowsWithTimers()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun selectWorkflow(workflowId: Long) {
        viewModelScope.launch {
            workflowRepository.getWorkflowWithTimers(workflowId)
                .collectLatest { workflow ->
                    _selectedWorkflow.value = workflow
                }
        }
    }

    fun startWorkflow(context: Context) {
        selectedWorkflow.value?.let { workflow ->
            val intent = Intent(context, TimerService::class.java)
            context.startForegroundService(intent)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

            viewModelScope.launch {
                kotlinx.coroutines.delay(500) // Wait for service to bind
                timerService?.startWorkflow(workflow)
            }
        }
    }

    fun pauseTimer() {
        timerService?.pauseTimer()
    }

    fun resumeTimer() {
        timerService?.resumeTimer()
    }

    fun stopTimer(context: Context) {
        timerService?.stopTimer()
        if (serviceBound) {
            context.unbindService(serviceConnection)
            serviceBound = false
        }
        _timerState.value = TimerState.Idle
        _selectedWorkflow.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timerService = null
    }
}
