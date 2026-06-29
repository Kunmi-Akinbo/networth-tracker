package com.networth.api.controller

import com.networth.api.dto.AnalyticsResponse
import com.networth.api.dto.MonthlySummary
import com.networth.api.service.AnalyticsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/analytics")
class AnalyticsController(
    private val analyticsService: AnalyticsService
) {

    @GetMapping
    fun getAnalytics(
        @RequestParam userId: Long,
        @RequestParam(defaultValue = "12") months: Int
    ): ResponseEntity<AnalyticsResponse> {
        val analytics = analyticsService.getAnalytics(userId, months)
        return ResponseEntity.ok(analytics)
    }

    @GetMapping("/monthly-summaries")
    fun getMonthlySummaries(
        @RequestParam userId: Long,
        @RequestParam(defaultValue = "12") months: Int
    ): ResponseEntity<List<MonthlySummary>> {
        val summaries = analyticsService.getMonthlySummaries(userId, months)
        return ResponseEntity.ok(summaries)
    }
}
