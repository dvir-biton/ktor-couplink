package com.fylora.auth.data.logging

import com.fylora.auth.data.serializer.ObjectIdSerializer
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class Log(
    val timestamp: Long = System.currentTimeMillis(),
    val level: String,
    val message: String,

    val userId: String?,
    @Serializable(with = ObjectIdSerializer::class)
    @BsonId
    val id: ObjectId = ObjectId()
)
