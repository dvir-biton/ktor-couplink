package com.fylora.plugins

import com.fylora.session.LoginManager
import com.fylora.session.session
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    loginManager: LoginManager
) {
    routing {
        session(loginManager)
    }
}
