package com.networth.api.repository

import com.networth.api.entity.Account
import com.networth.api.model.AccountCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.List

@Repository
interface AccountRepository : JpaRepository<Account, Long> {
    fun findByUserId(userId: Long): List<Account>
    fun findByUserIdAndCategory(userId: Long, category: AccountCategory): List<Account>
}
