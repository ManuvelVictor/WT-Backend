package com.example.service

import com.example.model.UserCheckModel
import com.example.model.UserModel
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.Document
import org.bson.types.ObjectId
import org.mindrot.jbcrypt.BCrypt

class UserService(private val database: MongoDatabase) {

    private var collection: MongoCollection<Document> = if (!database.listCollectionNames().contains("users")) {
        database.createCollection("users")
        database.getCollection("users")
    } else {
        database.getCollection("users")
    }

    // Hash password
    private fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    // Verify password
    private fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(plainPassword, hashedPassword)
    }

    // Create new user (Sign Up)
    suspend fun signUp(user: UserModel): String = withContext(Dispatchers.IO) {
        // Hash the password before storing it
        val hashedUser = user.copy(password = hashPassword(user.password))
        val doc = hashedUser.toDocument()
        collection.insertOne(doc)
        doc["_id"].toString()
    }

    // Read user (Login)
    suspend fun login(username: String, password: String): Boolean = withContext(Dispatchers.IO) {
        // Find user by username
        val userDoc = collection.find(Filters.eq("username", username)).first()
        userDoc?.let {
            val user = UserModel.fromDocument(it)
            // Verify the password
            verifyPassword(password, user.password)
        } ?: false
    }

    // Update user details
    suspend fun update(id: String, user: UserModel): Document? = withContext(Dispatchers.IO) {
        collection.findOneAndReplace(Filters.eq("_id", ObjectId(id)), user.toDocument())
    }

    // Delete user
    suspend fun delete(id: String): Document? = withContext(Dispatchers.IO) {
        collection.findOneAndDelete(Filters.eq("_id", ObjectId(id)))
    }

    // Read a user by ID (optional, can be customized)
    suspend fun read(id: String): UserModel? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("_id", ObjectId(id))).first()?.let(UserModel::fromDocument)
    }

}