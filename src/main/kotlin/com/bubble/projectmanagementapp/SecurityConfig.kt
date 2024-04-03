package com.bubble.projectmanagementapp

import com.bubble.projectmanagementapp.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig(private val jwtAuthFilter: JwtAuthFilter,private val customUserRepository: UserRepository) {


    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain{
        return http.csrf { it.disable() }
                .authorizeHttpRequests {
                    it.requestMatchers("/error/**").permitAll()
                    it.requestMatchers(HttpMethod.POST,"/api/v1/users/").permitAll()
                    it.requestMatchers(HttpMethod.POST,"/api/v1/users/login").permitAll()
                    it.requestMatchers(HttpMethod.POST,"/api/v1/users/logout").permitAll()
                    it.requestMatchers("/api/v1/**").authenticated()

                }
                .sessionManagement {
                    it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                }

                .addFilterAfter(jwtAuthFilter,UsernamePasswordAuthenticationFilter::class.java)
                .build()

    }


}