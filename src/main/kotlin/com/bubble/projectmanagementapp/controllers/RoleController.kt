package com.bubble.projectmanagementapp.controllers

import com.bubble.projectmanagementapp.dtos.UserRole
import com.bubble.projectmanagementapp.models.Role
import com.bubble.projectmanagementapp.models.User
import com.bubble.projectmanagementapp.repository.RoleRepository
import com.bubble.projectmanagementapp.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import kotlin.jvm.optionals.getOrNull


@RestController
@RequestMapping("/api/v1/roles")
class RoleController(private val roleRepository: RoleRepository, private val userRepository: UserRepository ) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllRoles():ResponseEntity<List<Role>>{
        val allRoles = roleRepository.findAll().toList()

        return ResponseEntity.ok(allRoles)
    }

    @GetMapping
    @RequestMapping("{role}")
    @PreAuthorize("hasRole('ADMIN')")
    fun findAllUsersInRole(@PathVariable("role") roleName: String): ResponseEntity<List<User>>{
        val usersInRole = roleRepository.findUsersInRoleByName(roleName.uppercase())

        return ResponseEntity.ok(usersInRole)
    }

    @PostMapping
    @RequestMapping("/user")
    @PreAuthorize("hasRole('ADMIN')")
    fun associateRoleToUser(@RequestBody userRole: UserRole): ResponseEntity<*>{

        //We need to get the role and user profile from our DB to associate it
        val user = userRepository.findById(userRole.username).getOrNull() ?: return ResponseEntity.badRequest().body("Failed to find user with username ${userRole.username}")
        val role = roleRepository.findById(userRole.roleId).getOrNull() ?: return ResponseEntity.badRequest().body("Failed to find role with roleId ${userRole.roleId}")

        val existingUserRoles = user.roles.toMutableSet()

        existingUserRoles.add(role)

        try{
            user.roles = existingUserRoles.toSet()

            userRepository.save(user)

            return ResponseEntity.ok().body(null)
        }catch(ex: Exception){
            println(ex.message)
            println(ex.stackTrace)

            return ResponseEntity.internalServerError().body("Failed to associate ${role.name} role with ${user.username}")
        }
    }
}