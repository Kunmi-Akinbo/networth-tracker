package com.networth.api.service

import com.networth.api.dto.GoalRequest
import com.networth.api.dto.GoalResponse
import com.networth.api.entity.Goal
import com.networth.api.entity.User
import com.networth.api.model.GoalStatus
import com.networth.api.repository.GoalRepository
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
import kotlin.test.assertTrue

class GoalServiceTest {

    @Mock
    private lateinit var goalRepository: GoalRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var goalService: GoalService

    private lateinit var testUser: User
    private lateinit var testGoal: Goal

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        testUser = User(
            id = 1L,
            email = "test@example.com",
            passwordHash = "hashed",
            createdAt = LocalDateTime.now()
        )
        testGoal = Goal(
            id = 1L,
            user = testUser,
            name = "Emergency Fund",
            targetAmount = BigDecimal("10000.00"),
            currentAmount = BigDecimal("5000.00"),
            deadline = LocalDate.now().plusMonths(6),
            status = GoalStatus.ACTIVE,
            createdAt = LocalDateTime.now()
        )
    }

    @Test
    fun `createGoal should create new goal successfully`() {
        // Arrange
        val userId = 1L
        val request = GoalRequest(
            name = "Emergency Fund",
            targetAmount = BigDecimal("10000.00"),
            currentAmount = BigDecimal("5000.00"),
            deadline = LocalDate.now().plusMonths(6)
        )
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(testUser))
        whenever(goalRepository.save(any())).thenReturn(testGoal)

        // Act
        val result = goalService.createGoal(userId, request)

        // Assert
        assertNotNull(result)
        assertEquals(testGoal.id, result.id)
        assertEquals(request.name, result.name)
        assertEquals(request.targetAmount, result.targetAmount)
        verify(userRepository).findById(userId)
        verify(goalRepository).save(any())
    }

    @Test
    fun `createGoal should throw exception for invalid user`() {
        // Arrange
        val userId = 999L
        val request = GoalRequest(
            name = "Emergency Fund",
            targetAmount = BigDecimal("10000.00")
        )
        whenever(userRepository.findById(userId)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            goalService.createGoal(userId, request)
        }
        assertEquals("User not found with id: $userId", exception.message)
        verify(userRepository).findById(userId)
        verify(goalRepository, never()).save(any())
    }

    @Test
    fun `updateGoal should update goal successfully`() {
        // Arrange
        val goalId = 1L
        val request = GoalRequest(
            name = "Updated Goal",
            targetAmount = BigDecimal("15000.00"),
            currentAmount = BigDecimal("7000.00"),
            deadline = LocalDate.now().plusMonths(12)
        )
        whenever(goalRepository.findById(goalId)).thenReturn(Optional.of(testGoal))
        whenever(goalRepository.save(any())).thenReturn(testGoal)

        // Act
        val result = goalService.updateGoal(goalId, request)

        // Assert
        assertNotNull(result)
        verify(goalRepository).findById(goalId)
        verify(goalRepository).save(any())
    }

    @Test
    fun `updateGoal should throw exception for invalid goal id`() {
        // Arrange
        val goalId = 999L
        val request = GoalRequest(
            name = "Updated Goal",
            targetAmount = BigDecimal("15000.00")
        )
        whenever(goalRepository.findById(goalId)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            goalService.updateGoal(goalId, request)
        }
        assertEquals("Goal not found with id: $goalId", exception.message)
        verify(goalRepository).findById(goalId)
        verify(goalRepository, never()).save(any())
    }

    @Test
    fun `updateGoalProgress should update progress successfully`() {
        // Arrange
        val goalId = 1L
        val currentAmount = BigDecimal("8000.00")
        whenever(goalRepository.findById(goalId)).thenReturn(Optional.of(testGoal))
        whenever(goalRepository.save(any())).thenReturn(testGoal)

        // Act
        val result = goalService.updateGoalProgress(goalId, currentAmount)

        // Assert
        assertNotNull(result)
        verify(goalRepository).findById(goalId)
        verify(goalRepository).save(any())
    }

    @Test
    fun `updateGoalProgress should mark goal as completed when target reached`() {
        // Arrange
        val goalId = 1L
        val currentAmount = BigDecimal("10000.00")
        val completedGoal = testGoal.copy(
            currentAmount = currentAmount,
            status = GoalStatus.COMPLETED,
            updatedAt = LocalDateTime.now()
        )
        whenever(goalRepository.findById(goalId)).thenReturn(Optional.of(testGoal))
        whenever(goalRepository.save(any())).thenReturn(completedGoal)

        // Act
        val result = goalService.updateGoalProgress(goalId, currentAmount)

        // Assert
        assertNotNull(result)
        assertEquals(GoalStatus.COMPLETED, result.status)
        assertEquals(BigDecimal("100.00"), result.progressPercentage)
    }

    @Test
    fun `updateGoalProgress should mark goal as overdue when deadline passed`() {
        // Arrange
        val goalId = 1L
        val currentAmount = BigDecimal("5000.00")
        val overdueGoal = testGoal.copy(
            currentAmount = currentAmount,
            deadline = LocalDate.now().minusDays(1),
            status = GoalStatus.OVERDUE,
            updatedAt = LocalDateTime.now()
        )
        whenever(goalRepository.findById(goalId)).thenReturn(Optional.of(overdueGoal))
        whenever(goalRepository.save(any())).thenReturn(overdueGoal)

        // Act
        val result = goalService.updateGoalProgress(goalId, currentAmount)

        // Assert
        assertNotNull(result)
        assertEquals(GoalStatus.OVERDUE, result.status)
    }

    @Test
    fun `deleteGoal should delete goal successfully`() {
        // Arrange
        val goalId = 1L
        whenever(goalRepository.existsById(goalId)).thenReturn(true)

        // Act
        goalService.deleteGoal(goalId)

        // Assert
        verify(goalRepository).existsById(goalId)
        verify(goalRepository).deleteById(goalId)
    }

    @Test
    fun `deleteGoal should throw exception for invalid goal id`() {
        // Arrange
        val goalId = 999L
        whenever(goalRepository.existsById(goalId)).thenReturn(false)

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            goalService.deleteGoal(goalId)
        }
        assertEquals("Goal not found with id: $goalId", exception.message)
        verify(goalRepository).existsById(goalId)
        verify(goalRepository, never()).deleteById(any())
    }

    @Test
    fun `getGoalById should return goal successfully`() {
        // Arrange
        val goalId = 1L
        whenever(goalRepository.findById(goalId)).thenReturn(Optional.of(testGoal))

        // Act
        val result = goalService.getGoalById(goalId)

        // Assert
        assertNotNull(result)
        assertEquals(testGoal.id, result.id)
        assertEquals(testGoal.name, result.name)
        verify(goalRepository).findById(goalId)
    }

    @Test
    fun `getGoalById should throw exception for invalid goal id`() {
        // Arrange
        val goalId = 999L
        whenever(goalRepository.findById(goalId)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            goalService.getGoalById(goalId)
        }
        assertEquals("Goal not found with id: $goalId", exception.message)
        verify(goalRepository).findById(goalId)
    }

    @Test
    fun `getGoalsByUserId should return goals for user`() {
        // Arrange
        val userId = 1L
        val goals = listOf(testGoal)
        whenever(goalRepository.findByUserId(userId)).thenReturn(goals)

        // Act
        val result = goalService.getGoalsByUserId(userId)

        // Assert
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(testGoal.id, result[0].id)
        verify(goalRepository).findByUserId(userId)
    }

    @Test
    fun `getGoalsByUserIdAndStatus should return filtered goals`() {
        // Arrange
        val userId = 1L
        val goals = listOf(testGoal)
        whenever(goalRepository.findByUserIdAndStatus(userId, GoalStatus.ACTIVE))
            .thenReturn(goals)

        // Act
        val result = goalService.getGoalsByUserIdAndStatus(userId, GoalStatus.ACTIVE)

        // Assert
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(GoalStatus.ACTIVE, result[0].status)
        verify(goalRepository).findByUserIdAndStatus(userId, GoalStatus.ACTIVE)
    }

    @Test
    fun `getActiveGoalsByUserId should return active goals ordered by deadline`() {
        // Arrange
        val userId = 1L
        val goals = listOf(testGoal)
        whenever(goalRepository.findByUserIdAndStatusOrderByDeadlineAsc(userId, GoalStatus.ACTIVE))
            .thenReturn(goals)

        // Act
        val result = goalService.getActiveGoalsByUserId(userId)

        // Assert
        assertNotNull(result)
        assertEquals(1, result.size)
        assertEquals(GoalStatus.ACTIVE, result[0].status)
        verify(goalRepository).findByUserIdAndStatusOrderByDeadlineAsc(userId, GoalStatus.ACTIVE)
    }

    @Test
    fun `mapToResponse should calculate progress percentage correctly`() {
        // Arrange
        val goal = testGoal.copy(
            targetAmount = BigDecimal("10000.00"),
            currentAmount = BigDecimal("5000.00")
        )
        whenever(goalRepository.findById(1L)).thenReturn(Optional.of(goal))
        whenever(goalRepository.save(any())).thenReturn(goal)

        // Act
        val result = goalService.getGoalById(1L)

        // Assert
        assertEquals(BigDecimal("50.00"), result.progressPercentage)
    }

    @Test
    fun `mapToResponse should calculate remaining amount correctly`() {
        // Arrange
        val goal = testGoal.copy(
            targetAmount = BigDecimal("10000.00"),
            currentAmount = BigDecimal("5000.00")
        )
        whenever(goalRepository.findById(1L)).thenReturn(Optional.of(goal))
        whenever(goalRepository.save(any())).thenReturn(goal)

        // Act
        val result = goalService.getGoalById(1L)

        // Assert
        assertEquals(BigDecimal("5000.00"), result.remainingAmount)
    }

    @Test
    fun `mapToResponse should handle zero target amount`() {
        // Arrange
        val goal = testGoal.copy(
            targetAmount = BigDecimal.ZERO,
            currentAmount = BigDecimal("1000.00")
        )
        whenever(goalRepository.findById(1L)).thenReturn(Optional.of(goal))
        whenever(goalRepository.save(any())).thenReturn(goal)

        // Act
        val result = goalService.getGoalById(1L)

        // Assert
        assertEquals(BigDecimal.ZERO, result.progressPercentage)
    }

    @Test
    fun `mapToResponse should handle overachievement`() {
        // Arrange
        val goal = testGoal.copy(
            targetAmount = BigDecimal("10000.00"),
            currentAmount = BigDecimal("12000.00"),
            status = GoalStatus.COMPLETED
        )
        whenever(goalRepository.findById(1L)).thenReturn(Optional.of(goal))
        whenever(goalRepository.save(any())).thenReturn(goal)

        // Act
        val result = goalService.getGoalById(1L)

        // Assert
        assertEquals(BigDecimal("120.00"), result.progressPercentage)
        assertEquals(BigDecimal.ZERO, result.remainingAmount)
    }
}
