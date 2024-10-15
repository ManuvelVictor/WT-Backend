package com.example

import com.example.database.MongoDBClient
import com.example.routes.userRoutes
import com.example.service.UserService
import kotlinx.serialization.json.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    val mongoDatabase = MongoDBClient.getDatabase(this)
    val userService = UserService(mongoDatabase)

    routing {
        userRoutes(userService)
    }
}