package com.networth.api.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val expiration: Long,
    val refreshExpiration: Long
)
