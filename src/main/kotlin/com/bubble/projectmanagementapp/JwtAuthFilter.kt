package com.bubble.projectmanagementapp

import com.bubble.projectmanagementapp.repository.TokenRepository
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
class JwtAuthFilter(private val jwtService: JWTService,private val tokenRepository: TokenRepository,private val userDetailService: CustomUserDetailService): OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {

        val allowedEndpoints = mapOf<String,String>("/error/**" to "GET","/api/v1/users/" to "POST","/api/v1/users/login" to "POST","/api/v1/users/logout" to "POST","/api/v1/setup/welcome" to "GET")
        val authHeader = request.getHeader("Authorization")

        var token = ""
        var username = ""

        if(authHeader != null && authHeader.startsWith("Bearer ")){
            token = authHeader.replace("Bearer ","")
        }

        //Only execute for all routes that require auth
        if(!allowedEndpoints.contains(request.requestURI.toString()) || request.method != allowedEndpoints[request.requestURI.toString()] ){

            if( authHeader == null || jwtService.isTokenExpired(token) ){
                response.status = HttpServletResponse.SC_UNAUTHORIZED
                return
            }

            //We assume we're mostly using access tokens, so we attempt to find the entry using access token to see if it's still valid
            val userToken = tokenRepository.findByAccessToken(token)

            //we'll assume it's null in the off chance the user is hitting the refresh endpoint to refresh their access token using the refresh token
            if(userToken == null){
                val refreshToken = tokenRepository.findByRefreshToken(token) // in this case we attempt to search by the refresh token instead as a last resort

                //if it's still not found using both token we consider it invalid by default
                if(refreshToken == null){

                    response.status = HttpServletResponse.SC_UNAUTHORIZED
                    return
                }
            }

            // return a 401 if the token is invalid
            if(userToken != null && !userToken.isValid){
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