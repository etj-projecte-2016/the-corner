package com.example.thecorner.ui.training.session

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper

/** One player per session ViewModel, with no Fragment or Activity references. */
internal class WorkoutSessionAudio(context: Context) {
    enum class Cue(val resourceName: String) {
        COUNTDOWN("boxing_countdown_beep"),
        ROUND_END("boxing_round_start")
    }

    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val pool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build())
        .build()
    private val samples = mutableMapOf<Cue, Int>()
    private val pending = mutableSetOf<Int>()
    private val loaded = mutableSetOf<Int>()
    private var onReady: (() -> Unit)? = null
    private var released = false
    private var active = false
    private var stream = 0
    private val loadTimeout = Runnable { finishLoading() }

    @SuppressLint("DiscouragedApi") // Audio files are optional until supplied in res/raw.
    fun load(onReady: () -> Unit) {
        this.onReady = onReady
        pool.setOnLoadCompleteListener { _, sample, status ->
            handler.post {
                if (!released) {
                    pending.remove(sample)
                    if (status == 0) loaded.add(sample)
                    if (pending.isEmpty()) finishLoading()
                }
            }
        }
        for (cue in Cue.entries) {
            val resource = appContext.resources.getIdentifier(cue.resourceName, "raw", appContext.packageName)
            if (resource == 0) continue
            val sample = runCatching { pool.load(appContext, resource, 1) }.getOrDefault(0)
            if (sample != 0) {
                samples[cue] = sample
                pending.add(sample)
            }
        }
        // Never block the workout indefinitely on missing/invalid audio.
        if (pending.isEmpty()) finishLoading() else handler.postDelayed(loadTimeout, 1_500L)
    }

    private fun finishLoading() {
        handler.removeCallbacks(loadTimeout)
        val callback = onReady
        onReady = null
        if (!released) callback?.invoke()
    }

    fun setActive(active: Boolean) {
        this.active = active
        if (!active) stop()
    }

    fun play(cue: Cue) {
        if (released || !active) return
        val sample = samples[cue] ?: return
        if (sample !in loaded) return // Do not queue stale cues for later playback.
        stop()
        stream = pool.play(sample, 1f, 1f, 1, 0, 1f)
    }

    fun stop() {
        if (!released && stream != 0) pool.stop(stream)
        stream = 0
    }

    fun release() {
        if (released) return
        stop()
        released = true
        onReady = null
        handler.removeCallbacksAndMessages(null)
        pool.setOnLoadCompleteListener(null)
        pool.release()
    }
}
