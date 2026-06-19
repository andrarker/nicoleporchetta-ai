package com.example

import java.util.UUID

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" o "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val model: String = "",
    val imageUrl: String? = null,
    val isLoading: Boolean = false
)
