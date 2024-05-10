package com.fylora.core.messages

import org.bson.types.ObjectId

interface MessageDataSource {
    suspend fun save(chatId: ObjectId, message: Message): Boolean
    suspend fun getChatById(chatId: ObjectId): Chat?
}