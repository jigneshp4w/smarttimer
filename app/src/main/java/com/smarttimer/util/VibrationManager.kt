package com.smarttimer.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class VibrationManager(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Vibrate with a pulsing pattern for the specified duration.
     * Pattern: 500ms vibrate, 500ms pause, repeat.
     * For short durations (< 1 second), uses a single vibration.
     */
    fun vibrate(durationMs: Long) {
        if (vibrator == null || !vibrator.hasVibrator()) {
            Log.w(TAG, "Vibrator not available")
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (durationMs > 1000) {
                    // For longer durations, use a repeating pattern
                    // Pattern: delay, vibrate, pause (repeating at index 1)
                    val pattern = longArrayOf(0, 500, 500)
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, 1))
                } else {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } else {
                @Suppress("DEPRECATION")
                if (durationMs > 1000) {
                    val pattern = longArrayOf(0, 500, 500)
                    vibrator.vibrate(pattern, 1)
                } else {
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating", e)
        }
    }

    /**
     * Stop any ongoing vibration.
     */
    fun stop() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping vibration", e)
        }
    }

    companion object {
        private const val TAG = "VibrationManager"
    }
}
