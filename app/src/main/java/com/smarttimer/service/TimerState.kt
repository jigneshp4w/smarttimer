package com.smarttimer.service

import com.smarttimer.data.local.entity.TimerEntity

sealed class TimerState {
    object Idle : TimerState()

    data class Running(
        val currentTimer: TimerEntity,
        val nextTimer: TimerEntity?,
        val remainingSeconds: Int,
        val totalSeconds: Int,
        val currentIndex: Int,
        val totalTimers: Int
    ) : TimerState()

    data class AlertPlaying(
        val completedTimer: TimerEntity,
        val waitRemainingSeconds: Int = 0,
        val waitTotalSeconds: Int = 0
    ) : TimerState()

    object Paused : TimerState()

    object Stopped : TimerState()
}
