package com.bubble.projectmanagementapp.services

import com.bubble.projectmanagementapp.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class CustomUserDetailService(private val userRepository: UserRepository): UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails {
        val userToLoad = userRepository.findById(username ?: "").getOrNull() ?: throw Exception("User not found in custom user detail service")


        userToLoad.roles.map{println(it.normalizedName)}

        val roles = userToLoad.roles.map { SimpleGrantedAuthority("ROLE_${it.name.uppercase()}") }.toSet()
        return User(userToLoad.username,userToLoad.password,roles)
    }
}