package com.example.guildboard

data class User(
    val id: Long = 0,
    val nickname: String,
    val email: String,
    val password: String,
    val roleAndLevel: String = "Gracz • Początkujący",
    val city: String = "Kraków",
    val distanceText: String = "Kraków (1.0 km)",
    val ownedGamesText: String = "",
    val gamesPlayed: Int = 0,
    val tablesOrganized: Int = 0,
    val rating: Double = 0.0
)