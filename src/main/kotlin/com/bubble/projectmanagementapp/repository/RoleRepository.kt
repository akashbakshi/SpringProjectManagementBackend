package com.bubble.projectmanagementapp.repository

import com.bubble.projectmanagementapp.models.Role
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository: CrudRepository<Role, Int> {
    fun findByName(name: String): Role?
    fun findByNormalizedName(normalizedName: String): Role?
}