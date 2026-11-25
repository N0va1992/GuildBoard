package com.example.guildboard

data class ChatMessage(
    val senderName: String,
    val time: String,
    val messageText: String,
    val isSystem: Boolean = false
)