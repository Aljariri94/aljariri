package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.model.KeyType

class SoundHapticManager(context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Lazy ToneGenerator initialization: only created when sound is first actually played!
    private var isToneInitAttempted = false
    private var toneGenerator: ToneGenerator? = null

    private fun getOrCreateToneGenerator(): ToneGenerator? {
        if (!isToneInitAttempted) {
            isToneInitAttempted = true
            toneGenerator = try {
                ToneGenerator(AudioManager.STREAM_MUSIC, 85)
            } catch (_: Exception) {
                try {
                    ToneGenerator(AudioManager.STREAM_SYSTEM, 70)
                } catch (_: Exception) {
                    null
                }
            }
        }
        return toneGenerator
    }

    /**
     * Triggers distinct native tactile feedback and audible sound effects based on the exact button pressed.
     */
    fun triggerFeedback(keyType: KeyType, hapticEnabled: Boolean, soundEnabled: Boolean) {
        if (hapticEnabled) {
            triggerHaptic(keyType)
        }
        if (soundEnabled) {
            triggerSound(keyType)
        }
    }

    fun triggerHaptic(keyType: KeyType) {
        vibrator ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                when (keyType) {
                    KeyType.DOUBLE_ZERO -> {
                        // Double tactile tap for 00
                        val timings = longArrayOf(0, 16, 35, 16)
                        val amplitudes = intArrayOf(0, 180, 0, 180)
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    }
                    KeyType.TRIPLE_ZERO -> {
                        // Triple tactile pulse for 000
                        val timings = longArrayOf(0, 14, 28, 14, 28, 14)
                        val amplitudes = intArrayOf(0, 200, 0, 200, 0, 200)
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    }
                    KeyType.EQUALS -> {
                        // Satisfying strong pulse on calculate
                        vibrator.vibrate(VibrationEffect.createOneShot(42, 230))
                    }
                    KeyType.BACKSPACE -> {
                        // Snappy sharp click
                        vibrator.vibrate(VibrationEffect.createOneShot(18, 160))
                    }
                    KeyType.CLEAR, KeyType.ALL_CLEAR -> {
                        // Double bump
                        val timings = longArrayOf(0, 22, 30, 22)
                        val amplitudes = intArrayOf(0, 150, 0, 150)
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    }
                    KeyType.OPERATOR, KeyType.PARENTHESES -> {
                        // Crisp operator click
                        vibrator.vibrate(VibrationEffect.createOneShot(20, 140))
                    }
                    KeyType.FUNCTION, KeyType.MEMORY -> {
                        // Medium tactile tap
                        vibrator.vibrate(VibrationEffect.createOneShot(18, 120))
                    }
                    KeyType.DIGIT -> {
                        // Ultra-light keyboard tick
                        vibrator.vibrate(VibrationEffect.createOneShot(10, 85))
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                when (keyType) {
                    KeyType.DOUBLE_ZERO -> vibrator.vibrate(longArrayOf(0, 15, 30, 15), -1)
                    KeyType.TRIPLE_ZERO -> vibrator.vibrate(longArrayOf(0, 12, 25, 12, 25, 12), -1)
                    KeyType.EQUALS -> vibrator.vibrate(40)
                    else -> vibrator.vibrate(15)
                }
            }
        } catch (_: Exception) {}
    }

    private fun triggerSound(keyType: KeyType) {
        try {
            // First try native system click effect
            val fx = when (keyType) {
                KeyType.EQUALS -> AudioManager.FX_KEYPRESS_RETURN
                KeyType.BACKSPACE -> AudioManager.FX_KEYPRESS_DELETE
                KeyType.CLEAR, KeyType.ALL_CLEAR -> AudioManager.FX_KEYPRESS_DELETE
                else -> AudioManager.FX_KEY_CLICK
            }
            audioManager?.playSoundEffect(fx, 1.0f)

            // Also play rich synthesized click tone via ToneGenerator on STREAM_MUSIC for guaranteed crisp response
            getOrCreateToneGenerator()?.let { tg ->
                val (tone, durationMs) = when (keyType) {
                    KeyType.EQUALS -> Pair(ToneGenerator.TONE_PROP_PROMPT, 50)
                    KeyType.CLEAR, KeyType.ALL_CLEAR -> Pair(ToneGenerator.TONE_PROP_BEEP2, 40)
                    KeyType.DOUBLE_ZERO, KeyType.TRIPLE_ZERO -> Pair(ToneGenerator.TONE_PROP_ACK, 25)
                    KeyType.BACKSPACE -> Pair(ToneGenerator.TONE_PROP_NACK, 25)
                    KeyType.OPERATOR, KeyType.PARENTHESES, KeyType.FUNCTION -> Pair(ToneGenerator.TONE_PROP_BEEP, 22)
                    else -> Pair(ToneGenerator.TONE_PROP_BEEP, 18)
                }
                tg.startTone(tone, durationMs)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
