package com.fylora

import com.fylora.auth.authModule
import com.fylora.plugins.configureMonitoring
import com.fylora.plugins.configureRouting
import com.fylora.plugins.configureSerialization
import com.fylora.session.LoginManager
import io.ktor.server.application.*
import io.ktor.server.websocket.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    authModule()
    install(WebSockets)

    val loginManager = LoginManager()
    configureRouting(loginManager)
    configureSerialization()
    configureMonitoring()
}
