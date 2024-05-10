package com.fylora.session.model

import com.fylora.core.user.User
import io.ktor.websocket.*

data class OnlineUser(
    val user: User,
    val socket: WebSocketSession
)
