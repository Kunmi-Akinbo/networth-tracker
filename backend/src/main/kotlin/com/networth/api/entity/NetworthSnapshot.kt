package com.networth.api.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.math.BigDecimal

@Entity
@Table(name = "networth_snapshots")
data class NetworthSnapshot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "total_assets", nullable = false, precision = 19, scale = 4)
    val totalAssets: BigDecimal,

    @Column(name = "total_liabilities", nullable = false, precision = 19, scale = 4)
    val totalLiabilities: BigDecimal,

    @Column(name = "net_worth", nullable = false, precision = 19, scale = 4)
    val netWorth: BigDecimal,

    @Column(name = "snapshot_date", nullable = false)
    val snapshotDate: LocalDate
)
