package com.bubble.projectmanagementapp.repository

import com.bubble.projectmanagementapp.models.Token
import com.bubble.projectmanagementapp.models.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TokenRepository: CrudRepository<Token,Int> {

    fun findByAccessToken(accessToken:String): Token?
    fun findByRefreshToken(accessToken:String): Token?
    fun findByUsername(user: User): List<Token>
}