package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

class CallAudioEngine(context: Context) {

  private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
  private var audioRecord: AudioRecord? = null
  private var audioTrack: AudioTrack? = null
  private var isStreaming = false
  private var isMuted = false
  private var isSpeakerOn = true
  private var loopbackJob: Job? = null

  private val sampleRate = 16000
  private val channelIn = AudioFormat.CHANNEL_IN_MONO
  private val channelOut = AudioFormat.CHANNEL_OUT_MONO
  private val audioEncoding = AudioFormat.ENCODING_PCM_16BIT
  private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelIn, audioEncoding).coerceAtLeast(1024)

  var onAudioLevel: ((Float) -> Unit)? = null

  fun startAudio(scope: CoroutineScope) {
    if (isStreaming) return
    try {
      audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
      audioManager.isSpeakerphoneOn = isSpeakerOn

      audioRecord = AudioRecord(
        MediaRecorder.AudioSource.VOICE_COMMUNICATION,
        sampleRate,
        channelIn,
        audioEncoding,
        bufferSize
      )

      val outBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelOut, audioEncoding).coerceAtLeast(1024)
      audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(audioEncoding)
            .setSampleRate(sampleRate)
            .setChannelMask(channelOut)
            .build()
        )
        .setBufferSizeInBytes(outBufferSize)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

      audioRecord?.startRecording()
      audioTrack?.play()
      isStreaming = true

      loopbackJob = scope.launch(Dispatchers.IO) {
        val audioData = ShortArray(bufferSize / 2)
        while (isActive && isStreaming) {
          val readSize = audioRecord?.read(audioData, 0, audioData.size) ?: 0
          if (readSize > 0) {
            // Calculate RMS / peak audio amplitude for animated visualizer
            var sum = 0L
            for (i in 0 until readSize) {
              sum += abs(audioData[i].toInt())
            }
            val avg = sum.toFloat() / readSize
            val normalizedLevel = (avg / 32767f).coerceIn(0f, 1f)
            onAudioLevel?.invoke(normalizedLevel)

            if (!isMuted) {
              // Write to speaker for communication feedback / stream
              audioTrack?.write(audioData, 0, readSize)
            }
          }
        }
      }
    } catch (e: Exception) {
      Log.w("CallAudioEngine", "Audio init note: ${e.message}")
    }
  }

  fun setMuted(muted: Boolean) {
    isMuted = muted
  }

  fun toggleSpeaker(): Boolean {
    isSpeakerOn = !isSpeakerOn
    audioManager.isSpeakerphoneOn = isSpeakerOn
    return isSpeakerOn
  }

  fun stopAudio() {
    isStreaming = false
    loopbackJob?.cancel()
    try {
      audioRecord?.stop()
      audioRecord?.release()
    } catch (e: Exception) {
      // Ignored
    }
    audioRecord = null

    try {
      audioTrack?.stop()
      audioTrack?.release()
    } catch (e: Exception) {
      // Ignored
    }
    audioTrack = null

    try {
      audioManager.mode = AudioManager.MODE_NORMAL
    } catch (e: Exception) {
      // Ignored
    }
  }
}
