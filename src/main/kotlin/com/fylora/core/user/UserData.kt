package com.fylora.core.user

import com.fylora.auth.data.serializer.ObjectIdSerializer
import com.fylora.domain.models.Event
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class UserData(
    @Serializable(with = ObjectIdSerializer::class)
    val partnerId: ObjectId? = null,
    val birthday: Long = 0,
    val anniversary: Long = 0,

    val dateHistory: List<Event> = emptyList(),
    val upcomingDates: List<Event> = emptyList(),
    val favoriteDates: List<Event> = emptyList(),
)
