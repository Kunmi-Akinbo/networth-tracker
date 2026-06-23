package com.networth.api.dto

import java.time.LocalDateTime

data class UserResponse(
    val id: Long,
    val email: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)
