package com.example.routes

import com.example.model.LoginRequestModel
import com.example.model.ResponseModel
import com.example.model.UserCheckModel
import com.example.model.UserModel
import com.example.service.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userService: UserService) {

    // User sign up (Create user)
    post("/signup") {
        val signupData = call.receive<UserModel>()
        try {
            // Create new user
            val createdUser = userService.signUp(signupData)

            val response = ResponseModel(
                status = true,
                statusCode = 201,
                message = "User created successfully",
                data = createdUser // Ensure this returns the created user's data if needed
            )
            call.respond(HttpStatusCode.Created, response) // Responds with 201 Created
        } catch (e: Exception) {
            val errorResponse = ResponseModel(
                status = false,
                statusCode = 500,
                message = "User creation failed: ${e.message}",
                data = null
            )
            call.respond(HttpStatusCode.InternalServerError, errorResponse) // Responds with 500 Internal Server Error
        }
    }

    // User login
    post("/login") {
        try {
            val loginData = call.receive<LoginRequestModel>()
            val isLoggedIn = userService.login(loginData.username, loginData.password)

            val response = if (isLoggedIn) {
                ResponseModel(
                    status = true,
                    statusCode = 200,
                    message = "Logged in successfully",
                    data = null
                )
            } else {
                ResponseModel(
                    status = false,
                    statusCode = 400,
                    message = "Invalid credentials",
                    data = null
                )
            }
            call.respond(HttpStatusCode.OK, response)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

    // Get user by ID
    get("/users/{id}") {
        try {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ResponseModel(false, 400, "No ID found", null)
            )

            val user = userService.read(id)

            if (user != null) {
                val response = ResponseModel(
                    status = true,
                    statusCode = 200,
                    message = "User found",
                    data = user
                )
                call.respond(HttpStatusCode.OK, response)
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ResponseModel(false, 404, "User not found", null)
                )
            }
        } catch (e: Exception) {
            val errorResponse = ResponseModel(
                status = false,
                statusCode = 500,
                message = "Error fetching user: ${e.message}",
                data = null
            )
            call.respond(HttpStatusCode.InternalServerError, errorResponse)
        }
    }

    // Update user info
    put("/users/{id}") {
        try {
            val id = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                ResponseModel(false, 400, "No ID found", null)
            )

            val user = call.receive<UserModel>()
            val updatedUser = userService.update(id, user)

            if (updatedUser != null) {
                val response = ResponseModel(
                    status = true,
                    statusCode = 200,
                    message = "User updated successfully",
                    data = updatedUser
                )
                call.respond(HttpStatusCode.OK, response)
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ResponseModel(false, 404, "User not found", null)
                )
            }
        } catch (e: Exception) {
            val errorResponse = ResponseModel(
                status = false,
                statusCode = 500,
                message = "Error updating user: ${e.message}",
                data = null
            )
            call.respond(HttpStatusCode.InternalServerError, errorResponse)
        }
    }

    // Delete user
    delete("/users/{id}") {
        try {
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                ResponseModel(false, 400, "No ID found", null)
            )

            val deletedUser = userService.delete(id)

            if (deletedUser != null) {
                val response = ResponseModel(
                    status = true,
                    statusCode = 200,
                    message = "User deleted successfully",
                    data = null
                )
                call.respond(HttpStatusCode.OK, response)
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ResponseModel(false, 404, "User not found", null)
                )
            }
        } catch (e: Exception) {
            val errorResponse = ResponseModel(
                status = false,
                statusCode = 500,
                message = "Error deleting user: ${e.message}",
                data = null
            )
            call.respond(HttpStatusCode.InternalServerError, errorResponse)
        }
    }
}

