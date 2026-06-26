package com.networth.api.dto

import com.networth.api.model.TransactionType
import java.time.LocalDate
import java.time.LocalDateTime

data class TransactionResponse(
    val id: Long,
    val accountId: Long,
    val description: String,
    val amount: BigDecimal,
    val type: TransactionType,
    val date: LocalDate,
    val category: String?,
    val notes: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)
