package com.example.azarnumerico

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import openHelper.DatabaseHelper
import com.example.azarnumerico.adapters.BackgroundMusic
import com.example.azarnumerico.adapters.MusicUtil
import com.example.azarnumerico.adapters.SoundPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private var userCoins: Int = 0

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
        val multiPlayer = findViewById<Button>(R.id.multiPlayerButton)


        FirebaseApp.initializeApp(this)

        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        reBuyButton.isEnabled = false

        if (Utility.UserSession.isLoggedIn(this) || GoogleSignIn.getLastSignedInAccount(this) != null || auth.currentUser != null) {
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
        multiPlayer.setOnClickListener {

            val startMultiplayer = Intent(this, Multiplayer::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(startMultiplayer)
        }

    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        registerReceiver(musicStateReceiver, IntentFilter("MUSIC_STATE_CHANGED"), RECEIVER_NOT_EXPORTED)

        val shouldPlayMusic = SoundPreferences.isSoundOn(this)
        val isMusicServiceRunning = BackgroundMusic.isRunning

        if (shouldPlayMusic && !isMusicServiceRunning) {
            startService(Intent(this, BackgroundMusic::class.java))
        } else if (!shouldPlayMusic && isMusicServiceRunning) {
            stopService(Intent(this, BackgroundMusic::class.java))
        }
        updateLoginButton()
        updateUserInfo()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(musicStateReceiver)
        Handler(Looper.getMainLooper()).postDelayed({
            if (MusicUtil.isAppInBackground(this)) {
                sendMusicControlIntent("STOP")
            }
        }, 250)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (BackgroundMusic.isRunning) {
            stopService(Intent(this, BackgroundMusic::class.java))
        }
    }


    private fun updateLoginButton() {

        val reBuyButton = findViewById<Button>(R.id.rebuyButton)
        val buttonLogIn = findViewById<Button>(R.id.logInButton)
        if (Utility.UserSession.isLoggedIn(this) || GoogleSignIn.getLastSignedInAccount(this) != null || auth.currentUser != null) {
            buttonLogIn.text = getString(R.string.logout)
            buttonLogIn.setOnClickListener {
                logOutUser()
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

        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid
            val userRef = firestore.collection("users").document(uid)


            userRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val coins = document.getLong("coins")?.toInt() ?: 0
                    val name = document.getString("name") ?: currentUser.displayName
                    userCoins = coins
                    userView.text = name
                    coinsView.text = "$userCoins"
                } else {
                    userView.text = currentUser.displayName ?: getString(R.string.logInSessionUI)
                    coinsView.text = "0"
                    userCoins = 0
                }
            }.addOnFailureListener {
                userView.text = currentUser.displayName ?: getString(R.string.logInSessionUI)
                coinsView.text = "0"
                userCoins = 0
                Toast.makeText(this, "Error al obtener monedas", Toast.LENGTH_SHORT).show()
            }
        } else {
            userView.text = getString(R.string.logInSessionUI)
            userCoins = 0
            coinsView.text = "0"
        }
    }

    private fun updateReBuy() {

        val username = Utility.UserSession.getUserInfo(this)?.first ?: return

        val dbHelper = DatabaseHelper(this)
        dbHelper.updateUserCoins(username, 100)

        Utility.UserSession.updateUserCoins(this, 100)

        Toast.makeText(this, getString(R.string.coinsAdvice), Toast.LENGTH_SHORT).show()

    }

    private fun sendMusicControlIntent(action: String) {
        Intent(this, BackgroundMusic::class.java).also { intent ->
            intent.action = action
            startService(intent)
        }
    }

    private fun logOutUser() {

        googleSignInClient.signOut().addOnCompleteListener(this) {
            if (it.isSuccessful){
                Toast.makeText(this, "Sesión Cerrada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
            }
        }

        FirebaseAuth.getInstance().signOut()
        Utility.UserSession.logOut(this)

    }

}