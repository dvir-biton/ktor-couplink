package com.fylora.auth.routes.admin

import com.fylora.core.user.UserDataSource
import com.fylora.auth.security.hashing.HashingService
import com.fylora.core.logging.LogDataSource
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureAdminRouting(
    hashingService: HashingService,
    userDataSource: UserDataSource,
    logDataSource: LogDataSource
) {
    routing {
        adminSignup(hashingService, userDataSource)
        adminPanel(userDataSource, logDataSource)
    }
}