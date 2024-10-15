package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestModel(
    val username: String,
    val password: String
)

