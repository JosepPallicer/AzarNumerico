package com.example.azarnumerico

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.azarnumerico.adapters.BackgroundMusic
import java.util.Locale

class LanguageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_language)

        startService(Intent(this, BackgroundMusic::class.java))

        findViewById<Button>(R.id.englishSelect).setOnClickListener {
            updateLocale("en")
        }
        findViewById<Button>(R.id.spanishSelect).setOnClickListener {
            updateLocale("es")
        }
        findViewById<Button>(R.id.italianSelect).setOnClickListener {
            updateLocale("it")
        }
        findViewById<Button>(R.id.frenchSelect).setOnClickListener {
            updateLocale("fr")
        }
        findViewById<Button>(R.id.portugueseSelect).setOnClickListener {
            updateLocale("pt")
        }
    }

    private fun updateLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        val context: Context = createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)

        val refresh = Intent(context, MainActivity::class.java)
        refresh.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(refresh)
    }
}