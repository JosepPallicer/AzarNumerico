package com.example.azarnumerico.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.azarnumerico.R
import model.User

class UserAdapter(private val users: MutableList<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_view_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)

    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }



    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val passwordTextView: TextView = itemView.findViewById(R.id.passwordTextView)
        private val coinsTextView: TextView = itemView.findViewById(R.id.coinsTextView)

        fun bind(user: User) {
            nameTextView.text = user.name
            //passwordTextView.text = user.password
            coinsTextView.text = user.coins.toString()
        }
    }
}
