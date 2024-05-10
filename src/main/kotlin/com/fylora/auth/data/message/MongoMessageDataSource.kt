package com.fylora.auth.data.message

import com.fylora.core.messages.Chat
import com.fylora.core.messages.Message
import com.fylora.core.messages.MessageDataSource
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.push

class MongoMessageDataSource(
    db: CoroutineDatabase
): MessageDataSource {
    private val chats = db.getCollection<Chat>()

    override suspend fun save(chatId: ObjectId, message: Message): Boolean {
        return chats.updateOne(
            Chat::id eq chatId,
            push(Chat::messages, message)
        ).wasAcknowledged()
    }

    override suspend fun getChatById(chatId: ObjectId): Chat? {
        return chats.findOne(Chat::id eq chatId)
    }
}