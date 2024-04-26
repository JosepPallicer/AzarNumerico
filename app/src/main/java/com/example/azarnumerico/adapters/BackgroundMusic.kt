package com.example.azarnumerico.adapters

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer

import android.os.IBinder
import com.example.azarnumerico.R

class BackgroundMusic : Service() {
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.main_menu_theme)
        mediaPlayer.isLooping = true

        SoundManager.soundEnabled.observeForever { isEnabled ->

            if (isEnabled) {
                if (!mediaPlayer.isPlaying) mediaPlayer.start()
            } else {
                if (mediaPlayer.isPlaying) mediaPlayer.pause()
            }

        }

    }


    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}