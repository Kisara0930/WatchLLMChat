package com.kisara0930.watchllmchat

import java.util.UUID

data class Message(
    val text: String,
    val author: String,
    val id: String = UUID.randomUUID().toString(),
    val isLoading: Boolean = false
)