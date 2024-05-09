package com.fylora.session

import com.fylora.core.DatabaseSource.messageDataSource
import com.fylora.core.messages.Message
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.types.ObjectId

class MessageManager {
    suspend fun send(
        message: Message,
        to: OnlineUser,
        chatId: ObjectId
    ) {
        messageDataSource.save(chatId, message)

        to.socket.send(
            Frame.Text(
                Json.encodeToString(
                    message
                )
            )
        )
    }

    suspend fun getAllMessages(chatId: ObjectId): List<Message> {
        return messageDataSource.getAllMessages(chatId)?.messages ?: emptyList()
    }
}