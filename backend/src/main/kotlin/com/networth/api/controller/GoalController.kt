package com.networth.api.controller

import com.networth.api.dto.GoalRequest
import com.networth.api.dto.GoalResponse
import com.networth.api.model.GoalStatus
import com.networth.api.service.GoalService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/goals")
class GoalController(
    private val goalService: GoalService
) {

    @PostMapping
    fun createGoal(
        @RequestParam userId: Long,
        @RequestBody request: GoalRequest
    ): ResponseEntity<GoalResponse> {
        val goal = goalService.createGoal(userId, request)
        return ResponseEntity.ok(goal)
    }

    @GetMapping("/{id}")
    fun getGoalById(@PathVariable id: Long): ResponseEntity<GoalResponse> {
        val goal = goalService.getGoalById(id)
        return ResponseEntity.ok(goal)
    }

    @GetMapping
    fun getGoalsByUserId(@RequestParam userId: Long): ResponseEntity<List<GoalResponse>> {
        val goals = goalService.getGoalsByUserId(userId)
        return ResponseEntity.ok(goals)
    }

    @GetMapping("/user/{userId}/status/{status}")
    fun getGoalsByUserIdAndStatus(
        @PathVariable userId: Long,
        @PathVariable status: GoalStatus
    ): ResponseEntity<List<GoalResponse>> {
        val goals = goalService.getGoalsByUserIdAndStatus(userId, status)
        return ResponseEntity.ok(goals)
    }

    @GetMapping("/user/{userId}/active")
    fun getActiveGoalsByUserId(@PathVariable userId: Long): ResponseEntity<List<GoalResponse>> {
        val goals = goalService.getActiveGoalsByUserId(userId)
        return ResponseEntity.ok(goals)
    }

    @PutMapping("/{id}")
    fun updateGoal(
        @PathVariable id: Long,
        @RequestBody request: GoalRequest
    ): ResponseEntity<GoalResponse> {
        val goal = goalService.updateGoal(id, request)
        return ResponseEntity.ok(goal)
    }

    @PatchMapping("/{id}/progress")
    fun updateGoalProgress(
        @PathVariable id: Long,
        @RequestParam currentAmount: BigDecimal
    ): ResponseEntity<GoalResponse> {
        val goal = goalService.updateGoalProgress(id, currentAmount)
        return ResponseEntity.ok(goal)
    }

    @DeleteMapping("/{id}")
    fun deleteGoal(@PathVariable id: Long): ResponseEntity<Void> {
        goalService.deleteGoal(id)
        return ResponseEntity.noContent().build()
    }
}
