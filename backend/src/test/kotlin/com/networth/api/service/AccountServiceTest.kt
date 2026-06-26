package com.networth.api.service

import com.networth.api.dto.AccountRequest
import com.networth.api.entity.Account
import com.networth.api.entity.User
import com.networth.api.model.AccountCategory
import com.networth.api.model.AccountType
import com.networth.api.repository.AccountRepository
import com.networth.api.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AccountServiceTest {

    @Mock
    private lateinit var accountRepository: AccountRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var accountService: AccountService

    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        testUser = User(
            id = 1L,
            email = "test@example.com",
            passwordHash = "hashed",
            createdAt = LocalDateTime.now()
        )
    }

    @Test
    fun `createAccount should create new account successfully`() {
        // Arrange
        val userId = 1L
        val request = AccountRequest(
            name = "Checking Account",
            type = AccountType.CHECKING,
            category = AccountCategory.ASSET,
            currency = "USD"
        )
        val savedAccount = Account(
            id = 1L,
            user = testUser,
            name = request.name,
            type = request.type,
            category = request.category,
            currency = request.currency,
            createdAt = LocalDateTime.now()
        )

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(testUser))
        whenever(accountRepository.save(any())).thenReturn(savedAccount)

        // Act
        val response = accountService.createAccount(userId, request)

        // Assert
        assertNotNull(response)
        assertEquals(1L, response.id)
        assertEquals(request.name, response.name)
        assertEquals(request.type, response.type)
        assertEquals(request.category, response.category)
        assertEquals(request.currency, response.currency)
        verify(userRepository).findById(userId)
        verify(accountRepository).save(any())
    }

    @Test
    fun `createAccount should throw exception when user not found`() {
        // Arrange
        val userId = 999L
        val request = AccountRequest(
            name = "Checking Account",
            type = AccountType.CHECKING,
            category = AccountCategory.ASSET,
            currency = "USD"
        )
        whenever(userRepository.findById(userId)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            accountService.createAccount(userId, request)
        }
        assertEquals("User not found", exception.message)
        verify(userRepository).findById(userId)
        verify(accountRepository, org.mockito.Mockito.never()).save(any())
    }

    @Test
    fun `getAccountsByUserId should return list of accounts`() {
        // Arrange
        val userId = 1L
        val accounts = listOf(
            Account(
                id = 1L,
                user = testUser,
                name = "Checking",
                type = AccountType.CHECKING,
                category = AccountCategory.ASSET,
                currency = "USD",
                createdAt = LocalDateTime.now()
            ),
            Account(
                id = 2L,
                user = testUser,
                name = "Savings",
                type = AccountType.SAVINGS,
                category = AccountCategory.ASSET,
                currency = "USD",
                createdAt = LocalDateTime.now()
            )
        )
        whenever(accountRepository.findByUserId(userId)).thenReturn(accounts)

        // Act
        val responses = accountService.getAccountsByUserId(userId)

        // Assert
        assertEquals(2, responses.size)
        assertEquals("Checking", responses[0].name)
        assertEquals("Savings", responses[1].name)
        verify(accountRepository).findByUserId(userId)
    }

    @Test
    fun `getAccountById should return account when found`() {
        // Arrange
        val accountId = 1L
        val account = Account(
            id = accountId,
            user = testUser,
            name = "Checking",
            type = AccountType.CHECKING,
            category = AccountCategory.ASSET,
            currency = "USD",
            createdAt = LocalDateTime.now()
        )
        whenever(accountRepository.findById(accountId)).thenReturn(Optional.of(account))

        // Act
        val response = accountService.getAccountById(accountId)

        // Assert
        assertNotNull(response)
        assertEquals(accountId, response.id)
        assertEquals("Checking", response.name)
        verify(accountRepository).findById(accountId)
    }

    @Test
    fun `getAccountById should throw exception when not found`() {
        // Arrange
        val accountId = 999L
        whenever(accountRepository.findById(accountId)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            accountService.getAccountById(accountId)
        }
        assertEquals("Account not found", exception.message)
        verify(accountRepository).findById(accountId)
    }

    @Test
    fun `updateAccount should update account successfully`() {
        // Arrange
        val accountId = 1L
        val request = AccountRequest(
            name = "Updated Account",
            type = AccountType.SAVINGS,
            category = AccountCategory.ASSET,
            currency = "EUR"
        )
        val existingAccount = Account(
            id = accountId,
            user = testUser,
            name = "Old Account",
            type = AccountType.CHECKING,
            category = AccountCategory.ASSET,
            currency = "USD",
            createdAt = LocalDateTime.now()
        )
        val updatedAccount = existingAccount.copy(
            name = request.name,
            type = request.type,
            category = request.category,
            currency = request.currency,
            updatedAt = LocalDateTime.now()
        )

        whenever(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount))
        whenever(accountRepository.save(any())).thenReturn(updatedAccount)

        // Act
        val response = accountService.updateAccount(accountId, request)

        // Assert
        assertNotNull(response)
        assertEquals(request.name, response.name)
        assertEquals(request.type, response.type)
        assertEquals(request.category, response.category)
        assertEquals(request.currency, response.currency)
        verify(accountRepository).findById(accountId)
        verify(accountRepository).save(any())
    }

    @Test
    fun `updateAccount should throw exception when account not found`() {
        // Arrange
        val accountId = 999L
        val request = AccountRequest(
            name = "Updated Account",
            type = AccountType.SAVINGS,
            category = AccountCategory.ASSET,
            currency = "EUR"
        )
        whenever(accountRepository.findById(accountId)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            accountService.updateAccount(accountId, request)
        }
        assertEquals("Account not found", exception.message)
        verify(accountRepository).findById(accountId)
        verify(accountRepository, org.mockito.Mockito.never()).save(any())
    }

    @Test
    fun `deleteAccount should delete account successfully`() {
        // Arrange
        val accountId = 1L

        // Act
        accountService.deleteAccount(accountId)

        // Assert
        verify(accountRepository).deleteById(accountId)
    }

    @Test
    fun `getAccountsByCategory should return filtered accounts`() {
        // Arrange
        val userId = 1L
        val category = "ASSET"
        val accounts = listOf(
            Account(
                id = 1L,
                user = testUser,
                name = "Checking",
                type = AccountType.CHECKING,
                category = AccountCategory.ASSET,
                currency = "USD",
                createdAt = LocalDateTime.now()
            )
        )
        whenever(accountRepository.findByUserIdAndCategory(userId, AccountCategory.ASSET))
            .thenReturn(accounts)

        // Act
        val responses = accountService.getAccountsByCategory(userId, category)

        // Assert
        assertEquals(1, responses.size)
        assertEquals("Checking", responses[0].name)
        assertEquals(AccountCategory.ASSET, responses[0].category)
        verify(accountRepository).findByUserIdAndCategory(userId, AccountCategory.ASSET)
    }
}
