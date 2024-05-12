package com.fylora.auth.requests

import com.fylora.auth.data.serializer.ObjectIdSerializer
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class SignupRequest(
    val username: String,
    val password: String,

    @Serializable(with = ObjectIdSerializer::class)
    val partnerId: ObjectId?,
    val birthday: Long,
    val anniversary: Long
)
