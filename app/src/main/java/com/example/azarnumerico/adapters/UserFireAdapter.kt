package com.example.azarnumerico.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.azarnumerico.R

class UserFireAdapter(private val userList: List<UserFirebase>) : RecyclerView.Adapter<UserFireAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.googleName)
        val coinsTextView: TextView = view.findViewById(R.id.googleCoins)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.nameTextView.text = user.name
        holder.coinsTextView.text = user.coins.toString()
    }

    override fun getItemCount() = userList.size
}