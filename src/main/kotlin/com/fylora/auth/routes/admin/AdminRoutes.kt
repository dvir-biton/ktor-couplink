package com.fylora.auth.routes.admin

import com.fylora.auth.data.user.UserRole
import com.fylora.auth.requests.admin.AdminRequest
import com.fylora.auth.requests.admin.AdminSignUpRequest
import com.fylora.auth.requests.admin.util.AdminAction
import com.fylora.auth.routes.MAX_USERNAME_LENGTH
import com.fylora.auth.security.hashing.HashingService
import com.fylora.core.handlers.ErrorResponse
import com.fylora.core.logging.LogDataSource
import com.fylora.core.user.User
import com.fylora.core.user.UserDataSource
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.types.ObjectId

val ADMIN_SIGNUP_ROUTE: String = System.getenv("ADMIN_SIGNUP_ROUTE")
val ADMIN_PANEL_ROUTE: String = System.getenv("ADMIN_PANEL_ROUTE")
val ADMIN_TOKEN: String = System.getenv("ADMIN_TOKEN")

fun Route.adminSignup(
    hashingService: HashingService,
    userDataSource: UserDataSource
) {
    post(ADMIN_SIGNUP_ROUTE) {
        val request = call.receiveNullable<AdminSignUpRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        if(request.adminToken != ADMIN_TOKEN) {
            call.respond(
                HttpStatusCode.Forbidden,
                message = ErrorResponse("Wrong admin token, aren't you admin? ;)")
            )
            return@post
        }
        if(request.username.length > MAX_USERNAME_LENGTH) {
            call.respond(
                HttpStatusCode.Conflict,
                message = ErrorResponse("The username cannot be more than $MAX_USERNAME_LENGTH characters (Max database length)")
            )
            return@post
        }
        if(userDataSource.getUserByUsername(request.username) != null) {
            call.respond(
                HttpStatusCode.Conflict,
                message = ErrorResponse("The username is already taken, to avoid conflicts it's recommended to choose a different username")
            )
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt,
            role = UserRole.Admin.type,
            data = null
        )

        val wasAcknowledged = userDataSource.insertUser(user)
        if(!wasAcknowledged) {
            call.respond(
                HttpStatusCode.Conflict,
                message = ErrorResponse("Unknown error occurred, Couldn't insert user")
            )
            return@post
        }

        call.respond(HttpStatusCode.OK)
    }
}

fun Route.adminPanel(
    userDataSource: UserDataSource,
    logDataSource: LogDataSource,
) {
    authenticate {
        post(ADMIN_PANEL_ROUTE) {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class) ?: kotlin.run {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    message = ErrorResponse("No userId found")
                )
                return@post
            }
            val user = userDataSource.getUserById(ObjectId(userId)) ?: kotlin.run {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    message = ErrorResponse("No user found")
                )
                return@post
            }

            if(UserRole.fromType(user.role) != UserRole.Admin) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    message = ErrorResponse("You are not an admin")
                )
                return@post
            }

            val request = call.receiveNullable<AdminRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            when(val action = request.action) {
                is AdminAction.CreateUser -> {
                    val result = userDataSource.insertUser(
                        user = action.user
                    )

                    if(!result) {
                        call.respond(
                            HttpStatusCode.Conflict,
                            message = ErrorResponse("Unknown error occurred, Couldn't create user")
                        )
                        return@post
                    }
                    call.respond(HttpStatusCode.OK)
                }
                is AdminAction.RunSqlQuery -> {
                    if("DROP" in action.query || "DELETE" in action.query) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            message = ErrorResponse("No permission to make such action, please use the local database connection to make this action")
                        )
                        return@post
                    }

                    val result = userDataSource.runCustomQuery(action.query)
                    call.respond(HttpStatusCode.OK, Json.encodeToString(result))
                }
                is AdminAction.UpdateUser -> {
                    val result = userDataSource.updateUser(action.user)

                    if(!result) {
                        call.respond(
                            HttpStatusCode.Conflict,
                            message = ErrorResponse("Unknown error occurred, Couldn't create user")
                        )
                        return@post
                    }
                    call.respond(HttpStatusCode.OK)
                }
                is AdminAction.ViewLogs -> {
                    val result = logDataSource.getLastLogs(action.count)

                    call.respond(HttpStatusCode.OK, Json.encodeToString(result))
                }
                is AdminAction.GetUserById -> {
                    val result = userDataSource.getUserById(ObjectId(action.id))

                    call.respond(HttpStatusCode.OK, Json.encodeToString(result))
                }
                is AdminAction.GetUserByUsername -> {
                    val result = userDataSource.getUserByUsername(action.username)

                    call.respond(HttpStatusCode.OK, Json.encodeToString(result))
                }
            }
        }
    }
}