package com.bubble.projectmanagementapp.repository

import com.bubble.projectmanagementapp.models.Role
import com.bubble.projectmanagementapp.models.User
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository: CrudRepository<Role, Int> {
    fun findByName(name: String): Role?
    fun findByNormalizedName(normalizedName: String): Role?

    @Query("select u from users u join u.roles r WHERE r.normalizedName LIKE %?1%")
    fun findUsersInRoleByName(name: String): List<User>
}