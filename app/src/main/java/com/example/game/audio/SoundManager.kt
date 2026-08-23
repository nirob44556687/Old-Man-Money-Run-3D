package com.example.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var musicJob: Job? = null

    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = true
    var chaserTension: Float = 0f // 0f (calm) to 1f (extreme panic)

    private val sampleRate = 44100

    private fun playPcm(buffer: ShortArray, volume: Float = 1.0f) {
        if (!isSoundEnabled) return
        scope.launch {
            try {
                val minBufSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minBufSize, buffer.size * 2)

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.setVolume(volume.coerceIn(0f, 1f))
                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                // Calculate duration and release
                val durationMs = (buffer.size.toDouble() / sampleRate * 1000).toLong() + 50
                delay(durationMs)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (e: Exception) {
                    // Ignore release exceptions
                }
            } catch (e: Exception) {
                Log.d("SoundManager", "Error playing sound: ${e.message}")
            }
        }
    }

    // Sound FX Generators:
    fun playJump() {
        // Upward frequency sweep (boing!)
        val duration = 0.18f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = i.toFloat() / numSamples
            val freq = 220f + 650f * progress
            val amp = (1f - progress) * 0.8f
            val sample = (sin(2.0 * PI * freq * t) * amp * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer, 0.7f)
    }

    fun playSlide() {
        // Friction white noise swoosh
        val duration = 0.22f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        var last = 0f
        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val white = (Math.random() * 2 - 1).toFloat()
            // Low-pass filter for whoosh
            last = last * 0.7f + white * 0.3f
            val amp = sin(progress * PI.toFloat()) * 0.6f
            val sample = (last * amp * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer, 0.6f)
    }

    fun playCoin() {
        // High sparkling dual-tone chime (880Hz to 1760Hz)
        val duration = 0.14f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = i.toFloat() / numSamples
            val freq = if (progress < 0.5f) 987.77f else 1318.51f // B5 to E6
            val amp = exp(-progress * 5.0).toFloat() * 0.7f
            val sample = (sin(2.0 * PI * freq * t) * amp * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer, 0.6f)
    }

    fun playMoney() {
        // Crisp high-pitch arpeggio (E6, G#6, B6, E7) - rustle & cash reward
        val duration = 0.25f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        val freqs = floatArrayOf(1318f, 1661f, 1975f, 2637f)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val segment = (i.toFloat() / numSamples * freqs.size).toInt().coerceIn(0, freqs.size - 1)
            val freq = freqs[segment]
            val decay = exp(-((i % (numSamples / freqs.size)).toFloat() / (numSamples / freqs.size) * 4f)).toFloat()
            val amp = decay * 0.75f
            val sample = ((sin(2.0 * PI * freq * t) + 0.3 * sin(4.0 * PI * freq * t)) * amp * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer, 0.75f)
    }

    fun playPowerUp() {
        // Fast energetic rising fanfare
        val duration = 0.35f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        val notes = floatArrayOf(523f, 659f, 784f, 1046f, 1318f)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val noteIdx = (i.toFloat() / numSamples * notes.size).toInt().coerceIn(0, notes.size - 1)
            val freq = notes[noteIdx]
            val progress = i.toFloat() / numSamples
            val amp = (1f - progress * 0.4f) * 0.8f
            val sample = (sin(2.0 * PI * freq * t) * amp * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer, 0.8f)
    }

    fun playCrash() {
        // Heavy cartoon bonk & explosion
        val duration = 0.3f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = i.toFloat() / numSamples
            val freq = 160f * (1f - progress * 0.7f)
            val noise = (Math.random() * 2 - 1).toFloat() * (1f - progress)
            val tone = sin(2.0 * PI * freq * t).toFloat()
            val amp = (1f - progress) * 0.9f
            val sample = ((tone * 0.6f + noise * 0.4f) * amp * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer, 0.9f)
    }

    fun playShieldHit() {
        // Sci-fi shield absorption pulse
        val duration = 0.25f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = i.toFloat() / numSamples
            val freq = 800f + 400f * sin(progress * 15f)
            val amp = (1f - progress) * 0.75f
            val sample = (sin(2.0 * PI * freq * t) * amp * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer, 0.75f)
    }

    fun playWomanShout() {
        // Funny cartoon angry siren / shout tone
        val duration = 0.3f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = i.toFloat() / numSamples
            val freq = 440f + 220f * sin(progress * 20f)
            val amp = (1f - progress * 0.5f) * 0.8f
            val sample = (sin(2.0 * PI * freq * t) * amp * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer, 0.7f)
    }

    fun playCatchGameOver() {
        // Dramatic cartoon trombone slide down (womp womp womp waaah)
        val duration = 1.1f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = i.toFloat() / numSamples
            val noteProgress = progress * 4f
            val baseFreq = when (noteProgress.toInt()) {
                0 -> 466.16f // Bb4
                1 -> 440.00f // A4
                2 -> 415.30f // Ab4
                else -> 370f * (1f - (progress - 0.75f) * 1.5f) // G4 down pitch slide
            }
            val amp = (1f - (progress % 0.25f) * 2f).coerceIn(0f, 1f) * 0.8f
            // Square wave harmonics for comic brass sound
            val tone = (sin(2.0 * PI * baseFreq * t) + 0.3 * sin(6.0 * PI * baseFreq * t)).toFloat()
            val sample = (tone * amp * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer, 0.85f)
    }

    fun playLevelComplete() {
        // Joyful victory fanfare
        val duration = 0.9f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f, 1318.51f, 1567.98f)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = i.toFloat() / numSamples
            val noteIdx = (progress * notes.size).toInt().coerceIn(0, notes.size - 1)
            val freq = notes[noteIdx]
            val subProg = (progress * notes.size) % 1f
            val amp = (1f - subProg * 0.6f) * 0.85f
            val sample = (sin(2.0 * PI * freq * t) * amp * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer, 0.8f)
    }

    fun playButtonClick() {
        val duration = 0.05f
        val numSamples = (duration * sampleRate).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = i.toFloat() / numSamples
            val freq = 600f + 400f * (1f - progress)
            val amp = (1f - progress) * 0.5f
            val sample = (sin(2.0 * PI * freq * t) * amp * Short.MAX_VALUE).toInt()
            buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(buffer, 0.5f)
    }

    // Background Cartoon Music Engine
    fun startBgm() {
        if (musicJob != null && musicJob?.isActive == true) return
        musicJob = scope.launch {
            // A catchy, comedic Bengali/Desi running melody pattern
            val melodyNotes = floatArrayOf(
                261.63f, 329.63f, 392.00f, 523.25f, 392.00f, 329.63f, 293.66f, 349.23f,
                392.00f, 440.00f, 392.00f, 349.23f, 329.63f, 261.63f, 293.66f, 246.94f
            )
            val bassNotes = floatArrayOf(
                130.81f, 130.81f, 164.81f, 164.81f, 146.83f, 146.83f, 174.61f, 174.61f
            )

            var step = 0
            while (isActive) {
                if (!isMusicEnabled) {
                    delay(300)
                    continue
                }

                // Dynamic speed: when chaser is close, tempo speeds up!
                val baseTempoMs = (150 - (chaserTension * 55)).toLong().coerceIn(80L, 160L)
                val melFreq = melodyNotes[step % melodyNotes.size]
                val bassFreq = bassNotes[(step / 2) % bassNotes.size]

                // Generate short note
                val durationSec = baseTempoMs / 1000f * 0.9f
                val numSamples = (durationSec * sampleRate).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toFloat() / sampleRate
                    val progress = i.toFloat() / numSamples
                    val amp = (1f - progress * 0.7f) * 0.22f

                    // Lead + Bass + Percussion tap
                    val lead = sin(2.0 * PI * melFreq * t)
                    val bass = sin(2.0 * PI * bassFreq * t) * 0.7
                    val perc = if (step % 2 == 0 && progress < 0.1f) (Math.random() * 2 - 1) * 0.3 else 0.0
                    val mixed = (lead * 0.5 + bass * 0.3 + perc) * amp * Short.MAX_VALUE
                    buffer[i] = mixed.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playPcm(buffer, 0.45f)
                step++
                delay(baseTempoMs)
            }
        }
    }

    fun stopBgm() {
        musicJob?.cancel()
        musicJob = null
    }

    fun release() {
        stopBgm()
    }
}
