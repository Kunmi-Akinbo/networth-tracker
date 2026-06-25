package com.networth.api.dto

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: Long,
    val email: String
)
