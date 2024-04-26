package com.example.azarnumerico.adapters

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.azarnumerico.R

class BackgroundMusic : Service() {
    private lateinit var mediaPlayer: MediaPlayer

    private val musicStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SoundPreferences.isSoundOn(context)) {
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                } else {
                    if (mediaPlayer.isPlaying)
                        mediaPlayer.pause()
                }

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.main_menu_theme)
        mediaPlayer.isLooping = true
        if (mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
        val filter = IntentFilter("MUSIC_STATE_CHANGED")
        LocalBroadcastManager.getInstance(this).registerReceiver(musicStateReceiver, filter)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(musicStateReceiver)
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}