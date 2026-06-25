package com.networth.api.dto

import com.networth.api.model.AccountCategory
import com.networth.api.model.AccountType
import java.time.LocalDateTime

data class AccountResponse(
    val id: Long,
    val name: String,
    val type: AccountType,
    val category: AccountCategory,
    val currency: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)
