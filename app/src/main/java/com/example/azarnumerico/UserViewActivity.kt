package com.example.azarnumerico

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.azarnumerico.adapters.BackgroundMusic
import com.example.azarnumerico.adapters.MusicUtil
import com.example.azarnumerico.adapters.UserAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import openHelper.DatabaseHelper

class UserViewActivity : ComponentActivity() {

    private lateinit var userAdapter: UserAdapter
    private lateinit var usersView: RecyclerView
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_view)

        startService(Intent(this, BackgroundMusic::class.java))

        usersView = findViewById(R.id.usersView)
        usersView.layoutManager = LinearLayoutManager(this)

        val dbHelper = DatabaseHelper(this)
        compositeDisposable.add(
            dbHelper.getAllUsers()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ users ->
                    userAdapter = UserAdapter(users.toMutableList())
                    usersView.adapter = userAdapter
                }, { error ->
                    Toast.makeText(
                        this,
                        "Error al obtener los usuarios: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                })
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()

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