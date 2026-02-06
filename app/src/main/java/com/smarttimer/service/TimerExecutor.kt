package com.smarttimer.service

import com.smarttimer.data.local.entity.WorkflowWithTimers
import com.smarttimer.util.SoundPlayer
import com.smarttimer.util.TtsManager
import com.smarttimer.util.VibrationManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimerExecutor(
    private val workflow: WorkflowWithTimers,
    private val soundPlayer: SoundPlayer,
    private val ttsManager: TtsManager,
    private val vibrationManager: VibrationManager,
    private val onWorkflowComplete: () -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var currentTimerIndex = 0
    private var currentJob: Job? = null
    private var isPaused = false
    private var remainingSeconds = 0

    private val ttsEnabled = workflow.workflow.ttsEnabled
    private val vibrationEnabled = workflow.workflow.vibrationEnabled

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    fun start() {
        if (workflow.timers.isEmpty()) {
            onWorkflowComplete()
            return
        }
        scope.launch {
            // Wait for TTS to initialize before first announcement
            if (ttsEnabled) {
                ttsManager.awaitInitialization()
                ttsManager.speakAndWait("Starting ${workflow.workflow.name}")
            }
            startNextTimer()
        }
    }

    private fun startNextTimer() {
        if (currentTimerIndex >= workflow.timers.size) {
            scope.launch {
                // Announce workflow completion
                if (ttsEnabled) {
                    ttsManager.speakAndWait("${workflow.workflow.name} completed")
                }
                _timerState.value = TimerState.Stopped
                onWorkflowComplete()
            }
            return
        }

        val currentTimer = workflow.timers[currentTimerIndex]
        val nextTimer = workflow.timers.getOrNull(currentTimerIndex + 1)
        remainingSeconds = currentTimer.durationSeconds

        currentJob = scope.launch {
            // Announce task start
            if (ttsEnabled) {
                ttsManager.speakAndWait("Starting ${currentTimer.label}")
            }

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
        val waitDurationSeconds = workflow.workflow.alertDurationSeconds

        // Announce task completion
        if (ttsEnabled) {
            ttsManager.speakAndWait("${completedTimer.label} completed")
        }

        soundPlayer.playAlert()

        // Start vibration for the wait period
        if (vibrationEnabled) {
            vibrationManager.vibrate(waitDurationSeconds * 1000L)
        }

        // Countdown loop for wait period with UI updates
        for (remaining in waitDurationSeconds downTo 1) {
            _timerState.value = TimerState.AlertPlaying(
                completedTimer = completedTimer,
                waitRemainingSeconds = remaining,
                waitTotalSeconds = waitDurationSeconds
            )
            delay(1000L)
        }

        soundPlayer.stopAlert()
        if (vibrationEnabled) {
            vibrationManager.stop()
        }

        currentTimerIndex++
        if (currentTimerIndex < workflow.timers.size) {
            startNextTimer()
        } else {
            // Announce workflow completion
            if (ttsEnabled) {
                ttsManager.speakAndWait("${workflow.workflow.name} completed")
            }
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
        ttsManager.stop()
        soundPlayer.release()
        vibrationManager.stop()
    }
}
