package com.example.azarnumerico

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.azarnumerico.adapters.UserFireAdapter
import com.example.azarnumerico.adapters.UserFirebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RankingActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserFireAdapter
    private var userList: MutableList<UserFirebase> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        recyclerView = findViewById(R.id.RankigView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        userAdapter = UserFireAdapter(userList)
        recyclerView.adapter = userAdapter

        fetchUserData()
    }

    private fun fetchUserData() {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("users")

        usersRef.orderBy("coins", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    if (result != null && !result.isEmpty) {
                        userList.clear()
                        for (document in result) {
                            val user = document.toObject(UserFirebase::class.java)
                            Log.d("RankingActivity", "User: ${user.name}, Coins: ${user.coins}")
                            userList.add(user)
                        }
                        userAdapter.notifyDataSetChanged()
                    }
                } else {
                    Log.w("RankingActivity", "Error getting documents.", task.exception)
                }
            }
    }
}