package com.networth.api.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.math.BigDecimal

@Entity
@Table(name = "account_balances")
data class AccountBalance(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    val account: Account,

    @Column(nullable = false, precision = 19, scale = 4)
    val balance: BigDecimal,

    @Column(name = "recorded_at", nullable = false)
    val recordedAt: LocalDateTime = LocalDateTime.now()
)
