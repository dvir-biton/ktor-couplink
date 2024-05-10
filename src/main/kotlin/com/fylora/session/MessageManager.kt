package com.fylora.session

import com.fylora.core.DatabaseSource.messageDataSource
import com.fylora.core.messages.Chat
import com.fylora.core.messages.Message
import com.fylora.session.model.OnlineUser
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.types.ObjectId

class MessageManager {
    suspend fun send(
        message: Message,
        to: List<OnlineUser?>,
        chatId: ObjectId
    ) {
        messageDataSource.save(chatId, message)

        to.forEach { client ->
            client?.socket?.send(
                Frame.Text(
                    Json.encodeToString(
                        message
                    )
                )
            )
        }
    }

    suspend fun getChat(chatId: ObjectId): Chat? {
        return messageDataSource.getChatById(chatId)
    }
}