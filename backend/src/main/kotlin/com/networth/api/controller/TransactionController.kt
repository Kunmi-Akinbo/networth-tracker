package com.networth.api.controller

import com.networth.api.dto.CsvImportResponse
import com.networth.api.dto.TransactionRequest
import com.networth.api.dto.TransactionResponse
import com.networth.api.service.CsvParserService
import com.networth.api.service.TransactionService
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@RestController
@RequestMapping("/api/transactions")
class TransactionController(
    private val transactionService: TransactionService,
    private val csvParserService: CsvParserService
) {

    @PostMapping
    fun createTransaction(
        @RequestParam accountId: Long,
        @Valid @RequestBody request: TransactionRequest
    ): ResponseEntity<TransactionResponse> {
        val response = transactionService.createTransaction(accountId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getTransactionsByAccountId(@RequestParam accountId: Long): ResponseEntity<List<TransactionResponse>> {
        val transactions = transactionService.getTransactionsByAccountId(accountId)
        return ResponseEntity.ok(transactions)
    }

    @GetMapping("/{id}")
    fun getTransactionById(@PathVariable id: Long): ResponseEntity<TransactionResponse> {
        val transaction = transactionService.getTransactionById(id)
        return ResponseEntity.ok(transaction)
    }

    @GetMapping("/by-date-range")
    fun getTransactionsByDateRange(
        @RequestParam accountId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<List<TransactionResponse>> {
        val transactions = transactionService.getTransactionsByAccountIdAndDateRange(accountId, startDate, endDate)
        return ResponseEntity.ok(transactions)
    }

    @PutMapping("/{id}")
    fun updateTransaction(
        @PathVariable id: Long,
        @Valid @RequestBody request: TransactionRequest
    ): ResponseEntity<TransactionResponse> {
        val response = transactionService.updateTransaction(id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun deleteTransaction(@PathVariable id: Long): ResponseEntity<Void> {
        transactionService.deleteTransaction(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/import")
    fun importTransactionsFromCsv(
        @RequestParam accountId: Long,
        @RequestParam file: MultipartFile
    ): ResponseEntity<CsvImportResponse> {
        val csvContent = String(file.bytes)
        val parseResult = csvParserService.parseTransactions(csvContent)

        val transactionIds = mutableListOf<Long>()
        val errors = mutableListOf<String>()

        for (transactionRequest in parseResult.transactions) {
            try {
                val response = transactionService.createTransaction(accountId, transactionRequest)
                transactionIds.add(response.id)
            } catch (e: Exception) {
                errors.add("Failed to create transaction: ${e.message}")
            }
        }

        errors.addAll(parseResult.errors)

        val importResponse = CsvImportResponse(
            successCount = transactionIds.size,
            errorCount = errors.size,
            errors = errors,
            transactionIds = transactionIds
        )

        return ResponseEntity.ok(importResponse)
    }
}
