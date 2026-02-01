package com.smarttimer.service

import com.smarttimer.data.local.entity.WorkflowWithTimers
import com.smarttimer.util.SoundPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimerExecutor(
    private val workflow: WorkflowWithTimers,
    private val soundPlayer: SoundPlayer,
    private val onWorkflowComplete: () -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var currentTimerIndex = 0
    private var currentJob: Job? = null
    private var isPaused = false
    private var remainingSeconds = 0

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    fun start() {
        if (workflow.timers.isEmpty()) {
            onWorkflowComplete()
            return
        }
        startNextTimer()
    }

    private fun startNextTimer() {
        if (currentTimerIndex >= workflow.timers.size) {
            _timerState.value = TimerState.Stopped
            onWorkflowComplete()
            return
        }

        val currentTimer = workflow.timers[currentTimerIndex]
        val nextTimer = workflow.timers.getOrNull(currentTimerIndex + 1)
        remainingSeconds = currentTimer.durationSeconds

        currentJob = scope.launch {
            while (remainingSeconds > 0 && isActive) {
                if (!isPaused) {
                    _timerState.value = TimerState.Running(
                        currentTimer = currentTimer,
                        nextTimer = nextTimer,
                        remainingSeconds = remainingSeconds,
                        totalSeconds = currentTimer.durationSeconds,
                        currentIndex = currentTimerIndex,
                        totalTimers = workflow.timers.size
                    )
                    delay(1000L)
                    remainingSeconds--
                } else {
                    delay(100L)
                }
            }

            if (isActive && remainingSeconds == 0) {
                onTimerComplete(currentTimer)
            }
        }
    }

    private suspend fun onTimerComplete(completedTimer: com.smarttimer.data.local.entity.TimerEntity) {
        _timerState.value = TimerState.AlertPlaying(completedTimer)
        soundPlayer.playAlert()

        // Use the configured alert duration from workflow
        val alertDuration = workflow.workflow.alertDurationSeconds * 1000L
        delay(alertDuration)

        soundPlayer.stopAlert()

        currentTimerIndex++
        if (currentTimerIndex < workflow.timers.size) {
            startNextTimer()
        } else {
            _timerState.value = TimerState.Stopped
            onWorkflowComplete()
        }
    }

    fun pause() {
        isPaused = true
        _timerState.value = TimerState.Paused
    }

    fun resume() {
        if (isPaused) {
            isPaused = false
            // The running state will be updated in the next loop iteration
        }
    }

    fun stop() {
        currentJob?.cancel()
        scope.cancel()
        _timerState.value = TimerState.Stopped
        soundPlayer.release()
    }
}
