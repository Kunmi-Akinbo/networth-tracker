package com.networth.api.service

import com.networth.api.entity.Account
import com.networth.api.entity.NetworthSnapshot
import com.networth.api.entity.User
import com.networth.api.model.AccountCategory
import com.networth.api.model.AccountType
import com.networth.api.repository.AccountRepository
import com.networth.api.repository.NetworthSnapshotRepository
import com.networth.api.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SnapshotServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var accountRepository: AccountRepository

    @Mock
    private lateinit var snapshotRepository: NetworthSnapshotRepository

    @InjectMocks
    private lateinit var snapshotService: SnapshotService

    private lateinit var testUser: User
    private lateinit var testAccounts: List<Account>

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
    }

    @Test
    fun `createSnapshotForUser should create new snapshot successfully`() {
        // Arrange
        val date = LocalDate.of(2024, 1, 15)
        whenever(snapshotRepository.findByUserIdAndSnapshotDate(testUser.id!!, date))
            .thenReturn(Optional.empty())
        whenever(accountRepository.findByUserId(testUser.id!!)).thenReturn(testAccounts)
        val savedSnapshot = NetworthSnapshot(
            id = 1L,
            user = testUser,
            totalAssets = BigDecimal("25000.00"),
            totalLiabilities = BigDecimal("1000.00"),
            netWorth = BigDecimal("24000.00"),
            snapshotDate = date
        )
        whenever(snapshotRepository.save(any())).thenReturn(savedSnapshot)

        // Act
        val result = snapshotService.createSnapshotForUser(testUser, date)

        // Assert
        assertNotNull(result)
        assertEquals(BigDecimal("25000.00"), result.totalAssets)
        assertEquals(BigDecimal("1000.00"), result.totalLiabilities)
        assertEquals(BigDecimal("24000.00"), result.netWorth)
        verify(snapshotRepository).findByUserIdAndSnapshotDate(testUser.id!!, date)
        verify(accountRepository).findByUserId(testUser.id!!)
        verify(snapshotRepository).save(any())
    }

    @Test
    fun `createSnapshotForUser should return existing snapshot if already exists`() {
        // Arrange
        val date = LocalDate.of(2024, 1, 15)
        val existingSnapshot = NetworthSnapshot(
            id = 1L,
            user = testUser,
            totalAssets = BigDecimal("25000.00"),
            totalLiabilities = BigDecimal("1000.00"),
            netWorth = BigDecimal("24000.00"),
            snapshotDate = date
        )
        whenever(snapshotRepository.findByUserIdAndSnapshotDate(testUser.id!!, date))
            .thenReturn(Optional.of(existingSnapshot))

        // Act
        val result = snapshotService.createSnapshotForUser(testUser, date)

        // Assert
        assertNotNull(result)
        assertEquals(existingSnapshot.id, result.id)
        verify(snapshotRepository).findByUserIdAndSnapshotDate(testUser.id!!, date)
        verify(accountRepository, never()).findByUserId(any())
        verify(snapshotRepository, never()).save(any())
    }

    @Test
    fun `createSnapshotForUserByUserId should create snapshot for valid user`() {
        // Arrange
        val userId = 1L
        val date = LocalDate.of(2024, 1, 15)
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(testUser))
        whenever(snapshotRepository.findByUserIdAndSnapshotDate(userId, date))
            .thenReturn(Optional.empty())
        whenever(accountRepository.findByUserId(userId)).thenReturn(testAccounts)
        val savedSnapshot = NetworthSnapshot(
            id = 1L,
            user = testUser,
            totalAssets = BigDecimal("25000.00"),
            totalLiabilities = BigDecimal("1000.00"),
            netWorth = BigDecimal("24000.00"),
            snapshotDate = date
        )
        whenever(snapshotRepository.save(any())).thenReturn(savedSnapshot)

        // Act
        val result = snapshotService.createSnapshotForUserByUserId(userId, date)

        // Assert
        assertNotNull(result)
        assertEquals(BigDecimal("24000.00"), result.netWorth)
        verify(userRepository).findById(userId)
    }

    @Test
    fun `createSnapshotForUserByUserId should throw exception for invalid user`() {
        // Arrange
        val userId = 999L
        val date = LocalDate.of(2024, 1, 15)
        whenever(userRepository.findById(userId)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            snapshotService.createSnapshotForUserByUserId(userId, date)
        }
        assertEquals("User not found with id: $userId", exception.message)
        verify(userRepository).findById(userId)
    }

    @Test
    fun `getSnapshotsByUserId should return snapshots ordered by date desc`() {
        // Arrange
        val userId = 1L
        val snapshots = listOf(
            NetworthSnapshot(
                id = 1L,
                user = testUser,
                totalAssets = BigDecimal("25000.00"),
                totalLiabilities = BigDecimal("1000.00"),
                netWorth = BigDecimal("24000.00"),
                snapshotDate = LocalDate.of(2024, 1, 15)
            ),
            NetworthSnapshot(
                id = 2L,
                user = testUser,
                totalAssets = BigDecimal("26000.00"),
                totalLiabilities = BigDecimal("1000.00"),
                netWorth = BigDecimal("25000.00"),
                snapshotDate = LocalDate.of(2024, 1, 16)
            )
        )
        whenever(snapshotRepository.findByUserIdOrderBySnapshotDateDesc(userId))
            .thenReturn(snapshots)

        // Act
        val result = snapshotService.getSnapshotsByUserId(userId)

        // Assert
        assertEquals(2, result.size)
        assertEquals(LocalDate.of(2024, 1, 16), result[0].snapshotDate)
        assertEquals(LocalDate.of(2024, 1, 15), result[1].snapshotDate)
        verify(snapshotRepository).findByUserIdOrderBySnapshotDateDesc(userId)
    }

    @Test
    fun `getSnapshotByUserIdAndDate should return snapshot if found`() {
        // Arrange
        val userId = 1L
        val date = LocalDate.of(2024, 1, 15)
        val snapshot = NetworthSnapshot(
            id = 1L,
            user = testUser,
            totalAssets = BigDecimal("25000.00"),
            totalLiabilities = BigDecimal("1000.00"),
            netWorth = BigDecimal("24000.00"),
            snapshotDate = date
        )
        whenever(snapshotRepository.findByUserIdAndSnapshotDate(userId, date))
            .thenReturn(Optional.of(snapshot))

        // Act
        val result = snapshotService.getSnapshotByUserIdAndDate(userId, date)

        // Assert
        assertNotNull(result)
        assertEquals(snapshot.id, result.id)
        verify(snapshotRepository).findByUserIdAndSnapshotDate(userId, date)
    }

    @Test
    fun `getSnapshotByUserIdAndDate should throw exception if not found`() {
        // Arrange
        val userId = 1L
        val date = LocalDate.of(2024, 1, 15)
        whenever(snapshotRepository.findByUserIdAndSnapshotDate(userId, date))
            .thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            snapshotService.getSnapshotByUserIdAndDate(userId, date)
        }
        assertEquals("Snapshot not found for user $userId on $date", exception.message)
        verify(snapshotRepository).findByUserIdAndSnapshotDate(userId, date)
    }

    @Test
    fun `getSnapshotsByUserIdAndDateRange should return snapshots in date range`() {
        // Arrange
        val userId = 1L
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)
        val snapshots = listOf(
            NetworthSnapshot(
                id = 1L,
                user = testUser,
                totalAssets = BigDecimal("25000.00"),
                totalLiabilities = BigDecimal("1000.00"),
                netWorth = BigDecimal("24000.00"),
                snapshotDate = LocalDate.of(2024, 1, 15)
            )
        )
        whenever(snapshotRepository.findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            userId, startDate, endDate
        )).thenReturn(snapshots)

        // Act
        val result = snapshotService.getSnapshotsByUserIdAndDateRange(userId, startDate, endDate)

        // Assert
        assertEquals(1, result.size)
        verify(snapshotRepository).findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            userId, startDate, endDate
        )
    }

    @Test
    fun `calculateCurrentNetWorth should return correct net worth`() {
        // Arrange
        val userId = 1L
        whenever(accountRepository.findByUserId(userId)).thenReturn(testAccounts)

        // Act
        val result = snapshotService.calculateCurrentNetWorth(userId)

        // Assert
        assertEquals(BigDecimal("24000.00"), result)
        verify(accountRepository).findByUserId(userId)
    }

    @Test
    fun `calculateCurrentNetWorth should handle zero accounts`() {
        // Arrange
        val userId = 1L
        whenever(accountRepository.findByUserId(userId)).thenReturn(emptyList())

        // Act
        val result = snapshotService.calculateCurrentNetWorth(userId)

        // Assert
        assertEquals(BigDecimal.ZERO, result)
        verify(accountRepository).findByUserId(userId)
    }

    @Test
    fun `calculateCurrentNetWorth should handle only assets`() {
        // Arrange
        val userId = 1L
        val assetOnlyAccounts = listOf(
            Account(
                id = 1L,
                user = testUser,
                name = "Checking",
                type = AccountType.CHECKING,
                category = AccountCategory.ASSET,
                currency = "USD",
                currentBalance = BigDecimal("5000.00"),
                createdAt = LocalDateTime.now()
            )
        )
        whenever(accountRepository.findByUserId(userId)).thenReturn(assetOnlyAccounts)

        // Act
        val result = snapshotService.calculateCurrentNetWorth(userId)

        // Assert
        assertEquals(BigDecimal("5000.00"), result)
    }

    @Test
    fun `calculateCurrentNetWorth should handle only liabilities`() {
        // Arrange
        val userId = 1L
        val liabilityOnlyAccounts = listOf(
            Account(
                id = 1L,
                user = testUser,
                name = "Credit Card",
                type = AccountType.CREDIT_CARD,
                category = AccountCategory.LIABILITY,
                currency = "USD",
                currentBalance = BigDecimal("1000.00"),
                createdAt = LocalDateTime.now()
            )
        )
        whenever(accountRepository.findByUserId(userId)).thenReturn(liabilityOnlyAccounts)

        // Act
        val result = snapshotService.calculateCurrentNetWorth(userId)

        // Assert
        assertEquals(BigDecimal("-1000.00"), result)
    }
}
