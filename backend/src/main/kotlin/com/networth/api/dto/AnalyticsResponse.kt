package com.networth.api.dto

import java.math.BigDecimal
import java.time.LocalDate

data class AnalyticsResponse(
    val currentNetWorth: BigDecimal,
    val netWorthChange: BigDecimal,
    val netWorthChangePercentage: BigDecimal,
    val totalAssets: BigDecimal,
    val totalLiabilities: BigDecimal,
    val assetBreakdown: List<CategoryBreakdown>,
    val liabilityBreakdown: List<CategoryBreakdown>,
    val incomeExpenseSummary: IncomeExpenseSummary,
    val netWorthTrend: List<TrendDataPoint>
)

data class CategoryBreakdown(
    val category: String,
    val amount: BigDecimal,
    val percentage: BigDecimal
)

data class IncomeExpenseSummary(
    val totalIncome: BigDecimal,
    val totalExpenses: BigDecimal,
    val netCashFlow: BigDecimal,
    val expenseByCategory: List<CategoryBreakdown>,
    val incomeByCategory: List<CategoryBreakdown>
)

data class TrendDataPoint(
    val date: LocalDate,
    val value: BigDecimal
)

data class MonthlySummary(
    val month: String,
    val year: Int,
    val income: BigDecimal,
    val expenses: BigDecimal,
    val netCashFlow: BigDecimal,
    val netWorth: BigDecimal
)
