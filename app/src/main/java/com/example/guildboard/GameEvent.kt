package com.example.guildboard

data class GameEvent(
    val id: String,
    val title: String,
    val description: String,
    val distanceKm: Double,
    val currentSeats: Int,
    val maxSeats: Int,
    val category: String,
    var isJoined: Boolean = false,
    val organizerId: Long = 1,
    val organizerNick: String = "Jan_Gracz",
    val city: String = "",
    val address: String = "",
    val startsAt: String = "",
    val userCommitment: String = "",
    val userEventRole: String = ""
)