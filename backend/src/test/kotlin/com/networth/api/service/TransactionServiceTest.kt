package com.networth.api.service

import com.networth.api.dto.TransactionRequest
import com.networth.api.entity.Account
import com.networth.api.entity.Transaction
import com.networth.api.entity.User
import com.networth.api.model.TransactionType
import com.networth.api.repository.AccountRepository
import com.networth.api.repository.TransactionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TransactionServiceTest {

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @Mock
    private lateinit var accountRepository: AccountRepository

    @InjectMocks
    private lateinit var transactionService: TransactionService

    private lateinit var testAccount: Account

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        val testUser = User(
            id = 1L,
            email = "test@example.com",
            passwordHash = "hashed",
            createdAt = LocalDateTime.now()
        )
        testAccount = Account(
            id = 1L,
            user = testUser,
            name = "Checking Account",
            type = com.networth.api.model.AccountType.CHECKING,
            category = com.networth.api.model.AccountCategory.ASSET,
            currency = "USD",
            createdAt = LocalDateTime.now()
        )
    }

    @Test
    fun `createTransaction should create new transaction successfully`() {
        // Arrange
        val accountId = 1L
        val request = TransactionRequest(
            description = "Grocery shopping",
            amount = BigDecimal("50.00"),
            type = TransactionType.EXPENSE,
            date = LocalDate.now(),
            category = "Groceries",
            notes = "Weekly groceries"
        )
        val savedTransaction = Transaction(
            id = 1L,
            account = testAccount,
            description = request.description,
            amount = request.amount,
            type = request.type,
            date = request.date,
            category = request.category,
            notes = request.notes,
            createdAt = LocalDateTime.now()
        )

        whenever(accountRepository.findById(accountId)).thenReturn(Optional.of(testAccount))
        whenever(transactionRepository.save(any())).thenReturn(savedTransaction)

        // Act
        val response = transactionService.createTransaction(accountId, request)

        // Assert
        assertNotNull(response)
        assertEquals(1L, response.id)
        assertEquals(request.description, response.description)
        assertEquals(request.amount, response.amount)
        assertEquals(request.type, response.type)
        assertEquals(request.date, response.date)
        verify(accountRepository).findById(accountId)
        verify(transactionRepository).save(any())
    }

    @Test
    fun `createTransaction should throw exception when account not found`() {
        // Arrange
        val accountId = 999L
        val request = TransactionRequest(
            description = "Grocery shopping",
            amount = BigDecimal("50.00"),
            type = TransactionType.EXPENSE,
            date = LocalDate.now()
        )
        whenever(accountRepository.findById(accountId)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            transactionService.createTransaction(accountId, request)
        }
        assertEquals("Account not found", exception.message)
        verify(accountRepository).findById(accountId)
        verify(transactionRepository, org.mockito.Mockito.never()).save(any())
    }

    @Test
    fun `getTransactionsByAccountId should return list of transactions`() {
        // Arrange
        val accountId = 1L
        val transactions = listOf(
            Transaction(
                id = 1L,
                account = testAccount,
                description = "Grocery shopping",
                amount = BigDecimal("50.00"),
                type = TransactionType.EXPENSE,
                date = LocalDate.now(),
                createdAt = LocalDateTime.now()
            ),
            Transaction(
                id = 2L,
                account = testAccount,
                description = "Salary",
                amount = BigDecimal("5000.00"),
                type = TransactionType.INCOME,
                date = LocalDate.now(),
                createdAt = LocalDateTime.now()
            )
        )
        whenever(transactionRepository.findByAccountId(accountId)).thenReturn(transactions)

        // Act
        val responses = transactionService.getTransactionsByAccountId(accountId)

        // Assert
        assertEquals(2, responses.size)
        assertEquals("Grocery shopping", responses[0].description)
        assertEquals("Salary", responses[1].description)
        verify(transactionRepository).findByAccountId(accountId)
    }

    @Test
    fun `getTransactionById should return transaction when found`() {
        // Arrange
        val transactionId = 1L
        val transaction = Transaction(
            id = transactionId,
            account = testAccount,
            description = "Grocery shopping",
            amount = BigDecimal("50.00"),
            type = TransactionType.EXPENSE,
            date = LocalDate.now(),
            createdAt = LocalDateTime.now()
        )
        whenever(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction))

        // Act
        val response = transactionService.getTransactionById(transactionId)

        // Assert
        assertNotNull(response)
        assertEquals(transactionId, response.id)
        assertEquals("Grocery shopping", response.description)
        verify(transactionRepository).findById(transactionId)
    }

    @Test
    fun `getTransactionById should throw exception when not found`() {
        // Arrange
        val transactionId = 999L
        whenever(transactionRepository.findById(transactionId)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            transactionService.getTransactionById(transactionId)
        }
        assertEquals("Transaction not found", exception.message)
        verify(transactionRepository).findById(transactionId)
    }

    @Test
    fun `getTransactionsByAccountIdAndDateRange should return filtered transactions`() {
        // Arrange
        val accountId = 1L
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 31)
        val transactions = listOf(
            Transaction(
                id = 1L,
                account = testAccount,
                description = "Grocery shopping",
                amount = BigDecimal("50.00"),
                type = TransactionType.EXPENSE,
                date = LocalDate.of(2024, 1, 15),
                createdAt = LocalDateTime.now()
            )
        )
        whenever(transactionRepository.findByAccountIdAndDateBetween(accountId, startDate, endDate))
            .thenReturn(transactions)

        // Act
        val responses = transactionService.getTransactionsByAccountIdAndDateRange(accountId, startDate, endDate)

        // Assert
        assertEquals(1, responses.size)
        assertEquals("Grocery shopping", responses[0].description)
        verify(transactionRepository).findByAccountIdAndDateBetween(accountId, startDate, endDate)
    }

    @Test
    fun `updateTransaction should update transaction successfully`() {
        // Arrange
        val transactionId = 1L
        val request = TransactionRequest(
            description = "Updated transaction",
            amount = BigDecimal("75.00"),
            type = TransactionType.EXPENSE,
            date = LocalDate.now(),
            category = "Food",
            notes = "Updated notes"
        )
        val existingTransaction = Transaction(
            id = transactionId,
            account = testAccount,
            description = "Old transaction",
            amount = BigDecimal("50.00"),
            type = TransactionType.EXPENSE,
            date = LocalDate.now(),
            createdAt = LocalDateTime.now()
        )
        val updatedTransaction = existingTransaction.copy(
            description = request.description,
            amount = request.amount,
            type = request.type,
            date = request.date,
            category = request.category,
            notes = request.notes,
            updatedAt = LocalDateTime.now()
        )

        whenever(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existingTransaction))
        whenever(transactionRepository.save(any())).thenReturn(updatedTransaction)

        // Act
        val response = transactionService.updateTransaction(transactionId, request)

        // Assert
        assertNotNull(response)
        assertEquals(request.description, response.description)
        assertEquals(request.amount, response.amount)
        assertEquals(request.category, response.category)
        verify(transactionRepository).findById(transactionId)
        verify(transactionRepository).save(any())
    }

    @Test
    fun `updateTransaction should throw exception when transaction not found`() {
        // Arrange
        val transactionId = 999L
        val request = TransactionRequest(
            description = "Updated transaction",
            amount = BigDecimal("75.00"),
            type = TransactionType.EXPENSE,
            date = LocalDate.now()
        )
        whenever(transactionRepository.findById(transactionId)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            transactionService.updateTransaction(transactionId, request)
        }
        assertEquals("Transaction not found", exception.message)
        verify(transactionRepository).findById(transactionId)
        verify(transactionRepository, org.mockito.Mockito.never()).save(any())
    }

    @Test
    fun `deleteTransaction should delete transaction successfully`() {
        // Arrange
        val transactionId = 1L

        // Act
        transactionService.deleteTransaction(transactionId)

        // Assert
        verify(transactionRepository).deleteById(transactionId)
    }
}
