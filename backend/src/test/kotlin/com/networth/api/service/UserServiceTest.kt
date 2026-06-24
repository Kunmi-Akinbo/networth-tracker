package com.networth.api.service

import com.networth.api.dto.RegisterRequest
import com.networth.api.entity.User
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
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `register should create new user successfully`() {
        // Arrange
        val request = RegisterRequest(email = "test@example.com", password = "password123")
        val hashedPassword = "hashed_password"
        val savedUser = User(
            id = 1L,
            email = request.email,
            passwordHash = hashedPassword,
            createdAt = LocalDateTime.now()
        )

        whenever(userRepository.existsByEmail(request.email)).thenReturn(false)
        whenever(passwordEncoder.encode(request.password)).thenReturn(hashedPassword)
        whenever(userRepository.save(any())).thenReturn(savedUser)

        // Act
        val response = userService.register(request)

        // Assert
        assertNotNull(response)
        assertEquals(1L, response.id)
        assertEquals(request.email, response.email)
        verify(userRepository).existsByEmail(request.email)
        verify(passwordEncoder).encode(request.password)
        verify(userRepository).save(any())
    }

    @Test
    fun `register should throw exception when email already exists`() {
        // Arrange
        val request = RegisterRequest(email = "test@example.com", password = "password123")
        whenever(userRepository.existsByEmail(request.email)).thenReturn(true)

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            userService.register(request)
        }
        assertEquals("Email already registered", exception.message)
        verify(userRepository).existsByEmail(request.email)
        verify(passwordEncoder, org.mockito.Mockito.never()).encode(any())
        verify(userRepository, org.mockito.Mockito.never()).save(any())
    }

    @Test
    fun `findByEmail should return user when found`() {
        // Arrange
        val email = "test@example.com"
        val user = User(
            id = 1L,
            email = email,
            passwordHash = "hashed",
            createdAt = LocalDateTime.now()
        )
        whenever(userRepository.findByEmail(email)).thenReturn(Optional.of(user))

        // Act
        val response = userService.findByEmail(email)

        // Assert
        assertNotNull(response)
        assertEquals(1L, response.id)
        assertEquals(email, response.email)
        verify(userRepository).findByEmail(email)
    }

    @Test
    fun `findByEmail should return null when not found`() {
        // Arrange
        val email = "notfound@example.com"
        whenever(userRepository.findByEmail(email)).thenReturn(Optional.empty())

        // Act
        val response = userService.findByEmail(email)

        // Assert
        assertNull(response)
        verify(userRepository).findByEmail(email)
    }

    @Test
    fun `findById should return user when found`() {
        // Arrange
        val userId = 1L
        val user = User(
            id = userId,
            email = "test@example.com",
            passwordHash = "hashed",
            createdAt = LocalDateTime.now()
        )
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))

        // Act
        val response = userService.findById(userId)

        // Assert
        assertNotNull(response)
        assertEquals(userId, response.id)
        verify(userRepository).findById(userId)
    }

    @Test
    fun `findById should return null when not found`() {
        // Arrange
        val userId = 999L
        whenever(userRepository.findById(userId)).thenReturn(Optional.empty())

        // Act
        val response = userService.findById(userId)

        // Assert
        assertNull(response)
        verify(userRepository).findById(userId)
    }

    @Test
    fun `updatePassword should update user password successfully`() {
        // Arrange
        val userId = 1L
        val newPassword = "newpassword123"
        val hashedNewPassword = "new_hashed_password"
        val existingUser = User(
            id = userId,
            email = "test@example.com",
            passwordHash = "old_hashed",
            createdAt = LocalDateTime.now()
        )
        val updatedUser = existingUser.copy(
            passwordHash = hashedNewPassword,
            updatedAt = LocalDateTime.now()
        )

        whenever(userRepository.findById(userId)).thenReturn(Optional.of(existingUser))
        whenever(passwordEncoder.encode(newPassword)).thenReturn(hashedNewPassword)
        whenever(userRepository.save(any())).thenReturn(updatedUser)

        // Act
        val response = userService.updatePassword(userId, newPassword)

        // Assert
        assertNotNull(response)
        assertEquals(userId, response.id)
        verify(userRepository).findById(userId)
        verify(passwordEncoder).encode(newPassword)
        verify(userRepository).save(any())
    }

    @Test
    fun `updatePassword should throw exception when user not found`() {
        // Arrange
        val userId = 999L
        val newPassword = "newpassword123"
        whenever(userRepository.findById(userId)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            userService.updatePassword(userId, newPassword)
        }
        assertEquals("User not found", exception.message)
        verify(userRepository).findById(userId)
        verify(passwordEncoder, org.mockito.Mockito.never()).encode(any())
        verify(userRepository, org.mockito.Mockito.never()).save(any())
    }

    @Test
    fun `deleteById should delete user successfully`() {
        // Arrange
        val userId = 1L

        // Act
        userService.deleteById(userId)

        // Assert
        verify(userRepository).deleteById(userId)
    }
}
