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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExecutionViewModel @Inject constructor(
    private val workflowRepository: WorkflowRepository,
    @ApplicationContext private val appContext: Context
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

            // Check if service is already running a workflow
            if (timerService?.isRunning() == true) {
                _selectedWorkflow.value = timerService?.getCurrentWorkflow()
            }

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

    init {
        // Check if a workflow is already running and reconnect
        checkAndReconnectToRunningService()
    }

    private fun checkAndReconnectToRunningService() {
        val intent = Intent(appContext, TimerService::class.java)
        try {
            appContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            // Service not available or other error, continue with normal flow
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

            // Only bind if not already bound
            if (!serviceBound) {
                context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            }

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
            try {
                context.unbindService(serviceConnection)
            } catch (e: Exception) {
                // Service may already be unbound
            }
            serviceBound = false
        }
        _timerState.value = TimerState.Idle
        _selectedWorkflow.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // Unbind from service when ViewModel is cleared
        if (serviceBound) {
            try {
                appContext.unbindService(serviceConnection)
            } catch (e: Exception) {
                // Service may already be unbound
            }
            serviceBound = false
        }
        timerService = null
    }
}
