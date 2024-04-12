package com.example.azarnumerico.adapters

import androidx.recyclerview.widget.RecyclerView
import model.User
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.azarnumerico.R

class ScoreAdapter(private var scores: MutableList<User>) : RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.score_view_item, parent, false)
        return ScoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        val user = scores[position]
        holder.bind(user)
    }

    override fun getItemCount() = scores.size

    class ScoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val coinsTextView: TextView = itemView.findViewById(R.id.coinsTextView)

        fun bind(user: User) {
            nameTextView.text = user.name
            coinsTextView.text = user.coins.toString()
        }
    }

    fun updateData(newScores: List<User>) {
        scores.clear()
        scores.addAll(newScores)
        notifyDataSetChanged()
    }
}