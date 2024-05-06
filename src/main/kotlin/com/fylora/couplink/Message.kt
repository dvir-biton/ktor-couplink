package com.fylora.couplink

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val status: String
)
