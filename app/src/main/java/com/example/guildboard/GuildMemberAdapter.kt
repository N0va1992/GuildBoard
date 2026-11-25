package com.example.guildboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GuildMemberAdapter(
    private var members: List<GuildMember>,
    private val currentUserRole: String, // "OWNER", "MODERATOR", "MEMBER"
    private val currentUserId: Long,
    private val onActionClick: (GuildMember, View) -> Unit
) : RecyclerView.Adapter<GuildMemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNick: TextView = itemView.findViewById(R.id.tvMemberNick)
        val tvRoleStatus: TextView = itemView.findViewById(R.id.tvMemberRoleAndStatus)
        val btnAction: Button = itemView.findViewById(R.id.btnMemberAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_guild_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.tvNick.text = member.userNick

        val roleLabel = when (member.role) {
            "OWNER" -> "Właściciel"
            "MODERATOR" -> "Moderator"
            else -> "Członek"
        }

        val statusLabel = if (member.status == "ACCEPTED") "Zaakceptowany" else "Oczekujący na akceptację"
        holder.tvRoleStatus.text = "Rola: $roleLabel | Status: $statusLabel"

        // Weryfikacja uprawnień do przycisku opcji
        val canPerformAction = when (currentUserRole) {
            "OWNER" -> member.userId != currentUserId
            "MODERATOR" -> member.role == "MEMBER" || member.status == "PENDING"
            else -> false
        }

        if (canPerformAction) {
            holder.btnAction.visibility = View.VISIBLE
            holder.btnAction.setOnClickListener { view ->
                onActionClick(member, view)
            }
        } else {
            holder.btnAction.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = members.size

    fun updateMembers(newMembers: List<GuildMember>) {
        members = newMembers
        notifyDataSetChanged()
    }
}