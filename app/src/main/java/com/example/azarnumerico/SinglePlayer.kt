package com.example.azarnumerico

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.azarnumerico.adapters.BackgroundMusic
import openHelper.DatabaseHelper

class SinglePlayer : ComponentActivity() {

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

    private var bet1: Int = 0
    private var bet2: Int = 0
    private var bet3: Int = 0
    private var bet4: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_single_player)

        betClock = findViewById(R.id.betClock)
        startGameButton = findViewById(R.id.startGameButton)
        numGenerated = findViewById(R.id.randomNumber)
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

        betView1 = findViewById(R.id.bet1)
        betView2 = findViewById(R.id.bet2)
        betView3 = findViewById(R.id.bet3)
        betView4 = findViewById(R.id.bet4)



        enableBetButtons(false)
        checkCoinsState()
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

    private var userCoins: Int = 0


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

            1 -> {
                bet1 = userCoins
                updateBetUI(betView1, bet1)
            }

            2 -> {
                bet2 = userCoins
                updateBetUI(betView2, bet2)
            }

            3 -> {
                bet3 = userCoins
                updateBetUI(betView3, bet3)
            }

            4 -> {
                bet4 = userCoins
                updateBetUI(betView4, bet4)
            }

        }

        updateTotalBetUI()

    }

    private fun betPlus10(group: Int) {

        if (userCoins >= 10) {
            when (group) {

                1 -> {
                    bet1 += 10
                    updateBetUI(betView1, bet1)
                    updateTotalBetUI()
                }

                2 -> {
                    bet2 += 10
                    updateBetUI(betView2, bet2)
                    updateTotalBetUI()
                }

                3 -> {
                    bet3 += 10
                    updateBetUI(betView3, bet3)
                    updateTotalBetUI()
                }

                4 -> {
                    bet4 += 10
                    updateBetUI(betView4, bet4)
                    updateTotalBetUI()
                }

            }
            updateTotalBetUI()

        }


    }

    private fun updateBetUI(betView: TextView, bet: Int) {

        betView.text = bet.toString()

    }

    private fun updateTotalBetUI() {

        val totalBet = bet1 + bet2 + bet3 + bet4
        val betView = findViewById<TextView>(R.id.betView)
        betView.text = "$totalBet"

    }

    @SuppressLint("SetTextI18n")
    private fun startGame() {

        startGameButton.text = "STOP"
        timerRunning = true
        enableBetButtons(true)

        countDownTimer = object : CountDownTimer(20000, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                betClock.text = (millisUntilFinished / 1000).toString()

            }

            override fun onFinish() {

                val randomNum = (1..99).random()


                numGenerated.text = randomNum.toString()

                handleResult(randomNum)

                resetTimer()
            }

        }.start()

    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        startGameButton.text = "START"
        timerRunning = false
        betClock.text = "0"
        enableBetButtons(false)

        updateAllCoins(userCoins)
        updateCoinsUI()
        resetBets()
    }


    @SuppressLint("SetTextI18n")
    private fun resetTimer() {
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
        stopService(Intent(this, BackgroundMusic::class.java))
        updateUserInfo()
        checkCoinsState()
    }

    override fun onPause() {
        super.onPause()
        if (!isFinishing) {
            startService(Intent(this, BackgroundMusic::class.java))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUserInfo() {

        val userView = findViewById<TextView>(R.id.userName)
        val coinsView = findViewById<TextView>(R.id.coinsView)

        Utility.UserSession.getUserInfo(this)?.let { userInfo ->

            userView.text = userInfo.first
            userCoins = userInfo.second
            coinsView.text = "$userCoins"

        } ?: run {
            userView.text = "Inicia Sesión"
            userCoins = 0
            coinsView.text = "0"
        }

        checkCoinsState()

    }

    private fun updateCoinsUI() {

        val coinsView = findViewById<TextView>(R.id.coinsView)
        coinsView.text = "$userCoins"
        checkCoinsState()

    }

    private fun checkCoinsState() {

        val startGameButton = findViewById<Button>(R.id.startGameButton)

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

        if (profits > 0){
            Toast.makeText(this, "Enhorabuena! Has ganado $profits monedas!", Toast.LENGTH_LONG).show()
        }

        userCoins += profits

        updateAllCoins(userCoins)
        updateCoinsUI()
        resetBets()

    }

    private fun updateAllCoins(newCoins: Int) {

        Utility.UserSession.updateUserCoins(this, newCoins)

        val username = Utility.UserSession.getUserInfo(this)?.first ?: return

        val dbHelper = DatabaseHelper(this)
        dbHelper.updateUserCoins(username, newCoins)

    }


}