package com.example.azarnumerico

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import openHelper.DatabaseHelper
import com.example.azarnumerico.adapters.BackgroundMusic
import com.example.azarnumerico.adapters.SoundPreferences

class MainActivity : ComponentActivity() {

    private val musicStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SoundPreferences.isSoundOn(context)) {
                startService(Intent(context, BackgroundMusic::class.java))
            } else {
                stopService(Intent(context, BackgroundMusic::class.java))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val buttonStartSinglePlayer = findViewById<Button>(R.id.singlePlayerButton)
        val buttonRegister = findViewById<Button>(R.id.registerButton)
        val buttonUsersView = findViewById<Button>(R.id.usersButton)
        val buttonLogIn = findViewById<Button>(R.id.logInButton)
        val reBuyButton = findViewById<Button>(R.id.rebuyButton)
        val scoreButton = findViewById<Button>(R.id.scoreButton)
        val configButton = findViewById<Button>(R.id.configButton)

        startService(Intent(this, BackgroundMusic::class.java))

        reBuyButton.isEnabled = false

        if (Utility.UserSession.isLoggedIn(this)) {
            reBuyButton.isEnabled = true
        }

        reBuyButton.setOnClickListener {

            val dbHelper = DatabaseHelper(this)
            val userInfo = Utility.UserSession.getUserInfo(this)

            userInfo?.let { info ->
                val username = info.first
                val userId = dbHelper.getUserId(username)

                userId?.let { id ->
                    dbHelper.insertReBuy(id)
                    dbHelper.updateUserCoins(username, 100)
                }

                updateReBuy()
                updateUserInfo()
            } ?: run {
                Toast.makeText(this, "Usuario no logueado.", Toast.LENGTH_SHORT).show()
            }

            updateReBuy()
            updateUserInfo()
        }

        buttonStartSinglePlayer.setOnClickListener {

            val startSinglePlayer = Intent(this, SinglePlayer::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(startSinglePlayer)
        }

        buttonRegister.setOnClickListener {

            val registerUser = Intent(this, RegisterActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(registerUser)
        }

        buttonUsersView.setOnClickListener {

            val usersView = Intent(this, UserViewActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(usersView)
        }

        scoreButton.setOnClickListener {

            val scoreView = Intent(this, ScoreActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(scoreView)
        }

        buttonLogIn.setOnClickListener {

            val logIn = Intent(this, LogInActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(logIn)
        }

        configButton.setOnClickListener {

            val configuration = Intent(this, ConfigActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(configuration)
        }

    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("MUSIC_STATE_CHANGED")
        registerReceiver(musicStateReceiver, filter, RECEIVER_NOT_EXPORTED)
        if (SoundPreferences.isSoundOn(this)) {
            startService(Intent(this, BackgroundMusic::class.java))
        } else {
            stopService(Intent(this, BackgroundMusic::class.java))
        }
        updateLoginButton()
        updateUserInfo()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(musicStateReceiver)
    }

    private fun updateLoginButton() {

        val reBuyButton = findViewById<Button>(R.id.rebuyButton)
        val buttonLogIn = findViewById<Button>(R.id.logInButton)
        if (Utility.UserSession.isLoggedIn(this)) {
            buttonLogIn.text = getString(R.string.logout)
            buttonLogIn.setOnClickListener {
                Utility.UserSession.logOut(this)
                reBuyButton.isEnabled = false
                updateLoginButton()
                updateUserInfo()
            }
        } else {
            buttonLogIn.text = getString(R.string.logInButton)
            buttonLogIn.setOnClickListener {
                val intent = Intent(this, LogInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun updateUserInfo() {
        val userView = findViewById<TextView>(R.id.userView)
        val coinsView = findViewById<TextView>(R.id.moneyView)

        Utility.UserSession.getUserInfo(this)?.let { userInfo ->
            userView.text = userInfo.first
            coinsView.text = "${userInfo.second}"
        } ?: run {
            userView.text = "Inicia Sesión"
            coinsView.text = "0"
        }
    }


    private fun updateReBuy() {

        val username = Utility.UserSession.getUserInfo(this)?.first ?: return

        val dbHelper = DatabaseHelper(this)
        dbHelper.updateUserCoins(username, 100)

        Utility.UserSession.updateUserCoins(this, 100)

        Toast.makeText(this, "Monedas recompradas con éxito", Toast.LENGTH_SHORT).show()

    }

}