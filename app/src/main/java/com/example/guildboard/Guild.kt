package com.example.guildboard

data class Guild(
    val id: Long = 0,
    val name: String,
    val description: String,
    val ownerId: Long,
    val ownerNick: String,
    val city: String,
    val requiresApproval: Boolean = false,
    val memberCount: Int = 1
)