package com.fylora.core.logging

import com.fylora.auth.data.logging.Log

interface LogDataSource {
    suspend fun addLog(log: Log): Boolean
    suspend fun getLastLogs(count: Int): List<Log>
    suspend fun clear(): Boolean
}