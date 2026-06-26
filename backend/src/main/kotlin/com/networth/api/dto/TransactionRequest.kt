package com.networth.api.dto

import com.networth.api.model.TransactionType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDate

data class TransactionRequest(
    @field:NotBlank
    val description: String,
    
    @field:NotNull
    val amount: BigDecimal,
    
    @field:NotNull
    val type: TransactionType,
    
    @field:NotNull
    val date: LocalDate,
    
    val category: String? = null,
    val notes: String? = null
)
