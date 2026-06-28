package com.networth.api.dto

data class CsvImportResponse(
    val successCount: Int,
    val errorCount: Int,
    val errors: List<String>,
    val transactionIds: List<Long>
)
