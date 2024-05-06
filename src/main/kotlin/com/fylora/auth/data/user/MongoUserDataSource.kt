package com.fylora.auth.data.user

import com.fylora.core.user.User
import com.fylora.core.user.UserDataSource
import org.bson.Document
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoUserDataSource(
    private val db: CoroutineDatabase
): UserDataSource {

    private val users = db.getCollection<User>()

    override suspend fun getUserByUsername(username: String): User? {
        return users.findOne(User::username eq username)
    }

    override suspend fun getUserById(id: ObjectId): User? {
        return users.findOne(User::id eq id)
    }

    override suspend fun insertUser(user: User): Boolean {
        return users.insertOne(user).wasAcknowledged()
    }

    override suspend fun getAllUsers(): List<User> {
        return users.find().toList()
    }

    override suspend fun runCustomQuery(query: String): Boolean {
        return db.runCommand<Document>(Document.parse(query)) != null
    }

    override suspend fun updateUser(user: User): Boolean {
        return users.updateOneById(user.id, user).wasAcknowledged()
    }
}