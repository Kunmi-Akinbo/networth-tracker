package com.networth.api.entity

import com.networth.api.model.GoalStatus
import jakarta.persistence.*
import java.time.LocalDate
import java.math.BigDecimal

@Entity
@Table(name = "goals")
data class Goal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    val name: String,

    @Column(name = "target_amount", nullable = false, precision = 19, scale = 4)
    val targetAmount: BigDecimal,

    @Column(name = "current_amount", nullable = false, precision = 19, scale = 4)
    val currentAmount: BigDecimal = BigDecimal.ZERO,

    @Column
    val deadline: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: GoalStatus = GoalStatus.ACTIVE,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: java.time.LocalDateTime? = null
)
