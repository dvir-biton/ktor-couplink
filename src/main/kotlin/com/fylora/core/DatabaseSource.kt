package com.fylora.core

import com.fylora.core.logging.LogDataSource
import com.fylora.core.messages.MessageDataSource
import com.fylora.core.user.UserDataSource

object DatabaseSource {
    lateinit var userDataSource: UserDataSource
    lateinit var logDataSource: LogDataSource
    lateinit var messageDataSource: MessageDataSource
}