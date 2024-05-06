package com.fylora.auth.data.logging

import com.fylora.core.logging.LogDataSource
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.descending

class MongoLogDataSource(
    db: CoroutineDatabase
): LogDataSource {
    private val logs = db.getCollection<Log>()

    override suspend fun addLog(log: Log): Boolean {
        return logs.insertOne(log).wasAcknowledged()
    }

    override suspend fun getLastLogs(count: Int): List<Log> {
        return logs.find()
            .limit(count)
            .sort(descending(Log::timestamp))
            .toList()
    }

    override suspend fun clear(): Boolean {
        return logs.deleteMany("{}").wasAcknowledged()
    }
}