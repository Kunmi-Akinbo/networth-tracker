package com.networth.api.service

import com.networth.api.dto.GoalRequest
import com.networth.api.dto.GoalResponse
import com.networth.api.entity.Goal
import com.networth.api.entity.User
import com.networth.api.model.GoalStatus
import com.networth.api.repository.GoalRepository
import com.networth.api.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Service
class GoalService(
    private val goalRepository: GoalRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createGoal(userId: Long, request: GoalRequest): GoalResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found with id: $userId") }

        val goal = Goal(
            user = user,
            name = request.name,
            targetAmount = request.targetAmount,
            currentAmount = request.currentAmount,
            deadline = request.deadline,
            status = GoalStatus.ACTIVE,
            createdAt = LocalDateTime.now()
        )

        val savedGoal = goalRepository.save(goal)
        return mapToResponse(savedGoal)
    }

    @Transactional
    fun updateGoal(goalId: Long, request: GoalRequest): GoalResponse {
        val goal = goalRepository.findById(goalId)
            .orElseThrow { IllegalArgumentException("Goal not found with id: $goalId") }

        val updatedGoal = goal.copy(
            name = request.name,
            targetAmount = request.targetAmount,
            currentAmount = request.currentAmount,
            deadline = request.deadline,
            updatedAt = LocalDateTime.now()
        )

        // Update status based on progress
        val goalWithStatus = updateGoalStatus(updatedGoal)
        
        val savedGoal = goalRepository.save(goalWithStatus)
        return mapToResponse(savedGoal)
    }

    @Transactional
    fun updateGoalProgress(goalId: Long, currentAmount: BigDecimal): GoalResponse {
        val goal = goalRepository.findById(goalId)
            .orElseThrow { IllegalArgumentException("Goal not found with id: $goalId") }

        val updatedGoal = goal.copy(
            currentAmount = currentAmount,
            updatedAt = LocalDateTime.now()
        )

        val goalWithStatus = updateGoalStatus(updatedGoal)
        
        val savedGoal = goalRepository.save(goalWithStatus)
        return mapToResponse(savedGoal)
    }

    @Transactional
    fun deleteGoal(goalId: Long) {
        if (!goalRepository.existsById(goalId)) {
            throw IllegalArgumentException("Goal not found with id: $goalId")
        }
        goalRepository.deleteById(goalId)
    }

    fun getGoalById(goalId: Long): GoalResponse {
        val goal = goalRepository.findById(goalId)
            .orElseThrow { IllegalArgumentException("Goal not found with id: $goalId") }
        return mapToResponse(goal)
    }

    fun getGoalsByUserId(userId: Long): List<GoalResponse> {
        val goals = goalRepository.findByUserId(userId)
        return goals.map { mapToResponse(it) }
    }

    fun getGoalsByUserIdAndStatus(userId: Long, status: GoalStatus): List<GoalResponse> {
        val goals = goalRepository.findByUserIdAndStatus(userId, status)
        return goals.map { mapToResponse(it) }
    }

    fun getActiveGoalsByUserId(userId: Long): List<GoalResponse> {
        val goals = goalRepository.findByUserIdAndStatusOrderByDeadlineAsc(userId, GoalStatus.ACTIVE)
        return goals.map { mapToResponse(it) }
    }

    private fun updateGoalStatus(goal: Goal): Goal {
        return when {
            goal.currentAmount >= goal.targetAmount -> {
                goal.copy(status = GoalStatus.COMPLETED, updatedAt = LocalDateTime.now())
            }
            goal.deadline != null && LocalDateTime.now().toLocalDate().isAfter(goal.deadline) -> {
                goal.copy(status = GoalStatus.OVERDUE, updatedAt = LocalDateTime.now())
            }
            else -> goal
        }
    }

    private fun mapToResponse(goal: Goal): GoalResponse {
        val progressPercentage = if (goal.targetAmount > BigDecimal.ZERO) {
            goal.currentAmount.divide(goal.targetAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        val remainingAmount = goal.targetAmount.subtract(goal.currentAmount).max(BigDecimal.ZERO)

        return GoalResponse(
            id = goal.id!!,
            name = goal.name,
            targetAmount = goal.targetAmount,
            currentAmount = goal.currentAmount,
            deadline = goal.deadline,
            status = goal.status,
            progressPercentage = progressPercentage,
            remainingAmount = remainingAmount,
            createdAt = goal.createdAt,
            updatedAt = goal.updatedAt
        )
    }
}
