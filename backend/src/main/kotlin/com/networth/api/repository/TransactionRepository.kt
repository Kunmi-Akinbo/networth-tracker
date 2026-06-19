package com.networth.api.repository

import com.networth.api.entity.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.List

@Repository
interface TransactionRepository : JpaRepository<Transaction, Long> {
    fun findByAccountId(accountId: Long): List<Transaction>
    fun findByAccountIdAndDateBetween(accountId: Long, startDate: LocalDate, endDate: LocalDate): List<Transaction>
    fun findByAccountIdOrderByDateDesc(accountId: Long): List<Transaction>
}
