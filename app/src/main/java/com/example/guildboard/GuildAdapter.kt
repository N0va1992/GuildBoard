package com.example.guildboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GuildAdapter(
    private var guilds: List<Guild>,
    private val onDetailsClick: (Guild) -> Unit,
    private val onJoinClick: (Guild) -> Unit
) : RecyclerView.Adapter<GuildAdapter.GuildViewHolder>() {

    class GuildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvGuildName)
        val tvMeta: TextView = itemView.findViewById(R.id.tvGuildCityAndMembers)
        val tvDesc: TextView = itemView.findViewById(R.id.tvGuildDescription)
        val btnDetails: Button = itemView.findViewById(R.id.btnGuildDetails)
        val btnJoin: Button = itemView.findViewById(R.id.btnJoinGuild)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuildViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guild_card, parent, false)
        return GuildViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuildViewHolder, position: Int) {
        val guild = guilds[position]
        holder.tvName.text = guild.name
        holder.tvMeta.text = "Miasto: ${guild.city} | Członków: ${guild.memberCount}"
        holder.tvDesc.text = guild.description

        holder.btnDetails.setOnClickListener { onDetailsClick(guild) }
        holder.btnJoin.setOnClickListener { onJoinClick(guild) }
    }

    override fun getItemCount(): Int = guilds.size

    fun updateGuilds(newGuilds: List<Guild>) {
        guilds = newGuilds
        notifyDataSetChanged()
    }
}