package com.smarttimer.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.smarttimer.data.local.entity.WorkflowWithTimers
import com.smarttimer.util.SoundPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerService : Service() {

    private val binder = TimerServiceBinder()
    private var timerExecutor: TimerExecutor? = null
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var soundPlayer: SoundPlayer
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Persistent state flow that always exists
    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    private val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        soundPlayer = SoundPlayer(this)
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun startWorkflow(workflow: WorkflowWithTimers) {
        timerExecutor = TimerExecutor(
            workflow = workflow,
            soundPlayer = soundPlayer,
            onWorkflowComplete = {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        )

        val notification = notificationHelper.createNotification(
            title = "Timer Running",
            content = "Executing: ${workflow.workflow.name}"
        )
        startForeground(NotificationHelper.NOTIFICATION_ID, notification)

        // Collect timer executor state and forward to service state
        serviceScope.launch {
            timerExecutor?.timerState?.collect { state ->
                _timerState.value = state
            }
        }

        timerExecutor?.start()
    }

    fun pauseTimer() {
        timerExecutor?.pause()
    }

    fun resumeTimer() {
        timerExecutor?.resume()
    }

    fun stopTimer() {
        timerExecutor?.stop()
        _timerState.value = TimerState.Idle
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun getTimerState(): StateFlow<TimerState> {
        return timerState
    }

    override fun onDestroy() {
        timerExecutor?.stop()
        soundPlayer.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    inner class TimerServiceBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }
}
