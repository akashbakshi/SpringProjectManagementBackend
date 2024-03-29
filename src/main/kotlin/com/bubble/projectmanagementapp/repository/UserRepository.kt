package com.bubble.projectmanagementapp.repository

import com.bubble.projectmanagementapp.models.User
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: CrudRepository<User, String> {

    fun findByEmail(email: String): User?

    @Query("select u from users u where u.name like '%?1%'")
    fun findByName(name: String): List<User>
}