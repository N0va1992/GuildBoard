package com.example.guildboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(
    private var events: List<GameEvent>,
    private val onJoinClick: (GameEvent) -> Unit,
    private val onOpenChatClick: (GameEvent) -> Unit
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvGameTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvEventDescription)
        val tvDistanceAndSeats: TextView = itemView.findViewById(R.id.tvDistanceAndSeats)
        val btnJoin: Button = itemView.findViewById(R.id.btnJoinEvent)
        val btnOpenChat: Button = itemView.findViewById(R.id.btnOpenChat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event_card, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.tvTitle.text = event.title
        holder.tvDesc.text = event.description
        holder.tvDistanceAndSeats.text = "Odległość: ${event.distanceKm} km | Miejsca: ${event.currentSeats}/${event.maxSeats}"

        if (event.isJoined) {
            holder.btnJoin.text = "Prośba wysłana"
            holder.btnJoin.isEnabled = false
        } else {
            holder.btnJoin.text = "Wyślij prośbę (Join)"
            holder.btnJoin.isEnabled = true
        }

        holder.btnJoin.setOnClickListener {
            onJoinClick(event)
        }

        holder.btnOpenChat.setOnClickListener {
            onOpenChatClick(event)
        }
    }

    override fun getItemCount(): Int = events.size

    fun updateEvents(newEvents: List<GameEvent>) {
        events = newEvents
        notifyDataSetChanged()
    }
}