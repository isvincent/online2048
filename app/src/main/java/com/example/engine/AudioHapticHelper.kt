package com.example.engine

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class AudioHapticHelper(private val context: Context) {

    private var toneGen: ToneGenerator? = null
    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) {
        null
    }

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (_: Exception) {
            toneGen = null
        }
    }

    fun playSlideSound(enabled: Boolean) {
        if (!enabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
        } catch (_: Exception) {}
    }

    fun playMergeSound(enabled: Boolean, tileValue: Int) {
        if (!enabled) return
        try {
            val tone = when {
                tileValue >= 2048 -> ToneGenerator.TONE_CDMA_HIGH_PBX_L
                tileValue >= 512 -> ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE
                tileValue >= 64 -> ToneGenerator.TONE_PROP_ACK
                else -> ToneGenerator.TONE_PROP_BEEP2
            }
            toneGen?.startTone(tone, 60)
        } catch (_: Exception) {}
    }

    fun playVictorySound(enabled: Boolean) {
        if (!enabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
        } catch (_: Exception) {}
    }

    fun playGameOverSound(enabled: Boolean) {
        if (!enabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_NACK, 180)
        } catch (_: Exception) {}
    }

    fun triggerVibration(enabled: Boolean, isMerge: Boolean = false) {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (isMerge) {
                    VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    VibrationEffect.createOneShot(15, 80)
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(if (isMerge) 35 else 15)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGen?.release()
            toneGen = null
        } catch (_: Exception) {}
    }
}
