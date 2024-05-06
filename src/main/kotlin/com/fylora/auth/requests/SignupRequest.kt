package com.fylora.auth.requests

import com.fylora.core.user.UserData
import kotlinx.serialization.Serializable

@Serializable
data class SignupRequest(
    val username: String,
    val password: String,
    val userData: UserData
)
