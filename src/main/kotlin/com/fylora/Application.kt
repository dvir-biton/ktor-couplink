package com.fylora

import com.fylora.auth.authModule
import com.fylora.auth.data.message.MongoMessageDataSource
import com.fylora.core.DatabaseSource
import com.fylora.plugins.configureMonitoring
import com.fylora.plugins.configureRouting
import com.fylora.plugins.configureSerialization
import com.fylora.session.LoginManager
import io.ktor.server.application.*
import io.ktor.server.websocket.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    install(WebSockets)
    authModule()

    val chatsDbName = "chat-database"
    val usersDb = KMongo.createClient(
        connectionString = "mongodb://localhost:27017"
    ).coroutine
        .getDatabase(chatsDbName)
    DatabaseSource.messageDataSource = MongoMessageDataSource(usersDb)

    val loginManager = LoginManager()
    configureRouting(loginManager)
    configureSerialization()
    configureMonitoring()
}
