package com.bubble.projectmanagementapp.controllers

import com.bubble.projectmanagementapp.repository.RoleRepository
import com.bubble.projectmanagementapp.repository.UserRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/roles")
class RoleController(private val roleRepository: RoleRepository, private val userRepository: UserRepository ) {


}