package com.example.engine

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.concurrent.thread
import kotlin.math.sin

class SoundManager(context: Context) {
    private val appContext = context.applicationContext
    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null
    var soundEnabled: Boolean = true
    var hapticsEnabled: Boolean = true

    // AudioTrack for F1 Engine Sound Synthesis
    private var audioTrack: AudioTrack? = null
    @Volatile private var isEngineRunning = false
    @Volatile private var currentFreq = 160f // Base RPM Hz
    @Volatile private var targetFreq = 160f
    @Volatile private var isBoostingEngine = false
    private var audioThread: Thread? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun startEngineSound() {
        if (!soundEnabled || isEngineRunning) return
        isEngineRunning = true

        audioThread = thread(start = true, name = "F1EngineAudio") {
            val sampleRate = 22050
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            try {
                audioTrack = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufSize * 2,
                    AudioTrack.MODE_STREAM
                )

                audioTrack?.play()

                val bufferSize = 1024
                val buffer = ShortArray(bufferSize)
                var phase = 0.0

                while (isEngineRunning && soundEnabled) {
                    // Smooth frequency transition (pitch revving up/down)
                    currentFreq += (targetFreq - currentFreq) * 0.15f
                    val freq = currentFreq

                    for (i in 0 until bufferSize) {
                        val incr = (2.0 * Math.PI * freq) / sampleRate
                        phase += incr
                        if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI

                        // F1 Engine Harmonic Waveform (Fundamental + Sawtooth + Screaming high harmonics)
                        val fundamental = sin(phase)
                        val h2 = 0.5 * sin(2.0 * phase)
                        val h3 = 0.3 * sin(3.0 * phase)
                        val h4 = 0.25 * sin(4.0 * phase)
                        val h6 = if (isBoostingEngine) 0.4 * sin(6.0 * phase) else 0.0

                        var wave = (fundamental + h2 + h3 + h4 + h6) / 2.2
                        if (wave > 1.0) wave = 1.0
                        if (wave < -1.0) wave = -1.0

                        val volume = if (isBoostingEngine) 0.45f else 0.35f
                        buffer[i] = (wave * Short.MAX_VALUE * volume).toInt().toShort()
                    }

                    audioTrack?.write(buffer, 0, bufferSize)
                }

                audioTrack?.stop()
                audioTrack?.release()
                audioTrack = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateEnginePitch(speedRatio: Float, isBoosting: Boolean = false) {
        if (!soundEnabled) {
            stopEngineSound()
            return
        }
        if (!isEngineRunning) {
            startEngineSound()
        }

        isBoostingEngine = isBoosting
        // F1 engine revs: 140Hz idle -> 750Hz base speed -> 1100Hz max boost scream!
        val baseHz = 140f
        val maxHz = if (isBoosting) 1150f else 850f
        targetFreq = baseHz + (maxHz - baseHz) * speedRatio.coerceIn(0f, 1f)
    }

    fun stopEngineSound() {
        isEngineRunning = false
        audioThread = null
    }

    fun playCoinSound() {
        if (!soundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 80)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playBoostSound() {
        if (!soundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_DTMF_D, 120)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playCrashSound() {
        stopEngineSound()
        if (!soundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 300)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        vibrateCrash()
    }

    fun playBrakeSound() {
        if (!soundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun vibrateTap() {
        if (!hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun vibrateCrash() {
        if (!hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(350, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(350)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

