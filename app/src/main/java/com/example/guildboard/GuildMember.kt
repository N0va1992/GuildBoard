package com.example.guildboard

data class GuildMember(
    val id: Long = 0,
    val guildId: Long,
    val userId: Long,
    val userNick: String,
    val role: String = "MEMBER", // "OWNER", "MODERATOR", "MEMBER"
    val status: String = "ACCEPTED" // "ACCEPTED", "PENDING"
)