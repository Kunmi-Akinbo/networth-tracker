package com.networth.api.repository

import com.networth.api.entity.NetworthSnapshot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.List
import java.util.Optional

@Repository
interface NetworthSnapshotRepository : JpaRepository<NetworthSnapshot, Long> {
    fun findByUserIdOrderBySnapshotDateDesc(userId: Long): List<NetworthSnapshot>
    fun findByUserIdAndSnapshotDate(userId: Long, snapshotDate: LocalDate): Optional<NetworthSnapshot>
    fun findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<NetworthSnapshot>
}
