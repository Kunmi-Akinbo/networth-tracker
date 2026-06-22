package com.networth.api.util

import com.networth.api.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtUtil(private val jwtProperties: JwtProperties) {

    private val key = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    fun generateToken(userId: Long, email: String): String {
        val claims: Claims = Jwts.claims().apply {
            subject = email
            put("userId", userId)
        }

        return Jwts.builder()
            .claims(claims)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + jwtProperties.expiration))
            .signWith(key)
            .compact()
    }

    fun generateRefreshToken(userId: Long, email: String): String {
        val claims: Claims = Jwts.claims().apply {
            subject = email
            put("userId", userId)
            put("type", "refresh")
        }

        return Jwts.builder()
            .claims(claims)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + jwtProperties.refreshExpiration))
            .signWith(key)
            .compact()
    }

    fun extractEmail(token: String): String? {
        return extractClaim(token) { it.subject }
    }

    fun extractUserId(token: String): Long? {
        return extractClaim(token) { claims ->
            claims["userId"]?.toString()?.toLong()
        }
    }

    fun extractExpiration(token: String): Date? {
        return extractClaim(token) { it.expiration }
    }

    fun isTokenExpired(token: String): Boolean {
        val expiration = extractExpiration(token) ?: return true
        return expiration.before(Date())
    }

    fun validateToken(token: String, email: String): Boolean {
        val extractedEmail = extractEmail(token)
        return extractedEmail == email && !isTokenExpired(token)
    }

    fun validateRefreshToken(token: String): Boolean {
        val tokenType = extractClaim(token) { claims ->
            claims["type"]?.toString()
        }
        return tokenType == "refresh" && !isTokenExpired(token)
    }

    private fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T? {
        return try {
            val claims = extractAllClaims(token)
            claimsResolver(claims)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
