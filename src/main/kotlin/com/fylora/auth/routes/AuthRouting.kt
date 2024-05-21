package com.fylora.auth.routes

import com.fylora.auth.security.hashing.HashingService
import com.fylora.auth.security.token.TokenConfig
import com.fylora.auth.security.token.TokenService
import com.fylora.session.session
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureAuthRouting(
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    routing {
        session(hashingService, tokenService, tokenConfig)
        signUp(hashingService)
        authenticate()
        getUserInfo()
    }
}

