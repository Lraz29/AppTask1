package com.example.apptask1

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build

class AudioManager(context: Context) {

    private val appContext = context.applicationContext

    // Background (long) sound
    private var bgPlayer: MediaPlayer? =
        MediaPlayer.create(appContext, R.raw.bg)?.apply {
            isLooping = true
            setVolume(0.25f, 0.25f)
        }

    // Short sounds (SoundPool)
    private val soundPool: SoundPool =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder().setMaxStreams(4).build()
        } else {
            @Suppress("DEPRECATION")
            SoundPool(4, android.media.AudioManager.STREAM_MUSIC, 0)
        }

    private val crashSoundId: Int = soundPool.load(appContext, R.raw.hit, 1)
    private val gameOverSoundId: Int = soundPool.load(appContext, R.raw.game_over, 1)

    // ✅ NEW: verify sound (make sure res/raw/verify.* exists)
    private val verifySoundId: Int = soundPool.load(appContext, R.raw.verify, 1)

    fun startBackground() {
        bgPlayer?.let { if (!it.isPlaying) it.start() }
    }

    fun pauseBackground() {
        bgPlayer?.let { if (it.isPlaying) it.pause() }
    }

    fun playCrash() {
        soundPool.play(crashSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun playGameOver() {
        soundPool.play(gameOverSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun playVerify() {
        soundPool.play(verifySoundId, 0.7f, 0.7f, 1, 0, 1f)
    }

    fun release() {
        bgPlayer?.release()
        bgPlayer = null
        soundPool.release()
    }
}
