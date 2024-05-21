package com.fylora.session

import com.fylora.auth.data.logging.Log
import com.fylora.auth.requests.AuthRequest
import com.fylora.auth.requests.AuthResponse
import com.fylora.auth.security.hashing.HashingService
import com.fylora.auth.security.hashing.SaltedHash
import com.fylora.auth.security.token.TokenClaim
import com.fylora.auth.security.token.TokenConfig
import com.fylora.auth.security.token.TokenService
import com.fylora.core.DatabaseSource
import com.fylora.core.DatabaseSource.logDataSource
import com.fylora.core.handlers.ErrorResponse
import com.fylora.core.logging.util.LogLevel
import com.fylora.domain.util.Result
import com.fylora.session.Managers.loginManager
import com.fylora.session.Managers.messageManager
import com.fylora.session.model.OnlineUser
import com.fylora.session.model.Request
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.types.ObjectId

fun Route.session(
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    authenticate {
        webSocket("/login") {
            val authRequest = call.receiveNullable<AuthRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@webSocket
            }

            val user = DatabaseSource.userDataSource.getUserByUsername(authRequest.username)
            if(user == null) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse("Incorrect username or password")
                )
                return@webSocket
            }

            val isValidPassword = hashingService.verify(
                value = authRequest.password,
                saltedHash = SaltedHash(
                    hash = user.password,
                    salt = user.salt
                )
            )
            if(!isValidPassword) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse("Incorrect username or password")
                )
                return@webSocket
            }

            val token = tokenService.generate(
                config = tokenConfig,
                TokenClaim(
                    name = "userId",
                    value = user.id.toString()
                ),
                TokenClaim(
                    name = "username",
                    value = user.username
                )
            )
            call.respond(
                status = HttpStatusCode.OK,
                message = AuthResponse(
                    token = token
                )
            )

            val result = loginManager.connect(
                username = user.username,
                userId = user.id,
                socket = this
            )
            val onlineUser = result.data

            if (result is Result.Error || onlineUser == null) {
                close(
                    CloseReason(
                        CloseReason.Codes.INTERNAL_ERROR,
                        message = result.message ?: "Unknown error with the connection"
                    )
                )
                if (onlineUser != null)
                    loginManager.disconnect(onlineUser)
                return@webSocket
            }
            
            try {
                incoming.consumeAsFlow().collect { frame ->
                    if (frame is Frame.Text) {
                        val requestBody = frame.readText()

                        when (val request = Json.decodeFromString<Request>(requestBody)) {
                            is Request.ChatRequest -> {
                                onlineUser.socket.send(
                                    Frame.Text(
                                        Json.encodeToString(
                                            messageManager.getChat(
                                                ObjectId(request.chatId)
                                            )
                                        )
                                    )
                                )
                            }
                            is Request.SendMessageRequest -> {
                                val chat = messageManager.getChat(ObjectId(request.chatId))

                                val onlineUsers = mutableListOf<OnlineUser?>()
                                chat?.users?.forEach { username ->
                                    onlineUsers.add(loginManager.getOnlineUser(username))
                                }

                                messageManager.send(
                                    request.message,
                                    onlineUsers,
                                    ObjectId(request.chatId)
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                logDataSource.addLog(
                    Log(
                        level = LogLevel.Error.type,
                        message = e.message ?: "Unknown error",
                        userId = user.id.toString()
                    )
                )
            } finally {
                loginManager.disconnect(onlineUser)
            }
        }
    }
}