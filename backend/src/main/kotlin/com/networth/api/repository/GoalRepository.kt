package com.networth.api.repository

import com.networth.api.entity.Goal
import com.networth.api.model.GoalStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.List

@Repository
interface GoalRepository : JpaRepository<Goal, Long> {
    fun findByUserId(userId: Long): List<Goal>
    fun findByUserIdAndStatus(userId: Long, status: GoalStatus): List<Goal>
    fun findByUserIdAndStatusOrderByDeadlineAsc(userId: Long, status: GoalStatus): List<Goal>
}
