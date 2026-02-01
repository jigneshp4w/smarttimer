package com.smarttimer.util

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.util.Log

class SoundPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun playAlert() {
        try {
            mediaPlayer?.release()

            // Use default notification sound
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            mediaPlayer = MediaPlayer.create(context, notification).apply {
                setOnCompletionListener {
                    release()
                    mediaPlayer = null
                }
                start()
            }
        } catch (e: Exception) {
            Log.e("SoundPlayer", "Error playing alert", e)
        }
    }

    fun stopAlert() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
