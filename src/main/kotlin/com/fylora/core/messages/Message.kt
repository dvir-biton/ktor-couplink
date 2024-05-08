package com.fylora.core.messages

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val status: String,
    val sender: String
)
