package com.example.azarnumerico

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.azarnumerico.adapters.BackgroundMusic
import com.example.azarnumerico.adapters.MusicUtil
import com.example.azarnumerico.adapters.SoundManager

class ConfigActivity : ComponentActivity() {

    private lateinit var muteButton: Button
    private lateinit var musicSelectButton: Button
    private lateinit var helpButton: Button
    private lateinit var languageButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_config)

        startService(Intent(this, BackgroundMusic::class.java))

        muteButton = findViewById(R.id.muteButton)
        muteButton.setOnClickListener {
            SoundManager.changeSound()
            updateMuteButtonText()
        }

        musicSelectButton = findViewById(R.id.musicSelectButton)
        musicSelectButton.setOnClickListener {
            sendMusicControlIntent("STOP")
            launchMusicPicker()
        }

        helpButton = findViewById(R.id.helpButton)
        helpButton.setOnClickListener {

            val startHelpActivity = Intent(this, HelpActivity::class.java)
            startActivity(startHelpActivity)
            sendMusicControlIntent("STOP")

        }

        languageButton = findViewById(R.id.languageButton)
        languageButton.setOnClickListener {

            val startLanguageActivity = Intent (this, LanguageActivity::class.java)
            startActivity(startLanguageActivity)

        }

    }

    private fun launchMusicPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        resultLauncher.launch(intent)
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    playMusicFromUri(uri)
                }
            }
        }

    private fun playMusicFromUri(uri: Uri) {
        val intent = Intent(this, BackgroundMusic::class.java).apply {
            action = "LOAD"
            putExtra("EXTRA_MUSIC_URI", uri.toString())
        }
        startService(intent)
    }

    private fun updateMuteButtonText() {
        SoundManager.soundEnabled.observe(this) { isEnabled ->
            muteButton.text = if (isEnabled) "Mute App" else "Unmute App"
        }
    }

    override fun onPause() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (MusicUtil.isAppInBackground(this)) {
                sendMusicControlIntent("STOP")
            }
        }, 250)
        super.onPause()
    }

    override fun onResume(){
        sendMusicControlIntent("START")
        super.onResume()
    }

    private fun sendMusicControlIntent(action: String) {
        Intent(this, BackgroundMusic::class.java).also { intent ->
            intent.action = action
            startService(intent)
        }
    }


}
