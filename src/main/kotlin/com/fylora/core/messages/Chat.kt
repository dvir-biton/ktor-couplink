package com.fylora.core.messages

import com.fylora.auth.data.serializer.ObjectIdSerializer
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

typealias Username = String

@Serializable
data class Chat(
    val users: List<Username>,
    val messages: List<Message>,

    @Serializable(with = ObjectIdSerializer::class)
    val id: ObjectId
)
