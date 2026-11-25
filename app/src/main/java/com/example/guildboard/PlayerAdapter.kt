package com.example.guildboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlayerAdapter(
    private var players: List<PlayerProfile>,
    private val onInviteClick: (PlayerProfile) -> Unit
) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNick: TextView = itemView.findViewById(R.id.tvPlayerNick)
        val tvRole: TextView = itemView.findViewById(R.id.tvPlayerRole)
        val tvDistance: TextView = itemView.findViewById(R.id.tvPlayerDistance)
        val tvOwnedGames: TextView = itemView.findViewById(R.id.tvPlayerOwnedGames)
        val btnInvite: Button = itemView.findViewById(R.id.btnInvitePlayer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player_card, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = players[position]
        holder.tvNick.text = player.nickname
        holder.tvRole.text = player.roleAndLevel
        holder.tvDistance.text = player.distanceText
        holder.tvOwnedGames.text = player.ownedGamesText

        holder.btnInvite.setOnClickListener {
            onInviteClick(player)
        }
    }

    override fun getItemCount(): Int = players.size

    fun updatePlayers(newPlayers: List<PlayerProfile>) {
        players = newPlayers
        notifyDataSetChanged()
    }
}