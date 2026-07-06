package com.networth.api.dto

import java.math.BigDecimal
import java.time.LocalDate

data class GoalRequest(
    val name: String,
    val targetAmount: BigDecimal,
    val currentAmount: BigDecimal = BigDecimal.ZERO,
    val deadline: LocalDate? = null
)
