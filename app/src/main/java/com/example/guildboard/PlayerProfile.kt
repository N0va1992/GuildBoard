package com.example.guildboard

data class PlayerProfile(
    val id: String,
    val nickname: String,
    val roleAndLevel: String,
    val distanceText: String,
    val ownedGamesText: String
)