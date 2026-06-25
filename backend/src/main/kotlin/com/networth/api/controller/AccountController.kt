package com.networth.api.controller

import com.networth.api.dto.AccountRequest
import com.networth.api.dto.AccountResponse
import com.networth.api.service.AccountService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/accounts")
class AccountController(
    private val accountService: AccountService
) {

    @PostMapping
    fun createAccount(
        @Valid @RequestBody request: AccountRequest,
        authentication: Authentication
    ): ResponseEntity<AccountResponse> {
        val userId = authentication.principal as Long
        val response = accountService.createAccount(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getAccounts(authentication: Authentication): ResponseEntity<List<AccountResponse>> {
        val userId = authentication.principal as Long
        val accounts = accountService.getAccountsByUserId(userId)
        return ResponseEntity.ok(accounts)
    }

    @GetMapping("/{id}")
    fun getAccountById(@PathVariable id: Long): ResponseEntity<AccountResponse> {
        val account = accountService.getAccountById(id)
        return ResponseEntity.ok(account)
    }

    @GetMapping("/category/{category}")
    fun getAccountsByCategory(
        @PathVariable category: String,
        authentication: Authentication
    ): ResponseEntity<List<AccountResponse>> {
        val userId = authentication.principal as Long
        val accounts = accountService.getAccountsByCategory(userId, category)
        return ResponseEntity.ok(accounts)
    }

    @PutMapping("/{id}")
    fun updateAccount(
        @PathVariable id: Long,
        @Valid @RequestBody request: AccountRequest
    ): ResponseEntity<AccountResponse> {
        val response = accountService.updateAccount(id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun deleteAccount(@PathVariable id: Long): ResponseEntity<Void> {
        accountService.deleteAccount(id)
        return ResponseEntity.noContent().build()
    }
}
