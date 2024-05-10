package com.fylora.session

import com.fylora.auth.data.logging.Log
import com.fylora.core.DatabaseSource.logDataSource
import com.fylora.core.DatabaseSource.userDataSource
import com.fylora.core.logging.util.LogLevel
import com.fylora.domain.util.Result
import io.ktor.websocket.*
import org.bson.types.ObjectId

class LoginManager {
    private val onlineUsers: MutableList<OnlineUser> = mutableListOf()

    suspend fun connect(
        username: String,
        userId: ObjectId,
        socket: WebSocketSession
    ): Result<OnlineUser> {
        val isUserOnline = onlineUsers.any { it.user.id == userId }
        if (isUserOnline)
            return Result.Error("The user $username already logged in")

        val user = userDataSource.getUserById(userId)
            ?: return Result.Error("The user $username doesn't exist")
        val onlineUser = OnlineUser(user, socket)

        onlineUsers.add(onlineUser)

        logDataSource.addLog(
            Log(
                level = LogLevel.Info.type,
                message = "The user $username logged in",
                userId = userId.toString()
            )
        )

        return Result.Success(onlineUser)
    }

    suspend fun disconnect(onlineUser: OnlineUser): Result<String> {
        if (!onlineUsers.contains(onlineUser))
            return Result.Error("User isn't online")

        onlineUser.socket.close()
        onlineUsers.remove(onlineUser)
        logDataSource.addLog(
            Log(
                level = LogLevel.Info.type,
                message = "The user ${onlineUser.user.username} logged out",
                userId = onlineUser.user.username
            )
        )

        return Result.Success("The user ${onlineUser.user.username} logged out successfully")
    }

    fun getOnlineUser(username: String): OnlineUser? {
        return onlineUsers.find { it.user.username == username }
    }
}