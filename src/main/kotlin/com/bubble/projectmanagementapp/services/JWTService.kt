package com.bubble.projectmanagementapp.services

import com.bubble.projectmanagementapp.models.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt
import org.springframework.stereotype.Service
import java.util.*

@Service
class JWTService(
        @Value("\${jwt.key}")
        private val key: String,
        @Value("\${jwt.access_expiration}")
        private val accessTokenExpirationInMinutes: Long,
        @Value("\${jwt.refresh_expiration}")
        private val refreshTokenExpirationInHours: Long
) {


    val secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(key))

    private fun getClaims(user: User): Map<String,String>{

        val claims = mapOf("email" to user.email, "roles" to user.roles.map { it.name }.toString())

        return claims
    }
    fun generateAccessToken(user: User):String{

        val claims = getClaims(user)

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.username)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis()+ (1000*60*accessTokenExpirationInMinutes)))
                .signWith(secretKey,SignatureAlgorithm.HS256)
                .compact()
    }

    fun isTokenExpired(token:String): Boolean{

        try{

            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token)
            return false
        }catch (expException: io.jsonwebtoken.ExpiredJwtException){
            return true
        }
    }

    fun generateRefreshToken(user: User):String{

        val claims = getClaims(user)

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.username)
                .setIssuedAt(Date(System.currentTimeMillis()))
                .setExpiration(Date(System.currentTimeMillis()+ (60000*60*refreshTokenExpirationInHours))) // 60000 milliseconds = 1 minute * 60 minutes = 1 hour. Then we multiply the number of hours we want for our refresh token
                .signWith(secretKey,SignatureAlgorithm.HS256)
                .compact()
    }

    fun getClaimsFromToken(token: String): Claims{
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
    }
}