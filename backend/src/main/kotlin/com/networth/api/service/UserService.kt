package com.networth.api.service

import com.networth.api.dto.RegisterRequest
import com.networth.api.dto.UserResponse
import com.networth.api.entity.User
import com.networth.api.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun register(request: RegisterRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already registered")
        }

        val passwordHash = passwordEncoder.encode(request.password)
        val user = User(
            email = request.email,
            passwordHash = passwordHash,
            createdAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(user)
        return toResponse(savedUser)
    }

    fun findByEmail(email: String): UserResponse? {
        return userRepository.findByEmail(email)
            .map { toResponse(it) }
            .orElse(null)
    }

    fun findById(id: Long): UserResponse? {
        return userRepository.findById(id)
            .map { toResponse(it) }
            .orElse(null)
    }

    fun updatePassword(userId: Long, newPassword: String): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val updatedUser = user.copy(
            passwordHash = passwordEncoder.encode(newPassword),
            updatedAt = LocalDateTime.now()
        )

        val savedUser = userRepository.save(updatedUser)
        return toResponse(savedUser)
    }

    fun deleteById(id: Long) {
        userRepository.deleteById(id)
    }

    private fun toResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id!!,
            email = user.email,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
}
