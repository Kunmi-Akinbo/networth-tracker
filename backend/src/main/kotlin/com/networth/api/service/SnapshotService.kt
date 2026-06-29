package com.networth.api.service

import com.networth.api.entity.Account
import com.networth.api.entity.NetworthSnapshot
import com.networth.api.entity.User
import com.networth.api.model.AccountCategory
import com.networth.api.repository.AccountRepository
import com.networth.api.repository.NetworthSnapshotRepository
import com.networth.api.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class SnapshotService(
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val snapshotRepository: NetworthSnapshotRepository
) {

    private val logger = LoggerFactory.getLogger(SnapshotService::class.java)

    @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    @Transactional
    fun createDailySnapshots() {
        logger.info("Starting daily net worth snapshot creation")
        
        val users = userRepository.findAll()
        var successCount = 0
        var errorCount = 0

        for (user in users) {
            try {
                createSnapshotForUser(user, LocalDate.now())
                successCount++
            } catch (e: Exception) {
                logger.error("Failed to create snapshot for user ${user.id}: ${e.message}", e)
                errorCount++
            }
        }

        logger.info("Daily snapshot creation completed. Success: $successCount, Errors: $errorCount")
    }

    @Transactional
    fun createSnapshotForUser(user: User, date: LocalDate): NetworthSnapshot {
        // Check if snapshot already exists for this date
        val existingSnapshot = snapshotRepository.findByUserIdAndSnapshotDate(user.id!!, date)
        if (existingSnapshot.isPresent) {
            logger.info("Snapshot already exists for user ${user.id} on $date, skipping")
            return existingSnapshot.get()
        }

        // Calculate net worth
        val accounts = accountRepository.findByUserId(user.id!!)
        val totalAssets = calculateTotalAssets(accounts)
        val totalLiabilities = calculateTotalLiabilities(accounts)
        val netWorth = totalAssets.subtract(totalLiabilities)

        // Create snapshot
        val snapshot = NetworthSnapshot(
            user = user,
            totalAssets = totalAssets,
            totalLiabilities = totalLiabilities,
            netWorth = netWorth,
            snapshotDate = date
        )

        return snapshotRepository.save(snapshot)
    }

    @Transactional
    fun createSnapshotForUserByUserId(userId: Long, date: LocalDate = LocalDate.now()): NetworthSnapshot {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with id: $userId") }

        return createSnapshotForUser(user, date)
    }

    fun getSnapshotsByUserId(userId: Long): List<NetworthSnapshot> {
        return snapshotRepository.findByUserIdOrderBySnapshotDateDesc(userId)
    }

    fun getSnapshotByUserIdAndDate(userId: Long, date: LocalDate): NetworthSnapshot {
        return snapshotRepository.findByUserIdAndSnapshotDate(userId, date)
            .orElseThrow { IllegalArgumentException("Snapshot not found for user $userId on $date") }
    }

    fun getSnapshotsByUserIdAndDateRange(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<NetworthSnapshot> {
        return snapshotRepository.findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            userId,
            startDate,
            endDate
        )
    }

    fun calculateCurrentNetWorth(userId: Long): BigDecimal {
        val accounts = accountRepository.findByUserId(userId)
        val totalAssets = calculateTotalAssets(accounts)
        val totalLiabilities = calculateTotalLiabilities(accounts)
        return totalAssets.subtract(totalLiabilities)
    }

    private fun calculateTotalAssets(accounts: List<Account>): BigDecimal {
        return accounts
            .filter { it.category == AccountCategory.ASSET }
            .fold(BigDecimal.ZERO) { acc, account ->
                acc.add(account.currentBalance)
            }
    }

    private fun calculateTotalLiabilities(accounts: List<Account>): BigDecimal {
        return accounts
            .filter { it.category == AccountCategory.LIABILITY }
            .fold(BigDecimal.ZERO) { acc, account ->
                acc.add(account.currentBalance)
            }
    }
}
