package com.fylora.couplink

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val title: String,
    val description: String,
    val date: Long = System.currentTimeMillis()
)
