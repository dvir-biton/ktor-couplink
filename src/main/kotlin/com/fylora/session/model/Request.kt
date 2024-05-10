package com.fylora.session.model

import com.fylora.core.messages.Message
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Request {
    @Serializable
    @SerialName("chat_request")
    data class ChatRequest(val chatId: String): Request

    @Serializable
    @SerialName("send_message")
    data class SendMessageRequest(val chatId: String, val message: Message): Request
}