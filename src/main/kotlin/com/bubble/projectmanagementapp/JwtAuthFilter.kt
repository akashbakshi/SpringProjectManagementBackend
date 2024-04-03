package com.bubble.projectmanagementapp

import com.bubble.projectmanagementapp.repository.UserRepository
import com.bubble.projectmanagementapp.services.CustomUserDetailService
import com.bubble.projectmanagementapp.services.JWTService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter


@Component
class JwtAuthFilter(private val jwtService: JWTService,private val userDetailService: CustomUserDetailService): OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {

        val allowedEndpoints = mapOf<String,String>("/error/**" to "GET","/api/v1/users/" to "POST","/api/v1/users/login" to "POST")
        val authHeader = request.getHeader("Authorization")

        var token = ""
        var username = ""

        if(authHeader != null && authHeader.startsWith("Bearer ")){
            token = authHeader.replace("Bearer ","")
        }

        //Only execute for all routes that require auth
        println(allowedEndpoints.contains(request.requestURI.toString()))
        println(allowedEndpoints[request.requestURI.toString()])
        if(!allowedEndpoints.contains(request.requestURI.toString()) && request.method != allowedEndpoints[request.requestURI.toString()] ){

            if( authHeader == null || jwtService.isTokenExpired(token) ){
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                return
            }

            username = jwtService.getClaimsFromToken(token).subject
            if(username.isNotBlank() && SecurityContextHolder.getContext().authentication == null){

                val userDetails = userDetailService.loadUserByUsername(username)

                val authenticationToken = UsernamePasswordAuthenticationToken(userDetails,null,userDetails.authorities)
                authenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authenticationToken

            }

        }

        filterChain.doFilter(request,response)
    }

}