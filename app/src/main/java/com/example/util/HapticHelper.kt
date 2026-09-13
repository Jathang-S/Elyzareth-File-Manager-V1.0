package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

class HapticHelper(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Tactile click for row taps, button clicks, and single-click file info.
     */
    fun performClick(composeHaptic: HapticFeedback? = null) {
        composeHaptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        vibrateEffect(VibrationEffect.EFFECT_CLICK, 15)
    }

    /**
     * Drag pickup feedback - crisp, distinct pulse when an item is grabbed.
     */
    fun performDragStart(composeHaptic: HapticFeedback? = null) {
        composeHaptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        vibrateEffect(VibrationEffect.EFFECT_HEAVY_CLICK, 35)
    }

    /**
     * Drag reorder step - light tactile tick when an item jumps over another slot.
     */
    fun performReorderTick(composeHaptic: HapticFeedback? = null) {
        composeHaptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        vibrateEffect(VibrationEffect.EFFECT_TICK, 10)
    }

    /**
     * Drop feedback - reassuring thud when item is dropped into place.
     */
    fun performDrop(composeHaptic: HapticFeedback? = null) {
        composeHaptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        vibrateEffect(VibrationEffect.EFFECT_CLICK, 25)
    }

    /**
     * Instant delete feedback - sharp double pulse indicating rapid deletion without dialog.
     */
    fun performDelete(composeHaptic: HapticFeedback? = null) {
        composeHaptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 30, 40, 50)
            val amplitudes = intArrayOf(0, 180, 0, 240)
            try {
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } catch (_: Exception) {
                fallbackVibrate(60)
            }
        } else {
            fallbackVibrate(60)
        }
    }

    /**
     * Folder color change or folder selection feedback.
     */
    fun performColorChange(composeHaptic: HapticFeedback? = null) {
        composeHaptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        vibrateEffect(VibrationEffect.EFFECT_DOUBLE_CLICK, 25)
    }

    /**
     * Restore / Undo operation feedback.
     */
    fun performUndo(composeHaptic: HapticFeedback? = null) {
        composeHaptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        vibrateEffect(VibrationEffect.EFFECT_CLICK, 20)
    }

    private fun vibrateEffect(predefinedEffectId: Int, fallbackDurationMs: Long) {
        if (vibrator == null || !vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                vibrator.vibrate(VibrationEffect.createPredefined(predefinedEffectId))
                return
            } catch (_: Exception) {
                // fall through to one-shot
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                vibrator.vibrate(VibrationEffect.createOneShot(fallbackDurationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } catch (_: Exception) {
                fallbackVibrate(fallbackDurationMs)
            }
        } else {
            fallbackVibrate(fallbackDurationMs)
        }
    }

    private fun fallbackVibrate(durationMs: Long) {
        @Suppress("DEPRECATION")
        try {
            vibrator?.vibrate(durationMs)
        } catch (_: Exception) {
            // ignore if permissions or hardware lacking
        }
    }
}
