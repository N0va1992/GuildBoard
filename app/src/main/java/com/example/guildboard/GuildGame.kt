package com.example.guildboard

data class GuildGame(
    val id: Long = 0,
    val guildId: Long,
    val gameTitle: String,
    val category: String = "Planszówki"
)