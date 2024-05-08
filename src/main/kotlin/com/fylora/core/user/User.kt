package com.fylora.core.user

import com.fylora.auth.data.serializer.ObjectIdSerializer
import io.ktor.server.auth.*
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class User(
    val username: String,
    val password: String,
    val salt: String,
    val role: String,
    val data: UserData,

    @Serializable(with = ObjectIdSerializer::class)
    @BsonId
    val id: ObjectId = ObjectId()
): Principal
