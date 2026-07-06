package com.networth.api.service

import com.networth.api.dto.AnalyticsResponse
import com.networth.api.dto.CategoryBreakdown
import com.networth.api.dto.IncomeExpenseSummary
import com.networth.api.dto.MonthlySummary
import com.networth.api.dto.TrendDataPoint
import com.networth.api.entity.Account
import com.networth.api.entity.NetworthSnapshot
import com.networth.api.entity.Transaction
import com.networth.api.entity.User
import com.networth.api.model.AccountCategory
import com.networth.api.model.AccountType
import com.networth.api.model.TransactionType
import com.networth.api.repository.AccountRepository
import com.networth.api.repository.NetworthSnapshotRepository
import com.networth.api.repository.TransactionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AnalyticsServiceTest {

    @Mock
    private lateinit var accountRepository: AccountRepository

    @Mock
    private lateinit var snapshotRepository: NetworthSnapshotRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @InjectMocks
    private lateinit var analyticsService: AnalyticsService

    private lateinit var testUser: User
    private lateinit var testAccounts: List<Account>
    private lateinit var testSnapshots: List<NetworthSnapshot>

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        testUser = User(
            id = 1L,
            email = "test@example.com",
            passwordHash = "hashed",
            createdAt = LocalDateTime.now()
        )
        testAccounts = listOf(
            Account(
                id = 1L,
                user = testUser,
                name = "Checking Account",
                type = AccountType.CHECKING,
                category = AccountCategory.ASSET,
                currency = "USD",
                currentBalance = BigDecimal("5000.00"),
                createdAt = LocalDateTime.now()
            ),
            Account(
                id = 2L,
                user = testUser,
                name = "Credit Card",
                type = AccountType.CREDIT_CARD,
                category = AccountCategory.LIABILITY,
                currency = "USD",
                currentBalance = BigDecimal("1000.00"),
                createdAt = LocalDateTime.now()
            ),
            Account(
                id = 3L,
                user = testUser,
                name = "Savings Account",
                type = AccountType.SAVINGS,
                category = AccountCategory.ASSET,
                currency = "USD",
                currentBalance = BigDecimal("20000.00"),
                createdAt = LocalDateTime.now()
            )
        )
        testSnapshots = listOf(
            NetworthSnapshot(
                id = 1L,
                user = testUser,
                totalAssets = BigDecimal("25000.00"),
                totalLiabilities = BigDecimal("1000.00"),
                netWorth = BigDecimal("24000.00"),
                snapshotDate = LocalDate.now()
            ),
            NetworthSnapshot(
                id = 2L,
                user = testUser,
                totalAssets = BigDecimal("23000.00"),
                totalLiabilities = BigDecimal("1000.00"),
                netWorth = BigDecimal("22000.00"),
                snapshotDate = LocalDate.now().minusMonths(1)
            )
        )
    }

    @Test
    fun `getAnalytics should return correct analytics data`() {
        // Arrange
        val userId = 1L
        whenever(accountRepository.findByUserId(userId)).thenReturn(testAccounts)
        whenever(snapshotRepository.findByUserIdOrderBySnapshotDateDesc(userId)).thenReturn(testSnapshots)
        whenever(transactionRepository.findByAccountIdAndDateBetween(any(), any(), any())).thenReturn(emptyList())

        // Act
        val result = analyticsService.getAnalytics(userId, 12)

        // Assert
        assertNotNull(result)
        assertEquals(BigDecimal("24000.00"), result.currentNetWorth)
        assertEquals(BigDecimal("25000.00"), result.totalAssets)
        assertEquals(BigDecimal("1000.00"), result.totalLiabilities)
        assertEquals(2, result.assetBreakdown.size)
        assertEquals(1, result.liabilityBreakdown.size)
    }

    @Test
    fun `getAnalytics should calculate net worth change correctly`() {
        // Arrange
        val userId = 1L
        whenever(accountRepository.findByUserId(userId)).thenReturn(testAccounts)
        whenever(snapshotRepository.findByUserIdOrderBySnapshotDateDesc(userId)).thenReturn(testSnapshots)
        whenever(transactionRepository.findByAccountIdAndDateBetween(any(), any(), any())).thenReturn(emptyList())

        // Act
        val result = analyticsService.getAnalytics(userId, 12)

        // Assert
        assertEquals(BigDecimal("2000.00"), result.netWorthChange)
    }

    @Test
    fun `getAnalytics should handle zero net worth for percentage calculation`() {
        // Arrange
        val userId = 1L
        val zeroBalanceAccounts = testAccounts.map { it.copy(currentBalance = BigDecimal.ZERO) }
        val zeroSnapshots = testSnapshots.map { it.copy(netWorth = BigDecimal.ZERO) }
        whenever(accountRepository.findByUserId(userId)).thenReturn(zeroBalanceAccounts)
        whenever(snapshotRepository.findByUserIdOrderBySnapshotDateDesc(userId)).thenReturn(zeroSnapshots)
        whenever(transactionRepository.findByAccountIdAndDateBetween(any(), any(), any())).thenReturn(emptyList())

        // Act
        val result = analyticsService.getAnalytics(userId, 12)

        // Assert
        assertEquals(BigDecimal.ZERO, result.netWorthChangePercentage)
    }

    @Test
    fun `getAnalytics should calculate category breakdowns correctly`() {
        // Arrange
        val userId = 1L
        whenever(accountRepository.findByUserId(userId)).thenReturn(testAccounts)
        whenever(snapshotRepository.findByUserIdOrderBySnapshotDateDesc(userId)).thenReturn(testSnapshots)
        whenever(transactionRepository.findByAccountIdAndDateBetween(any(), any(), any())).thenReturn(emptyList())

        // Act
        val result = analyticsService.getAnalytics(userId, 12)

        // Assert
        val assetBreakdown = result.assetBreakdown
        val totalAssets = BigDecimal("25000.00")
        val savingsPercentage = BigDecimal("20000.00").divide(totalAssets, 4, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal("100")).setScale(2, java.math.RoundingMode.HALF_UP)
        
        assertTrue(assetBreakdown.any { it.category == "Savings Account" && it.amount == BigDecimal("20000.00") })
    }

    @Test
    fun `getAnalytics should include income expense summary`() {
        // Arrange
        val userId = 1L
        whenever(accountRepository.findByUserId(userId)).thenReturn(testAccounts)
        whenever(snapshotRepository.findByUserIdOrderBySnapshotDateDesc(userId)).thenReturn(testSnapshots)
        whenever(transactionRepository.findByAccountIdAndDateBetween(any(), any(), any())).thenReturn(emptyList())

        // Act
        val result = analyticsService.getAnalytics(userId, 12)

        // Assert
        assertNotNull(result.incomeExpenseSummary)
        assertEquals(BigDecimal.ZERO, result.incomeExpenseSummary.totalIncome)
        assertEquals(BigDecimal.ZERO, result.incomeExpenseSummary.totalExpenses)
    }

    @Test
    fun `getAnalytics should include net worth trend`() {
        // Arrange
        val userId = 1L
        whenever(accountRepository.findByUserId(userId)).thenReturn(testAccounts)
        whenever(snapshotRepository.findByUserIdOrderBySnapshotDateDesc(userId)).thenReturn(testSnapshots)
        whenever(transactionRepository.findByAccountIdAndDateBetween(any(), any(), any())).thenReturn(emptyList())

        // Act
        val result = analyticsService.getAnalytics(userId, 12)

        // Assert
        assertNotNull(result.netWorthTrend)
        assertTrue(result.netWorthTrend.isNotEmpty())
    }

    @Test
    fun `getMonthlySummaries should return monthly data`() {
        // Arrange
        val userId = 1L
        whenever(accountRepository.findByUserId(userId)).thenReturn(testAccounts)
        whenever(snapshotRepository.findByUserIdAndSnapshotDate(any(), any())).thenReturn(Optional.empty())
        whenever(transactionRepository.findByAccountIdAndDateBetween(any(), any(), any())).thenReturn(emptyList())

        // Act
        val result = analyticsService.getMonthlySummaries(userId, 3)

        // Assert
        assertNotNull(result)
        assertEquals(3, result.size)
    }

    @Test
    fun `getMonthlySummaries should calculate income and expenses correctly`() {
        // Arrange
        val userId = 1L
        val transactions = listOf(
            Transaction(
                id = 1L,
                account = testAccounts[0],
                type = TransactionType.INCOME,
                amount = BigDecimal("5000.00"),
                date = LocalDate.now(),
                description = "Salary",
                category = "Salary"
            ),
            Transaction(
                id = 2L,
                account = testAccounts[0],
                type = TransactionType.EXPENSE,
                amount = BigDecimal("1000.00"),
                date = LocalDate.now(),
                description = "Rent",
                category = "Housing"
            )
        )
        whenever(accountRepository.findByUserId(userId)).thenReturn(testAccounts)
        whenever(snapshotRepository.findByUserIdAndSnapshotDate(any(), any())).thenReturn(Optional.empty())
        whenever(transactionRepository.findByAccountIdAndDateBetween(any(), any(), any())).thenReturn(transactions)

        // Act
        val result = analyticsService.getMonthlySummaries(userId, 1)

        // Assert
        assertNotNull(result)
        val currentMonth = result.first()
        assertEquals(BigDecimal("5000.00"), currentMonth.income)
        assertEquals(BigDecimal("1000.00"), currentMonth.expenses)
        assertEquals(BigDecimal("4000.00"), currentMonth.netCashFlow)
    }

    @Test
    fun `getMonthlySummaries should handle empty transactions`() {
        // Arrange
        val userId = 1L
        whenever(accountRepository.findByUserId(userId)).thenReturn(testAccounts)
        whenever(snapshotRepository.findByUserIdAndSnapshotDate(any(), any())).thenReturn(Optional.empty())
        whenever(transactionRepository.findByAccountIdAndDateBetween(any(), any(), any())).thenReturn(emptyList())

        // Act
        val result = analyticsService.getMonthlySummaries(userId, 1)

        // Assert
        assertNotNull(result)
        val currentMonth = result.first()
        assertEquals(BigDecimal.ZERO, currentMonth.income)
        assertEquals(BigDecimal.ZERO, currentMonth.expenses)
        assertEquals(BigDecimal.ZERO, currentMonth.netCashFlow)
    }

    @Test
    fun `getMonthlySummaries should include net worth from snapshot`() {
        // Arrange
        val userId = 1L
        val monthEnd = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())
        val snapshot = NetworthSnapshot(
            id = 1L,
            user = testUser,
            totalAssets = BigDecimal("25000.00"),
            totalLiabilities = BigDecimal("1000.00"),
            netWorth = BigDecimal("24000.00"),
            snapshotDate = monthEnd
        )
        whenever(accountRepository.findByUserId(userId)).thenReturn(testAccounts)
        whenever(snapshotRepository.findByUserIdAndSnapshotDate(userId, monthEnd)).thenReturn(Optional.of(snapshot))
        whenever(transactionRepository.findByAccountIdAndDateBetween(any(), any(), any())).thenReturn(emptyList())

        // Act
        val result = analyticsService.getMonthlySummaries(userId, 1)

        // Assert
        assertNotNull(result)
        val currentMonth = result.first()
        assertEquals(BigDecimal("24000.00"), currentMonth.netWorth)
    }

    @Test
    fun `getAnalytics should calculate income by category correctly`() {
        // Arrange
        val userId = 1L
        val transactions = listOf(
            Transaction(
                id = 1L,
                account = testAccounts[0],
                type = TransactionType.INCOME,
                amount = BigDecimal("5000.00"),
                date = LocalDate.now(),
                description = "Salary",
                category = "Salary"
            ),
            Transaction(
                id = 2L,
                account = testAccounts[0],
                type = TransactionType.INCOME,
                amount = BigDecimal("500.00"),
                date = LocalDate.now(),
                description = "Freelance",
                category = "Freelance"
            )
        )
        whenever(accountRepository.findByUserId(userId)).thenReturn(testAccounts)
        whenever(snapshotRepository.findByUserIdOrderBySnapshotDateDesc(userId)).thenReturn(testSnapshots)
        whenever(transactionRepository.findByAccountIdAndDateBetween(any(), any(), any())).thenReturn(transactions)

        // Act
        val result = analyticsService.getAnalytics(userId, 12)

        // Assert
        assertNotNull(result.incomeExpenseSummary.incomeByCategory)
        assertTrue(result.incomeExpenseSummary.incomeByCategory.any { it.category == "Salary" })
        assertTrue(result.incomeExpenseSummary.incomeByCategory.any { it.category == "Freelance" })
    }

    @Test
    fun `getAnalytics should calculate expense by category correctly`() {
        // Arrange
        val userId = 1L
        val transactions = listOf(
            Transaction(
                id = 1L,
                account = testAccounts[0],
                type = TransactionType.EXPENSE,
                amount = BigDecimal("1000.00"),
                date = LocalDate.now(),
                description = "Rent",
                category = "Housing"
            ),
            Transaction(
                id = 2L,
                account = testAccounts[0],
                type = TransactionType.EXPENSE,
                amount = BigDecimal("200.00"),
                date = LocalDate.now(),
                description = "Groceries",
                category = "Food"
            )
        )
        whenever(accountRepository.findByUserId(userId)).thenReturn(testAccounts)
        whenever(snapshotRepository.findByUserIdOrderBySnapshotDateDesc(userId)).thenReturn(testSnapshots)
        whenever(transactionRepository.findByAccountIdAndDateBetween(any(), any(), any())).thenReturn(transactions)

        // Act
        val result = analyticsService.getAnalytics(userId, 12)

        // Assert
        assertNotNull(result.incomeExpenseSummary.expenseByCategory)
        assertTrue(result.incomeExpenseSummary.expenseByCategory.any { it.category == "Housing" })
        assertTrue(result.incomeExpenseSummary.expenseByCategory.any { it.category == "Food" })
    }

    @Test
    fun `getAnalytics should handle uncategorized transactions`() {
        // Arrange
        val userId = 1L
        val transactions = listOf(
            Transaction(
                id = 1L,
                account = testAccounts[0],
                type = TransactionType.EXPENSE,
                amount = BigDecimal("100.00"),
                date = LocalDate.now(),
                description = "Misc",
                category = null
            )
        )
        whenever(accountRepository.findByUserId(userId)).thenReturn(testAccounts)
        whenever(snapshotRepository.findByUserIdOrderBySnapshotDateDesc(userId)).thenReturn(testSnapshots)
        whenever(transactionRepository.findByAccountIdAndDateBetween(any(), any(), any())).thenReturn(transactions)

        // Act
        val result = analyticsService.getAnalytics(userId, 12)

        // Assert
        assertNotNull(result.incomeExpenseSummary.expenseByCategory)
        assertTrue(result.incomeExpenseSummary.expenseByCategory.any { it.category == "Uncategorized" })
    }
}
