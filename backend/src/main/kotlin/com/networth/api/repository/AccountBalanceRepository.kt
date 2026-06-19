package com.networth.api.repository

import com.networth.api.entity.AccountBalance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional

@Repository
interface AccountBalanceRepository : JpaRepository<AccountBalance, Long> {
    fun findByAccountIdOrderByRecordedAtDesc(accountId: Long): List<AccountBalance>
    
    fun findFirstByAccountIdOrderByRecordedAtDesc(accountId: Long): Optional<AccountBalance>
    
    @Query("SELECT COALESCE(SUM(ab.balance), 0) FROM AccountBalance ab " +
           "JOIN ab.account a " +
           "WHERE a.user.id = :userId " +
           "AND a.category = :category " +
           "AND ab.id IN (" +
           "  SELECT MAX(ab2.id) FROM AccountBalance ab2 " +
           "  WHERE ab2.account.id = ab.account.id" +
           ")")
    fun sumLatestBalancesByCategory(@Param("userId") userId: Long, @Param("category") category: String): BigDecimal
}
