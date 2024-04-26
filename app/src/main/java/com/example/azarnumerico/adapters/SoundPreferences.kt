package com.example.azarnumerico.adapters

import android.content.Context
import android.widget.Toast

object SoundPreferences {

        private const val PREFS_NAME = "SoundPreferences"
        private const val SOUND_KEY = "soundEnabled"

        private fun setSoundOn(context: Context, isEnabled: Boolean) {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putBoolean(SOUND_KEY, isEnabled)
                apply()
            }
        }

        fun isSoundOn(context: Context): Boolean {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return sharedPreferences.getBoolean(SOUND_KEY, true)
        }

        fun soundEnabled(context: Context) {
            val isEnabled = !isSoundOn(context)
            setSoundOn(context, isEnabled)
            Toast.makeText(context, "Sonido ${if (isEnabled) "activado" else "desactivado"}", Toast.LENGTH_SHORT).show()
        }


    }
