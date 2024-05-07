package com.fylora.domain.util

sealed class MessageStatus(val status: String) {
    data object Sent : MessageStatus("sent")
    data object Received : MessageStatus("received")
    data object Read : MessageStatus("read")
    data object Error : MessageStatus("error")

    companion object {
        fun fromStatus(status: String): MessageStatus =
            when (status) {
                Sent.status -> Sent
                Received.status -> Received
                Read.status -> Read
                Error.status -> Error
                else -> Error
            }
    }
}
