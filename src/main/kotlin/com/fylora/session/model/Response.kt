package com.fylora.session.model

import com.fylora.core.messages.Chat
import com.fylora.core.messages.Message
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Response {
    @Serializable
    @SerialName("chat_response")
    data class ChatResponse(val chat: Chat): Response

    @Serializable
    @SerialName("message_response")
    data class MessageResponse(val message: Message): Response
}