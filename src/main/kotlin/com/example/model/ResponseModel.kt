package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class ResponseModel<T>(
    val status: Boolean,
    val statusCode: Int,
    val message: String,
    val data: T?
)
