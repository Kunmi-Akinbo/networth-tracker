package com.networth.api.service

import com.networth.api.dto.AnalyticsResponse
import com.networth.api.dto.CategoryBreakdown
import com.networth.api.dto.IncomeExpenseSummary
import com.networth.api.dto.MonthlySummary
import com.networth.api.dto.TrendDataPoint
import com.networth.api.entity.Account
import com.networth.api.entity.NetworthSnapshot
import com.networth.api.entity.Transaction
import com.networth.api.model.AccountCategory
import com.networth.api.model.TransactionType
import com.networth.api.repository.AccountRepository
import com.networth.api.repository.NetworthSnapshotRepository
import com.networth.api.repository.TransactionRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Service
class AnalyticsService(
    private val accountRepository: AccountRepository,
    private val snapshotRepository: NetworthSnapshotRepository,
    private val transactionRepository: TransactionRepository
) {

    fun getAnalytics(userId: Long, months: Int = 12): AnalyticsResponse {
        val currentNetWorth = calculateCurrentNetWorth(userId)
        val totalAssets = calculateTotalAssets(userId)
        val totalLiabilities = calculateTotalLiabilities(userId)
        
        val snapshots = snapshotRepository.findByUserIdOrderBySnapshotDateDesc(userId)
        val netWorthChange = calculateNetWorthChange(snapshots, months)
        val netWorthChangePercentage = calculateNetWorthChangePercentage(currentNetWorth, netWorthChange)
        
        val assetBreakdown = calculateCategoryBreakdown(userId, AccountCategory.ASSET)
        val liabilityBreakdown = calculateCategoryBreakdown(userId, AccountCategory.LIABILITY)
        
        val incomeExpenseSummary = calculateIncomeExpenseSummary(userId, months)
        
        val netWorthTrend = calculateNetWorthTrend(snapshots, months)
        
        return AnalyticsResponse(
            currentNetWorth = currentNetWorth,
            netWorthChange = netWorthChange,
            netWorthChangePercentage = netWorthChangePercentage,
            totalAssets = totalAssets,
            totalLiabilities = totalLiabilities,
            assetBreakdown = assetBreakdown,
            liabilityBreakdown = liabilityBreakdown,
            incomeExpenseSummary = incomeExpenseSummary,
            netWorthTrend = netWorthTrend
        )
    }

    fun getMonthlySummaries(userId: Long, months: Int = 12): List<MonthlySummary> {
        val summaries = mutableListOf<MonthlySummary>()
        val currentDate = LocalDate.now()
        
        for (i in 0 until months) {
            val yearMonth = currentDate.minusMonths(i.toLong())
            val monthStart = yearMonth.atDay(1)
            val monthEnd = yearMonth.atEndOfMonth()
            
            val income = calculateTotalIncome(userId, monthStart, monthEnd)
            val expenses = calculateTotalExpenses(userId, monthStart, monthEnd)
            val netCashFlow = income.subtract(expenses)
            
            // Get net worth at end of month
            val snapshot = snapshotRepository.findByUserIdAndSnapshotDate(userId, monthEnd)
            val netWorth = snapshot.map { it.netWorth }.orElse(BigDecimal.ZERO)
            
            summaries.add(
                MonthlySummary(
                    month = yearMonth.format(DateTimeFormatter.ofPattern("MMMM")),
                    year = yearMonth.year,
                    income = income,
                    expenses = expenses,
                    netCashFlow = netCashFlow,
                    netWorth = netWorth
                )
            )
        }
        
        return summaries.reversed()
    }

    private fun calculateCurrentNetWorth(userId: Long): BigDecimal {
        val accounts = accountRepository.findByUserId(userId)
        val totalAssets = accounts.filter { it.category == AccountCategory.ASSET }
            .fold(BigDecimal.ZERO) { acc, account -> acc.add(account.currentBalance) }
        val totalLiabilities = accounts.filter { it.category == AccountCategory.LIABILITY }
            .fold(BigDecimal.ZERO) { acc, account -> acc.add(account.currentBalance) }
        return totalAssets.subtract(totalLiabilities)
    }

    private fun calculateTotalAssets(userId: Long): BigDecimal {
        val accounts = accountRepository.findByUserId(userId)
        return accounts.filter { it.category == AccountCategory.ASSET }
            .fold(BigDecimal.ZERO) { acc, account -> acc.add(account.currentBalance) }
    }

    private fun calculateTotalLiabilities(userId: Long): BigDecimal {
        val accounts = accountRepository.findByUserId(userId)
        return accounts.filter { it.category == AccountCategory.LIABILITY }
            .fold(BigDecimal.ZERO) { acc, account -> acc.add(account.currentBalance) }
    }

    private fun calculateNetWorthChange(snapshots: List<NetworthSnapshot>, months: Int): BigDecimal {
        if (snapshots.size < 2) return BigDecimal.ZERO
        
        val current = snapshots.first().netWorth
        val targetDate = LocalDate.now().minusMonths(months.toLong())
        
        val previous = snapshots
            .filter { it.snapshotDate.isBefore(targetDate) || it.snapshotDate.isEqual(targetDate) }
            .maxByOrNull { it.snapshotDate }
        
        return if (previous != null) {
            current.subtract(previous.netWorth)
        } else {
            BigDecimal.ZERO
        }
    }

    private fun calculateNetWorthChangePercentage(currentNetWorth: BigDecimal, change: BigDecimal): BigDecimal {
        if (currentNetWorth == BigDecimal.ZERO) return BigDecimal.ZERO
        
        return change.divide(currentNetWorth.abs(), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal("100"))
            .setScale(2, RoundingMode.HALF_UP)
    }

    private fun calculateCategoryBreakdown(userId: Long, category: AccountCategory): List<CategoryBreakdown> {
        val accounts = accountRepository.findByUserId(userId)
            .filter { it.category == category }
        
        if (accounts.isEmpty()) return emptyList()
        
        val total = accounts.fold(BigDecimal.ZERO) { acc, account -> acc.add(account.currentBalance) }
        
        return accounts.map { account ->
            val percentage = if (total > BigDecimal.ZERO) {
                account.currentBalance.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }
            CategoryBreakdown(
                category = account.name,
                amount = account.currentBalance,
                percentage = percentage
            )
        }.sortedByDescending { it.amount }
    }

    private fun calculateIncomeExpenseSummary(userId: Long, months: Int): IncomeExpenseSummary {
        val startDate = LocalDate.now().minusMonths(months.toLong())
        val endDate = LocalDate.now()
        
        val totalIncome = calculateTotalIncome(userId, startDate, endDate)
        val totalExpenses = calculateTotalExpenses(userId, startDate, endDate)
        val netCashFlow = totalIncome.subtract(totalExpenses)
        
        val expenseByCategory = calculateExpenseByCategory(userId, startDate, endDate)
        val incomeByCategory = calculateIncomeByCategory(userId, startDate, endDate)
        
        return IncomeExpenseSummary(
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netCashFlow = netCashFlow,
            expenseByCategory = expenseByCategory,
            incomeByCategory = incomeByCategory
        )
    }

    private fun calculateTotalIncome(userId: Long, startDate: LocalDate, endDate: LocalDate): BigDecimal {
        val accounts = accountRepository.findByUserId(userId)
        val accountIds = accounts.map { it.id!! }
        
        return accountIds.fold(BigDecimal.ZERO) { acc, accountId ->
            val transactions = transactionRepository.findByAccountIdAndDateBetween(accountId, startDate, endDate)
            acc.add(
                transactions.filter { it.type == TransactionType.INCOME }
                    .fold(BigDecimal.ZERO) { tAcc, transaction -> tAcc.add(transaction.amount) }
            )
        }
    }

    private fun calculateTotalExpenses(userId: Long, startDate: LocalDate, endDate: LocalDate): BigDecimal {
        val accounts = accountRepository.findByUserId(userId)
        val accountIds = accounts.map { it.id!! }
        
        return accountIds.fold(BigDecimal.ZERO) { acc, accountId ->
            val transactions = transactionRepository.findByAccountIdAndDateBetween(accountId, startDate, endDate)
            acc.add(
                transactions.filter { it.type == TransactionType.EXPENSE }
                    .fold(BigDecimal.ZERO) { tAcc, transaction -> tAcc.add(transaction.amount) }
            )
        }
    }

    private fun calculateExpenseByCategory(userId: Long, startDate: LocalDate, endDate: LocalDate): List<CategoryBreakdown> {
        val accounts = accountRepository.findByUserId(userId)
        val accountIds = accounts.map { it.id!! }
        
        val categoryMap = mutableMapOf<String, BigDecimal>()
        
        for (accountId in accountIds) {
            val transactions = transactionRepository.findByAccountIdAndDateBetween(accountId, startDate, endDate)
            for (transaction in transactions.filter { it.type == TransactionType.EXPENSE }) {
                val category = transaction.category ?: "Uncategorized"
                categoryMap[category] = categoryMap.getOrDefault(category, BigDecimal.ZERO).add(transaction.amount)
            }
        }
        
        val total = categoryMap.values.fold(BigDecimal.ZERO) { acc, amount -> acc.add(amount) }
        
        return categoryMap.map { (category, amount) ->
            val percentage = if (total > BigDecimal.ZERO) {
                amount.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }
            CategoryBreakdown(category = category, amount = amount, percentage = percentage)
        }.sortedByDescending { it.amount }
    }

    private fun calculateIncomeByCategory(userId: Long, startDate: LocalDate, endDate: LocalDate): List<CategoryBreakdown> {
        val accounts = accountRepository.findByUserId(userId)
        val accountIds = accounts.map { it.id!! }
        
        val categoryMap = mutableMapOf<String, BigDecimal>()
        
        for (accountId in accountIds) {
            val transactions = transactionRepository.findByAccountIdAndDateBetween(accountId, startDate, endDate)
            for (transaction in transactions.filter { it.type == TransactionType.INCOME }) {
                val category = transaction.category ?: "Uncategorized"
                categoryMap[category] = categoryMap.getOrDefault(category, BigDecimal.ZERO).add(transaction.amount)
            }
        }
        
        val total = categoryMap.values.fold(BigDecimal.ZERO) { acc, amount -> acc.add(amount) }
        
        return categoryMap.map { (category, amount) ->
            val percentage = if (total > BigDecimal.ZERO) {
                amount.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }
            CategoryBreakdown(category = category, amount = amount, percentage = percentage)
        }.sortedByDescending { it.amount }
    }

    private fun calculateNetWorthTrend(snapshots: List<NetworthSnapshot>, months: Int): List<TrendDataPoint> {
        val targetDate = LocalDate.now().minusMonths(months.toLong())
        val filteredSnapshots = snapshots
            .filter { it.snapshotDate.isAfter(targetDate) || it.snapshotDate.isEqual(targetDate) }
            .sortedBy { it.snapshotDate }
        
        return filteredSnapshots.map { snapshot ->
            TrendDataPoint(
                date = snapshot.snapshotDate,
                value = snapshot.netWorth
            )
        }
    }
}
