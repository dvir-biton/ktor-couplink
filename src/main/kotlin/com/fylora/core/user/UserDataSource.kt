package com.fylora.core.user

import org.bson.types.ObjectId

interface UserDataSource {
    suspend fun getUserByUsername(username: String): User?
    suspend fun getUserById(id: ObjectId): User?
    suspend fun insertUser(user: User): Boolean
    suspend fun getAllUsers(): List<User>
    suspend fun runCustomQuery(query: String): Boolean
    suspend fun updateUser(user: User): Boolean
}