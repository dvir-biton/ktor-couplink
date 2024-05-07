package com.fylora.core.user

import com.fylora.auth.data.serializer.ObjectIdSerializer
import com.fylora.couplink.models.Event
import com.fylora.couplink.models.Message
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class UserData(
    @Serializable(with = ObjectIdSerializer::class)
    val partnerId: ObjectId?,
    val birthday: Long,
    val anniversary: Long,

    val dateHistory: List<Event> = emptyList(),
    val upcomingDates: List<Event> = emptyList(),
    val favoriteDates: List<Event> = emptyList(),

    val messages: List<Message> = emptyList()
)
