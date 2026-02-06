package com.smarttimer.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume

class TtsManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val initializationDeferred = CompletableDeferred<Boolean>()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.getDefault())
            isInitialized = result != TextToSpeech.LANG_MISSING_DATA &&
                           result != TextToSpeech.LANG_NOT_SUPPORTED
            if (!isInitialized) {
                // Fallback to English if default locale not supported
                tts?.setLanguage(Locale.US)
                isInitialized = true
            }
            Log.d(TAG, "TTS initialized successfully")
        } else {
            Log.e(TAG, "TTS initialization failed with status: $status")
            isInitialized = false
        }
        initializationDeferred.complete(isInitialized)
    }

    /**
     * Suspends until TTS is initialized or times out after the specified duration.
     * Returns true if initialized successfully, false otherwise.
     */
    suspend fun awaitInitialization(timeoutMs: Long = 3000L): Boolean {
        if (isInitialized) return true
        return withTimeoutOrNull(timeoutMs) {
            initializationDeferred.await()
        } ?: false
    }

    fun speak(text: String) {
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized, skipping: $text")
            return
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
    }

    suspend fun speakAndWait(text: String) {
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized, skipping: $text")
            return
        }

        suspendCancellableCoroutine { continuation ->
            val utteranceId = UUID.randomUUID().toString()

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}

                override fun onDone(id: String?) {
                    if (id == utteranceId && continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(id: String?) {
                    if (id == utteranceId && continuation.isActive) {
                        Log.e(TAG, "TTS error for utterance: $id")
                        continuation.resume(Unit)
                    }
                }
            })

            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

            continuation.invokeOnCancellation {
                tts?.stop()
            }
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    companion object {
        private const val TAG = "TtsManager"
    }
}
