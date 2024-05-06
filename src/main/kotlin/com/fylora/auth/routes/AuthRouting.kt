package com.fylora.auth.routes

import com.fylora.core.user.UserDataSource
import com.fylora.auth.security.hashing.HashingService
import com.fylora.auth.security.token.TokenConfig
import com.fylora.auth.security.token.TokenService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureAuthRouting(
    hashingService: HashingService,
    userDataSource: UserDataSource,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    routing {
        login(hashingService, userDataSource, tokenService, tokenConfig)
        signUp(hashingService, userDataSource)
        authenticate()
        getUserInfo()
    }
}

