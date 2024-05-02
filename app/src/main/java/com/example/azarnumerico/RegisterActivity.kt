package com.example.azarnumerico

import openHelper.DatabaseHelper
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.azarnumerico.adapters.BackgroundMusic
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable

class RegisterActivity : ComponentActivity() {

    private lateinit var dbHelper: DatabaseHelper

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.register_activity)

        startService(Intent(this, BackgroundMusic::class.java))



        dbHelper = DatabaseHelper(this)
        val registerConfirm = findViewById<Button>(R.id.confirmRegisterButton)

        registerConfirm.setOnClickListener {
            val username = findViewById<EditText>(R.id.userTV).text.toString().trim()
            val password = findViewById<EditText>(R.id.passwordTV).text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                addUser(username, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({

                        Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT)
                            .show()


                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }, { error ->

                        Toast.makeText(
                            this,
                            error.message ?: "Error al registrar el usuario.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }).let { compositeDisposable.add(it) }
            } else {
                Toast.makeText(
                    this,
                    "Por favor ingresa un usuario y una contraseña correctos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()

    }

    private fun addUser(username: String, password: String): Completable {

        return Completable.create { emitter ->

            val values = ContentValues().apply {

                put("name", username)
                put("password", password)
                put("coins", 100)

            }

            val newRowId = dbHelper.writableDatabase.insert("users", null, values)

            if (newRowId == -1L) {

                emitter.onError(Exception("Error al registrar el usuario"))

            } else {

                emitter.onComplete()

            }

        }


    }
}