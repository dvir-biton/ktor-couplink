package com.fylora.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val status: String
)
