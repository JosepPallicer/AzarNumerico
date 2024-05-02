package com.example.azarnumerico

import android.content.Context

class Utility {

    object UserSession {
        private const val PREFS_NAME = "userSessionPrefs"
        private const val LOGGED_IN = "loggedIn"
        private const val USERNAME = "username"
        private const val COINS = "coins"

        fun isLoggedIn(context: Context): Boolean {

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(LOGGED_IN, false)

        }

        fun logIn(context: Context, username: String, coins: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            with(prefs.edit()) {
                putBoolean(LOGGED_IN, true)
                putString(USERNAME, username)
                putInt(COINS, coins)
                apply()
            }
        }

        fun logOut(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            with(prefs.edit()) {
                putBoolean(LOGGED_IN, false)
                putString(USERNAME, "")
                putInt(COINS, 0)
                apply()
            }
        }

        fun getUserInfo(context: Context): Pair<String, Int>? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            if (!prefs.getBoolean(LOGGED_IN, false)) {
                return null
            }
            val username = prefs.getString(USERNAME, "") ?: ""
            val coins = prefs.getInt(COINS, 0)
            return Pair(username, coins)
        }

        fun updateUserCoins(context: Context, coins: Int) {

            val prefs = context.getSharedPreferences(UserSession.PREFS_NAME, Context.MODE_PRIVATE)
            with(prefs.edit()) {

                putInt(UserSession.COINS, coins)
                apply()
            }
        }
    }

}