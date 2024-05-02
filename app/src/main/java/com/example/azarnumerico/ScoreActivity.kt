package com.example.azarnumerico

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.azarnumerico.adapters.BackgroundMusic
import com.example.azarnumerico.adapters.ScoreAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import openHelper.DatabaseHelper

class ScoreActivity : ComponentActivity() {

    private lateinit var scoresView: RecyclerView
    private lateinit var scoreAdapter: ScoreAdapter
    private val compositeDisposable = CompositeDisposable()
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        startService(Intent(this, BackgroundMusic::class.java))

        databaseHelper = DatabaseHelper(this)

        scoresView = findViewById(R.id.scoreView)
        scoresView.layoutManager = LinearLayoutManager(this)

        scoreAdapter = ScoreAdapter(mutableListOf())
        scoresView.adapter = scoreAdapter

        loadScoreUsers()
    }

    private fun loadScoreUsers() {
        compositeDisposable.add(databaseHelper.getUserScore()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ users ->
                scoreAdapter.updateData(users.toMutableList())
            }, { error ->
                Toast.makeText(
                    this,
                    "Error al cargar los usuarios: ${error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            })
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()

    }
}