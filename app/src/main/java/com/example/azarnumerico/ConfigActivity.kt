package com.example.azarnumerico

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.azarnumerico.adapters.BackgroundMusic
import com.example.azarnumerico.adapters.SoundPreferences

class ConfigActivity : ComponentActivity() {

    private lateinit var muteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_config)

        startService(Intent(this, BackgroundMusic::class.java))

        muteButton = findViewById(R.id.muteButton)
        updateMuteButtonText()

        muteButton.setOnClickListener {
            SoundPreferences.soundEnabled(this)
            val intent = Intent("MUSIC_STATE_CHANGED")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            updateMuteButtonText()
        }

    }

    private fun updateMuteButtonText() {
        muteButton.text = if (SoundPreferences.isSoundOn(this)) {
            "Mutear App"
        } else {
            "Desmutear App"
        }
    }

}