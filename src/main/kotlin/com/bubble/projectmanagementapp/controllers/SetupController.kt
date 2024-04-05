package com.bubble.projectmanagementapp.controllers

import com.bubble.projectmanagementapp.dtos.SetupResults
import com.bubble.projectmanagementapp.repository.RoleRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/setup/")
class SetupController(private val roleRepository: RoleRepository) {

    /*
       this method will be used to see if the user is initializing a fresh copy of the app or upgrading
       If it's the first option we will walk them through making an admin account.
       We do this because only Admin roles can add other admins and users, so the user needs to create an account in order to use the application.
   */
    @GetMapping("welcome")
    fun welcomeCheck():ResponseEntity<SetupResults>{
        val numberOfAdmin = roleRepository.findUsersInRoleByName("admin").count()
        val setupResults = SetupResults(false)

        if(numberOfAdmin == 0){
            setupResults.needsAdmin = true
        }

        return ResponseEntity.ok(setupResults)
    }
}