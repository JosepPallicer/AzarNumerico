package com.example.azarnumerico


import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.azarnumerico.adapters.BackgroundMusic
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Multiplayer : ComponentActivity() {

    private lateinit var betClock: TextView
    private lateinit var startGameButton: Button
    private lateinit var numGenerated: TextView
    private var countDownTimer: CountDownTimer? = null
    private var timerRunning = false
    private lateinit var betButtons: List<Button>
    private lateinit var betView1: TextView
    private lateinit var betView2: TextView
    private lateinit var betView3: TextView
    private lateinit var betView4: TextView
    private lateinit var tensionSound: MediaPlayer
    private lateinit var victorySound: MediaPlayer
    private lateinit var lossSound: MediaPlayer


    private var bet1: Int = 0
    private var bet2: Int = 0
    private var bet3: Int = 0
    private var bet4: Int = 0

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var userCoins: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_multi_player)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        betClock = findViewById(R.id.multiplayerBetClock)
        startGameButton = findViewById(R.id.multiplayerStartGameButton)
        numGenerated = findViewById(R.id.multiplayerRandomNumber)
        betButtons = listOf(
            findViewById(R.id.allinButton1),
            findViewById(R.id.allinButton2),
            findViewById(R.id.allinButton3),
            findViewById(R.id.allinButton4),
            findViewById(R.id.bet10Button1),
            findViewById(R.id.bet10Button2),
            findViewById(R.id.bet10Button3),
            findViewById(R.id.bet10Button4),
        )

        betView1 = findViewById(R.id.multiplayerBet1)
        betView2 = findViewById(R.id.multiplayerBet2)
        betView3 = findViewById(R.id.multiplayerBet3)
        betView4 = findViewById(R.id.multiplayerBet4)

        startService(Intent(this, BackgroundMusic::class.java))
        initializeSounds()

        enableBetButtons(false)
        updateUserInfo()
        setupBetButtons()

        startGameButton.setOnClickListener {
            if (timerRunning) {
                stopTimer()
            } else {
                numGenerated.text = "0"
                startGame()
            }
        }

    }

    private fun initializeSounds() {
        tensionSound = MediaPlayer.create(this, R.raw.tension_apuesta)
        victorySound = MediaPlayer.create(this, R.raw.victory_sound)
        lossSound = MediaPlayer.create(this, R.raw.violin_perdida)
    }

    @SuppressLint("SetTextI18n")
    private fun updateUserInfo() {
        val userView = findViewById<TextView>(R.id.multiplayerUserName)
        val coinsView = findViewById<TextView>(R.id.multiplayerCoinsView)

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

    private fun setupBetButtons() {
        findViewById<Button>(R.id.allinButton1).setOnClickListener { betAllIn(1) }
        findViewById<Button>(R.id.bet10Button1).setOnClickListener { betPlus10(1) }
        findViewById<Button>(R.id.allinButton2).setOnClickListener { betAllIn(2) }
        findViewById<Button>(R.id.bet10Button2).setOnClickListener { betPlus10(2) }
        findViewById<Button>(R.id.allinButton3).setOnClickListener { betAllIn(3) }
        findViewById<Button>(R.id.bet10Button3).setOnClickListener { betPlus10(3) }
        findViewById<Button>(R.id.allinButton4).setOnClickListener { betAllIn(4) }
        findViewById<Button>(R.id.bet10Button4).setOnClickListener { betPlus10(4) }
    }

    private fun betAllIn(group: Int) {
        when (group) {
            1 -> { bet1 = userCoins; updateBetUI(betView1, bet1) }
            2 -> { bet2 = userCoins; updateBetUI(betView2, bet2) }
            3 -> { bet3 = userCoins; updateBetUI(betView3, bet3) }
            4 -> { bet4 = userCoins; updateBetUI(betView4, bet4) }
        }
        updateTotalBetUI()
    }

    private fun betPlus10(group: Int) {
        if (userCoins >= 10) {
            when (group) {
                1 -> { bet1 += 10; updateBetUI(betView1, bet1) }
                2 -> { bet2 += 10; updateBetUI(betView2, bet2) }
                3 -> { bet3 += 10; updateBetUI(betView3, bet3) }
                4 -> { bet4 += 10; updateBetUI(betView4, bet4) }
            }
            updateTotalBetUI()
        }
    }

    private fun updateBetUI(betView: TextView, bet: Int) {
        betView.text = bet.toString()
    }

    private fun updateTotalBetUI() {
        val totalBet = bet1 + bet2 + bet3 + bet4
        val betView = findViewById<TextView>(R.id.multiplayerBetView)
        betView.text = "$totalBet"
    }

    private fun startGame() {
        if (!timerRunning) {
            startGameButton.text = "STOP"
            timerRunning = true
            enableBetButtons(true)
            sendMusicControlIntent("PAUSE")
            tensionSound.start()
            tensionSound.setOnCompletionListener {
                if (!timerRunning) { sendMusicControlIntent("PLAY") }
            }

            countDownTimer = object : CountDownTimer(20000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    betClock.text = (millisUntilFinished / 1000).toString()
                }

                override fun onFinish() {
                    runOnUiThread {
                        val randomNum = (1..99).random()
                        numGenerated.text = randomNum.toString()
                        handleResult(randomNum)
                        resetTimer()
                    }
                }
            }.start()
        } else { stopTimer() }
    }

    private fun stopTimer() {
        timerRunning = false
        countDownTimer?.cancel()
        tensionSound.stop()
        tensionSound.prepare()
        sendMusicControlIntent("PLAY")
        startGameButton.text = "START"
        enableBetButtons(false)

        updateAllCoins(userCoins)
        updateCoinsUI()
        resetBets()
    }

    @SuppressLint("SetTextI18n")
    private fun resetTimer() {
        if (!tensionSound.isPlaying) { sendMusicControlIntent("PLAY") }
        tensionSound.pause()
        tensionSound.seekTo(0)
        startGameButton.text = "START"
        timerRunning = false
        betClock.text = "0"
        enableBetButtons(false)
    }

    private fun enableBetButtons(enable: Boolean) {
        betButtons.forEach { it.isEnabled = enable }
    }

    override fun onResume() {
        super.onResume()
        updateUserInfo()
    }

    override fun onDestroy() {
        super.onDestroy()
        tensionSound.release()
        victorySound.release()
        lossSound.release()
    }

    @SuppressLint("SetTextI18n")
    private fun updateCoinsUI() {
        val coinsView = findViewById<TextView>(R.id.multiplayerCoinsView)
        coinsView.text = "$userCoins"
        checkCoinsState()
    }

    private fun checkCoinsState() {
        val startGameButton = findViewById<Button>(R.id.multiplayerStartGameButton)
        startGameButton.isEnabled = userCoins > 0
    }

    private fun resetBets() {
        bet1 = 0
        bet2 = 0
        bet3 = 0
        bet4 = 0

        updateBetUI(betView1, bet1)
        updateBetUI(betView2, bet2)
        updateBetUI(betView3, bet3)
        updateBetUI(betView4, bet4)
        updateTotalBetUI()
    }

    private fun winningGroup(randomNum: Int): Int {
        return when (randomNum) {
            in 1..25 -> 1
            in 26..50 -> 2
            in 51..75 -> 3
            in 76..99 -> 4
            else -> 0
        }
    }

    private fun handleResult(randomNum: Int) {
        val winningGroup = winningGroup(randomNum)
        val totalBet = bet1 + bet2 + bet3 + bet4
        var profits = 0

        userCoins -= totalBet

        when (winningGroup) {
            1 -> profits = bet1 * 4
            2 -> profits = bet2 * 4
            3 -> profits = bet3 * 4
            4 -> profits = bet4 * 4
        }

        if (profits > 0) {
            victorySound.start()
            Toast.makeText(this, "Enhorabuena! Has ganado $profits monedas!", Toast.LENGTH_LONG).show()
            victorySound.setOnCompletionListener { sendMusicControlIntent("PLAY") }
        } else {
            lossSound.start()
            Toast.makeText(this, "Has perdido tus monedas!", Toast.LENGTH_LONG).show()
            lossSound.setOnCompletionListener { sendMusicControlIntent("PLAY") }
        }

        userCoins += profits

        updateAllCoins(userCoins)
        updateCoinsUI()
        resetBets()
    }

    private fun updateAllCoins(newCoins: Int) {
        val user = auth.currentUser
        user?.let {
            val userId = it.uid
            val userRef = firestore.collection("users").document(userId)
            userRef.update("coins", newCoins)

                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar monedas: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun sendMusicControlIntent(action: String) {
        Intent(this, BackgroundMusic::class.java).also { intent ->
            intent.action = action
            startService(intent)
        }
    }


}