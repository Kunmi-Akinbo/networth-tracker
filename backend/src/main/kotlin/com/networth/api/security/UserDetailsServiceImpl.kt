package com.networth.api.security

import com.networth.api.entity.User
import com.networth.api.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByEmail(email)
            .orElseThrow { UsernameNotFoundException("User not found with email: $email") }
        
        return CustomUserDetails(user)
    }
}

data class CustomUserDetails(
    private val user: User
) : UserDetails {

    override fun getAuthorities() = emptyList<org.springframework.security.core.GrantedAuthority>()

    override fun getPassword() = user.passwordHash

    override fun getUsername() = user.email

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = true

    fun getUserId() = user.id
}
