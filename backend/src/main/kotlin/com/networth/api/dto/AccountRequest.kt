package com.networth.api.dto

import com.networth.api.model.AccountCategory
import com.networth.api.model.AccountType
import jakarta.validation.constraints.NotBlank

data class AccountRequest(
    @field:NotBlank
    val name: String,
    
    val type: AccountType,
    
    val category: AccountCategory,
    
    val currency: String = "USD"
)
