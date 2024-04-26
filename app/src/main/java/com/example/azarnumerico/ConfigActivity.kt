package com.example.azarnumerico

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.azarnumerico.adapters.BackgroundMusic
import com.example.azarnumerico.adapters.SoundManager
import com.example.azarnumerico.adapters.SoundPreferences

class ConfigActivity : ComponentActivity() {

    private lateinit var muteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_config)

        startService(Intent(this, BackgroundMusic::class.java))

        muteButton = findViewById(R.id.muteButton)
        muteButton.setOnClickListener {
            SoundManager.changeSound()
        }

        SoundManager.soundEnabled.observe(this) { isEnabled ->
            muteButton.text = if (isEnabled) "Mutear app" else "Desmutear"

        }

    }

}