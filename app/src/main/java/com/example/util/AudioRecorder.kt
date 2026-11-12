package com.example.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null
    var isRecording: Boolean = false
        private set

    fun startRecording(): File? {
        if (isRecording) return currentFile

        val outputDir = context.cacheDir
        val file = File(outputDir, "audiorecord_${System.currentTimeMillis()}.m4a")
        currentFile = file

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(file.absolutePath)

            try {
                prepare()
                start()
                isRecording = true
                Log.d("AudioRecorder", "Recording started: ${file.absolutePath}")
            } catch (e: IOException) {
                Log.e("AudioRecorder", "prepare() failed", e)
                release()
                recorder = null
                isRecording = false
                return null
            } catch (e: Exception) {
                Log.e("AudioRecorder", "start() failed", e)
                release()
                recorder = null
                isRecording = false
                return null
            }
        }
        return file
    }

    fun stopRecording(): File? {
        if (!isRecording) return null

        try {
            recorder?.stop()
        } catch (e: Exception) {
            Log.e("AudioRecorder", "stop() failed", e)
        } finally {
            recorder?.release()
            recorder = null
            isRecording = false
        }

        return currentFile
    }
}
