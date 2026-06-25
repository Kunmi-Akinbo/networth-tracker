package com.networth.api.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.networth.api.dto.LoginRequest
import com.networth.api.dto.RefreshTokenRequest
import com.networth.api.dto.RegisterRequest
import com.networth.api.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertNotNull

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    private val objectMapper = jacksonObjectMapper()

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll()
    }

    @Test
    fun `register should create new user and return tokens`() {
        // Arrange
        val request = RegisterRequest(
            email = "test@example.com",
            password = "password123"
        )

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.userId").exists())
            .andExpect(jsonPath("$.email").value(request.email))

        // Verify user was created in database
        val user = userRepository.findByEmail(request.email)
        assertNotNull(user)
    }

    @Test
    fun `register should fail with duplicate email`() {
        // Arrange
        val request = RegisterRequest(
            email = "test@example.com",
            password = "password123"
        )

        // First registration
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // Second registration with same email
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `login should return tokens for valid credentials`() {
        // Arrange - Register a user first
        val registerRequest = RegisterRequest(
            email = "test@example.com",
            password = "password123"
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )

        val loginRequest = LoginRequest(
            email = "test@example.com",
            password = "password123"
        )

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.userId").exists())
            .andExpect(jsonPath("$.email").value(loginRequest.email))
    }

    @Test
    fun `login should fail with invalid credentials`() {
        // Arrange - Register a user first
        val registerRequest = RegisterRequest(
            email = "test@example.com",
            password = "password123"
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )

        val loginRequest = LoginRequest(
            email = "test@example.com",
            password = "wrongpassword"
        )

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `login should fail with non-existent email`() {
        // Arrange
        val loginRequest = LoginRequest(
            email = "nonexistent@example.com",
            password = "password123"
        )

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `refreshToken should return new tokens with valid refresh token`() {
        // Arrange - Register and login to get tokens
        val registerRequest = RegisterRequest(
            email = "test@example.com",
            password = "password123"
        )

        val registerResult = mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andReturn()

        val registerResponse = objectMapper.readTree(registerResult.response.contentAsString)
        val refreshToken = registerResponse["refreshToken"].asText()

        val refreshRequest = RefreshTokenRequest(refreshToken = refreshToken)

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.userId").exists())
            .andExpect(jsonPath("$.email").value(registerRequest.email))
    }

    @Test
    fun `refreshToken should fail with invalid refresh token`() {
        // Arrange
        val refreshRequest = RefreshTokenRequest(refreshToken = "invalid_token")

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `register should fail with invalid email format`() {
        // Arrange
        val request = RegisterRequest(
            email = "invalid-email",
            password = "password123"
        )

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `register should fail with missing password`() {
        // Arrange
        val request = RegisterRequest(
            email = "test@example.com",
            password = ""
        )

        // Act & Assert
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }
}
