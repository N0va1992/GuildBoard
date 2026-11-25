package com.example.guildboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GuildGameAdapter(
    private var games: MutableList<GuildGame>
) : RecyclerView.Adapter<GuildGameAdapter.GuildGameViewHolder>() {

    class GuildGameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvGuildGameTitle)
        val tvCategory: TextView = itemView.findViewById(R.id.tvGuildGameCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuildGameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guild_game, parent, false)
        return GuildGameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuildGameViewHolder, position: Int) {
        val game = games[position]
        holder.tvTitle.text = game.gameTitle
        holder.tvCategory.text = game.category
    }

    override fun getItemCount(): Int = games.size

    fun addGame(game: GuildGame) {
        games.add(game)
        notifyItemInserted(games.size - 1)
    }

    fun updateGames(newGames: List<GuildGame>) {
        games = newGames.toMutableList()
        notifyDataSetChanged()
    }
}