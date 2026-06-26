package com.networth.api.service

import com.networth.api.dto.TransactionRequest
import com.networth.api.dto.TransactionResponse
import com.networth.api.entity.Account
import com.networth.api.entity.Transaction
import com.networth.api.repository.AccountRepository
import com.networth.api.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) {

    fun createTransaction(accountId: Long, request: TransactionRequest): TransactionResponse {
        val account = accountRepository.findById(accountId)
            .orElseThrow { IllegalArgumentException("Account not found") }

        val transaction = Transaction(
            account = account,
            description = request.description,
            amount = request.amount,
            type = request.type,
            date = request.date,
            category = request.category,
            notes = request.notes,
            createdAt = LocalDateTime.now()
        )

        val savedTransaction = transactionRepository.save(transaction)
        return toResponse(savedTransaction)
    }

    fun getTransactionsByAccountId(accountId: Long): List<TransactionResponse> {
        return transactionRepository.findByAccountId(accountId)
            .map { toResponse(it) }
    }

    fun getTransactionById(transactionId: Long): TransactionResponse {
        val transaction = transactionRepository.findById(transactionId)
            .orElseThrow { IllegalArgumentException("Transaction not found") }
        return toResponse(transaction)
    }

    fun getTransactionsByAccountIdAndDateRange(
        accountId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<TransactionResponse> {
        return transactionRepository.findByAccountIdAndDateBetween(accountId, startDate, endDate)
            .map { toResponse(it) }
    }

    fun updateTransaction(transactionId: Long, request: TransactionRequest): TransactionResponse {
        val transaction = transactionRepository.findById(transactionId)
            .orElseThrow { IllegalArgumentException("Transaction not found") }

        val updatedTransaction = transaction.copy(
            description = request.description,
            amount = request.amount,
            type = request.type,
            date = request.date,
            category = request.category,
            notes = request.notes,
            updatedAt = LocalDateTime.now()
        )

        val savedTransaction = transactionRepository.save(updatedTransaction)
        return toResponse(savedTransaction)
    }

    fun deleteTransaction(transactionId: Long) {
        transactionRepository.deleteById(transactionId)
    }

    private fun toResponse(transaction: Transaction): TransactionResponse {
        return TransactionResponse(
            id = transaction.id!!,
            accountId = transaction.account.id!!,
            description = transaction.description,
            amount = transaction.amount,
            type = transaction.type,
            date = transaction.date,
            category = transaction.category,
            notes = transaction.notes,
            createdAt = transaction.createdAt,
            updatedAt = transaction.updatedAt
        )
    }
}
