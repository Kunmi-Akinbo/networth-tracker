package com.networth.api.controller

import com.networth.api.dto.AuthResponse
import com.networth.api.dto.LoginRequest
import com.networth.api.dto.RefreshTokenRequest
import com.networth.api.dto.RegisterRequest
import com.networth.api.entity.User
import com.networth.api.repository.UserRepository
import com.networth.api.service.UserService
import com.networth.api.util.JwtUtil
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil,
    private val passwordEncoder: PasswordEncoder
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        val userResponse = userService.register(request)
        val user = userRepository.findById(userResponse.id).get()
        
        val accessToken = jwtUtil.generateToken(user.id!!, user.email)
        val refreshToken = jwtUtil.generateRefreshToken(user.id!!, user.email)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            AuthResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userId = user.id!!,
                email = user.email
            )
        )
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { IllegalArgumentException("Invalid email or password") }
        
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid email or password")
        }
        
        val accessToken = jwtUtil.generateToken(user.id!!, user.email)
        val refreshToken = jwtUtil.generateRefreshToken(user.id!!, user.email)
        
        return ResponseEntity.ok(
            AuthResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userId = user.id!!,
                email = user.email
            )
        )
    }

    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<AuthResponse> {
        if (!jwtUtil.validateRefreshToken(request.refreshToken)) {
            throw IllegalArgumentException("Invalid or expired refresh token")
        }
        
        val email = jwtUtil.extractEmail(request.refreshToken)
            ?: throw IllegalArgumentException("Invalid refresh token")
        
        val userId = jwtUtil.extractUserId(request.refreshToken)
            ?: throw IllegalArgumentException("Invalid refresh token")
        
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }
        
        val newAccessToken = jwtUtil.generateToken(user.id!!, user.email)
        val newRefreshToken = jwtUtil.generateRefreshToken(user.id!!, user.email)
        
        return ResponseEntity.ok(
            AuthResponse(
                accessToken = newAccessToken,
                refreshToken = newRefreshToken,
                userId = user.id!!,
                email = user.email
            )
        )
    }
}
