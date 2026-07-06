package com.networth.api.dto

import com.networth.api.model.GoalStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class GoalResponse(
    val id: Long,
    val name: String,
    val targetAmount: BigDecimal,
    val currentAmount: BigDecimal,
    val deadline: LocalDate?,
    val status: GoalStatus,
    val progressPercentage: BigDecimal,
    val remainingAmount: BigDecimal,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)
