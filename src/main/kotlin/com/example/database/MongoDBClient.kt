package com.example.database

import com.mongodb.MongoClientSettings
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider

object MongoDBClient {
    private var mongoDatabase: MongoDatabase? = null

    fun getDatabase(application: Application): MongoDatabase {
        return mongoDatabase ?: synchronized(this) {
            mongoDatabase ?: connectToMongoDB(application).also { mongoDatabase = it }
        }
    }

    private fun connectToMongoDB(application: Application): MongoDatabase {
        val user = application.environment.config.tryGetString("db.mongo.user")
        val password = application.environment.config.tryGetString("db.mongo.password")
        val host = application.environment.config.tryGetString("db.mongo.host") ?: "127.0.0.1"
        val port = application.environment.config.tryGetString("db.mongo.port")?.toInt() ?: 27017
        val maxPoolSize = application.environment.config.tryGetString("db.mongo.maxPoolSize")?.toInt() ?: 20
        val databaseName = application.environment.config.tryGetString("db.mongo.database.name") ?: "myDatabase"

        // Setup MongoCredential (if user and password exist)
        val credential = if (user != null && password != null) {
            MongoCredential.createCredential(user, databaseName, password.toCharArray())
        } else null

        // Create MongoClientSettings with host, port, and optional credentials
        val codecRegistry = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()) // Handles Kotlin data classes
        )

        val settingsBuilder = MongoClientSettings.builder()
            .applyToClusterSettings { it.hosts(listOf(ServerAddress(host, port))) }
            .codecRegistry(codecRegistry)
            .applyToConnectionPoolSettings { it.maxSize(maxPoolSize) }

        credential?.let {
            settingsBuilder.credential(it)
        }

        val settings = settingsBuilder.build()
        val mongoClient = MongoClients.create(settings)
        val database = mongoClient.getDatabase(databaseName)

        // Close MongoClient when the application stops
        application.environment.monitor.subscribe(ApplicationStopped) {
            mongoClient.close()
        }

        // Check if "users" collection exists, if not, create it
        val collectionNames = database.listCollectionNames().toList()
        if (!collectionNames.contains("users")) {
            database.createCollection("users")
        }

        return database
    }
}