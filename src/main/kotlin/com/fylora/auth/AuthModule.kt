package com.fylora.auth

import com.fylora.auth.data.logging.MongoLogDataSource
import com.fylora.auth.data.user.MongoUserDataSource
import com.fylora.auth.routes.admin.configureAdminRouting
import com.fylora.auth.routes.configureAuthRouting
import com.fylora.auth.security.configureSecurity
import com.fylora.auth.security.hashing.SHA256HashingService
import com.fylora.auth.security.token.JwtTokenService
import com.fylora.auth.security.token.TokenConfig
import io.ktor.server.application.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

const val TOKEN_EXPIRATION_TIME = 2629746000L // 1 month

@Suppress("Unused")
fun Application.authModule() {
    val usersDbName = "users-database"
    val usersDb = KMongo.createClient(
        connectionString = "mongodb://localhost:27017"
    ).coroutine
        .getDatabase(usersDbName)
    val userDataSource = MongoUserDataSource(usersDb)

    val logDbName = "log-database"
    val logDb = KMongo.createClient(
        connectionString = "mongodb://localhost:27017"
    ).coroutine
        .getDatabase(logDbName)
    val logDataSource = MongoLogDataSource(logDb)

    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = TOKEN_EXPIRATION_TIME,
        secret = System.getenv("JWT_SECRET")
    )
    val hashingService = SHA256HashingService()

    configureSecurity(tokenConfig)
    configureAuthRouting(hashingService, userDataSource, tokenService, tokenConfig)
    configureAdminRouting(hashingService, userDataSource, logDataSource)
}