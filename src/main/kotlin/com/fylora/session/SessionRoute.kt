package com.fylora.session

import com.fylora.auth.data.logging.Log
import com.fylora.core.DatabaseSource.logDataSource
import com.fylora.core.logging.util.LogLevel
import com.fylora.domain.util.Result
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.consumeAsFlow
import org.bson.types.ObjectId

fun Route.session(
    loginManager: LoginManager
) {
    authenticate {
        webSocket("/connect") {
            val principal = call.principal<JWTPrincipal>()

            val userId = principal?.getClaim("userId", String::class)
            val username = principal?.getClaim("username", String::class)

            if(userId == null || username == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Authentication required"))
                return@webSocket
            }

            val result = loginManager.connect(
                username = username,
                userId = ObjectId(userId),
                socket = this
            )
            val user = result.data

            if (result is Result.Error || user == null) {
                close(
                    CloseReason(
                        CloseReason.Codes.INTERNAL_ERROR,
                        message = result.message ?: "Unknown error with the connection"
                    )
                )
                loginManager.disconnect(user ?: return@webSocket)
            }

            try {
                incoming.consumeAsFlow().collect { frame ->
                    if (frame is Frame.Text) {
                        val requestBody = frame.readText()


                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                logDataSource.addLog(
                    Log(
                        level = LogLevel.Error.type,
                        message = e.message ?: "Unknown error",
                        userId = userId
                    )
                )
            } finally {
                loginManager.disconnect(user)
            }
        }
    }
}