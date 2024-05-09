package com.fylora.session.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Request {
    @Serializable
    @SerialName("chat_request")
    data class ChatRequest(val chatId: String): Request
}