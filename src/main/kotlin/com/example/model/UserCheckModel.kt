package com.example.model


import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

data class UserCheckModel @BsonCreator constructor(
    @BsonProperty("_id") val id: String? = null,
    @BsonProperty("username") val username: String,
    @BsonProperty("email") val email: String,
    @BsonProperty("password") val password: String
) {
    // Add a no-argument constructor if needed (optional)
    constructor() : this(null, "", "", "")
}

