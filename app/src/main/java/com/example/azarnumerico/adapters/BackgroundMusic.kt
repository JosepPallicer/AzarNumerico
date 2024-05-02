package com.example.azarnumerico.adapters

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri

import android.os.IBinder
import com.example.azarnumerico.R

class BackgroundMusic : Service() {
    private lateinit var mediaPlayer: MediaPlayer

    companion object {
        @Volatile
        var isRunning: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.casino_music)
        mediaPlayer.isLooping = true

        SoundManager.soundEnabled.observeForever { isEnabled ->

            if (isEnabled) {
                if (!mediaPlayer.isPlaying) mediaPlayer.start()
            } else {
                if (mediaPlayer.isPlaying) mediaPlayer.pause()
            }

        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                "PLAY" -> {
                    if (!mediaPlayer.isPlaying) {
                        mediaPlayer.start()
                    }
                }
                "PAUSE" -> {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                    }
                }
                "STOP" -> stopSelf()
                "LOAD" -> {
                    val uri = intent.getStringExtra("EXTRA_MUSIC_URI")
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(this, Uri.parse(uri))
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                }
            }
        }
        isRunning = true
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }


    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
        isRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}