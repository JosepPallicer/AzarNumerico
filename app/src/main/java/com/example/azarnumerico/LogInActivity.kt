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
import android.util.Log
import com.example.azarnumerico.adapters.BackgroundMusic
import com.example.azarnumerico.adapters.MusicUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import io.reactivex.rxjava3.kotlin.subscribeBy

class LogInActivity : ComponentActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private val compositeDisposable = CompositeDisposable()

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

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
                        onSuccess = { user ->
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
                            ).show()
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

        val googleSignIn = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignIn)

        auth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.googleButton).setOnClickListener {
            signIn()
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

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w("LogInActivity", "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w("LogInActivity", "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(this, "Has iniciado sesión correctamente", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Falló el inicio de sesión", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
