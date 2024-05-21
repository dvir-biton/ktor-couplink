package com.fylora.auth.routes

import com.fylora.auth.data.user.UserRole
import com.fylora.auth.requests.SignupRequest
import com.fylora.auth.security.hashing.HashingService
import com.fylora.core.DatabaseSource.userDataSource
import com.fylora.core.handlers.ErrorResponse
import com.fylora.core.handlers.InfoResponse
import com.fylora.core.user.User
import com.fylora.core.user.UserData
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

const val MAX_USERNAME_LENGTH = 24
const val MIN_USERNAME_LENGTH = 3

fun Route.signUp(
    hashingService: HashingService
) {
    post("signup") {
        val request = call.receiveNullable<SignupRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        if(request.username.length < MIN_USERNAME_LENGTH) {
            call.respond(
                HttpStatusCode.Conflict,
                message = ErrorResponse("The username cannot be less than $MIN_USERNAME_LENGTH characters")
            )
            return@post
        }
        if(request.username.length > MAX_USERNAME_LENGTH) {
            call.respond(
                HttpStatusCode.Conflict,
                message = ErrorResponse("The username cannot be more than $MAX_USERNAME_LENGTH characters")
            )
            return@post
        }
        if(userDataSource.getUserByUsername(request.username) != null) {
            call.respond(
                HttpStatusCode.Conflict,
                message = ErrorResponse("The username is already taken")
            )
            return@post
        }
        val checkPassword = isStrongPassword(request.password)
        if(!checkPassword.first) {
            call.respond(
                HttpStatusCode.Conflict,
                message = ErrorResponse(checkPassword.second)
            )
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt,
            role = UserRole.User.type,
            data = UserData(
                partnerId = request.partnerId,
                birthday = request.birthday,
                anniversary = request.anniversary,
            )
        )
        if (request.partnerId != null) {
            val partner = userDataSource.getUserById(request.partnerId)
            if (partner == null) {
                call.respond(
                    HttpStatusCode.Conflict,
                    message = ErrorResponse("The partner does not exist")
                )
                return@post
            }
            if (partner.data.partnerId == null) {
                userDataSource.updateUser(
                    partner.copy(
                        data = partner.data.copy(
                            partnerId = user.id
                        )
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.Conflict,
                    message = ErrorResponse("The partner is already taken")
                )
                return@post
            }
        }

        val wasAcknowledged = userDataSource.insertUser(user)
        if(!wasAcknowledged) {
            call.respond(
                HttpStatusCode.Conflict,
                message = ErrorResponse("Unknown error occurred")
            )
            return@post
        }

        call.respond(HttpStatusCode.OK)
    }
}

fun Route.authenticate() {
    authenticate {
        get("authenticate") {
            call.respond(
                HttpStatusCode.OK
            )
        }
    }
}

fun Route.getUserInfo() {
    authenticate {
        get("info") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            val username = principal?.getClaim("username", String::class)
            call.respond(
                HttpStatusCode.OK,
                InfoResponse(
                    "id: $userId\n" +
                    "username: $username"
                )
            )
        }
    }
}

fun isStrongPassword(password: String): Pair<Boolean, String> {
    val minLength = 8
    val hasUpperCase = password.any {
        it.isUpperCase()
    }
    val hasLowerCase = password.any {
        it.isLowerCase()
    }
    val hasDigit = password.any {
        it.isDigit()
    }
    val hasSpecialChar = password.any {
        it.isLetterOrDigit().not()
    }
    val requirements = listOf(
        "at least $minLength characters" to (password.length >= minLength),
        "an uppercase letter" to hasUpperCase,
        "a lowercase letter" to hasLowerCase,
        "a digit" to hasDigit,
        "a special character" to hasSpecialChar
    )
    val missingRequirements = requirements.filterNot { it.second }.map { it.first }

    return if (missingRequirements.isEmpty()) {
        Pair(true, "Your password is strong")
    } else {
        if(missingRequirements.size == 1) {
            Pair(
                false,
                "Your password also need to contain ${missingRequirements.lastOrNull() ?: ""}"
            )
        } else {
            Pair(
                false,
                "Your password also need to contain ${missingRequirements.dropLast(1).joinToString(", ")}, and ${missingRequirements.lastOrNull() ?: ""}."
            )
        }
    }
}