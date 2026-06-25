package com.networth.api.service

import com.networth.api.dto.AccountRequest
import com.networth.api.dto.AccountResponse
import com.networth.api.entity.Account
import com.networth.api.entity.User
import com.networth.api.repository.AccountRepository
import com.networth.api.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class AccountService(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository
) {

    fun createAccount(userId: Long, request: AccountRequest): AccountResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val account = Account(
            user = user,
            name = request.name,
            type = request.type,
            category = request.category,
            currency = request.currency,
            createdAt = LocalDateTime.now()
        )

        val savedAccount = accountRepository.save(account)
        return toResponse(savedAccount)
    }

    fun getAccountsByUserId(userId: Long): List<AccountResponse> {
        return accountRepository.findByUserId(userId)
            .map { toResponse(it) }
    }

    fun getAccountById(accountId: Long): AccountResponse {
        val account = accountRepository.findById(accountId)
            .orElseThrow { IllegalArgumentException("Account not found") }
        return toResponse(account)
    }

    fun updateAccount(accountId: Long, request: AccountRequest): AccountResponse {
        val account = accountRepository.findById(accountId)
            .orElseThrow { IllegalArgumentException("Account not found") }

        val updatedAccount = account.copy(
            name = request.name,
            type = request.type,
            category = request.category,
            currency = request.currency,
            updatedAt = LocalDateTime.now()
        )

        val savedAccount = accountRepository.save(updatedAccount)
        return toResponse(savedAccount)
    }

    fun deleteAccount(accountId: Long) {
        accountRepository.deleteById(accountId)
    }

    fun getAccountsByCategory(userId: Long, category: String): List<AccountResponse> {
        return accountRepository.findByUserIdAndCategory(userId, 
            com.networth.api.model.AccountCategory.valueOf(category))
            .map { toResponse(it) }
    }

    private fun toResponse(account: Account): AccountResponse {
        return AccountResponse(
            id = account.id!!,
            name = account.name,
            type = account.type,
            category = account.category,
            currency = account.currency,
            createdAt = account.createdAt,
            updatedAt = account.updatedAt
        )
    }
}
