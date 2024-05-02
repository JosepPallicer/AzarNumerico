package com.example.azarnumerico

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import openHelper.DatabaseHelper
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.example.azarnumerico.adapters.BackgroundMusic
import com.example.azarnumerico.adapters.MusicUtil
import io.reactivex.rxjava3.kotlin.subscribeBy

class LogInActivity : ComponentActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_in)

        startService(Intent(this, BackgroundMusic::class.java))

        dbHelper = DatabaseHelper(this)

        findViewById<Button>(R.id.confirmLogInButton).setOnClickListener {
            val username = findViewById<EditText>(R.id.logInName).text.toString().trim()
            val password = findViewById<EditText>(R.id.logInPassword).text.toString().trim()

            compositeDisposable.add(
                dbHelper.loginUser(username, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onSuccess = {user ->

                            Utility.UserSession.logIn(this, user.name, user.coins)

                            Toast.makeText(this, "Sesión iniciada", Toast.LENGTH_SHORT)
                                .show()


                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()

                        },
                        onComplete = {
                            Toast.makeText(
                                this,
                                "Usuario o contraseña incorrectos",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        },
                        onError = { error ->
                        Toast.makeText(

                            this,
                            ": ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    })
            )

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        sendMusicControlIntent("STOP")
        compositeDisposable.clear()
    }

    override fun onResume() {
        super.onResume()
        sendMusicControlIntent("START")
    }


    override fun onPause() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (MusicUtil.isAppInBackground(this)) {
                sendMusicControlIntent("STOP")
            }
        }, 250)
        super.onPause()
    }

    private fun sendMusicControlIntent(action: String) {
        Intent(this, BackgroundMusic::class.java).also { intent ->
            intent.action = action
            startService(intent)
        }
    }

}

