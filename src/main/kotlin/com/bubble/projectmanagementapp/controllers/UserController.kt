package com.bubble.projectmanagementapp.controllers

import com.bubble.projectmanagementapp.dtos.UserCredentials
import com.bubble.projectmanagementapp.dtos.UserRegistration
import com.bubble.projectmanagementapp.models.User
import com.bubble.projectmanagementapp.repository.UserRepository
import jakarta.persistence.UniqueConstraint
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.jvm.optionals.getOrNull

@RestController
@RequestMapping("/api/v1/users/")
class UserController(private val userRepository: UserRepository) {

    @GetMapping
    fun getAllUsers():ResponseEntity<List<User>>{
        val allUsers = userRepository.findAll().toList()

        return ResponseEntity.ok(allUsers)
    }

    @GetMapping
    @RequestMapping("{username}")
    fun getUserByUsername(@PathVariable("username") username:String): ResponseEntity<User?>{

        val userToFind = userRepository.findById(username).getOrNull() ?: return ResponseEntity.badRequest().body(null)

        return ResponseEntity.ok(userToFind)
    }

    @GetMapping
    @RequestMapping("search")
    fun getUserByName(@RequestParam("name") name: String?):ResponseEntity<List<User>>{

        if(name.isNullOrBlank()){
            return ResponseEntity.badRequest().body(emptyList())
        }

        val usersMatchingName = userRepository.findByName(name).toList()

        return ResponseEntity.ok(usersMatchingName)
    }

    @PostMapping
    @RequestMapping("login")
    fun authenticateUser(@RequestBody credentials: UserCredentials):ResponseEntity<User?>{
        val userToAuth = userRepository.findById(credentials.username).getOrNull() ?: return ResponseEntity.badRequest().body(null)

        if(!BCryptPasswordEncoder().matches(credentials.password,userToAuth.password)){

            return ResponseEntity.badRequest().body(null)
        }

        return ResponseEntity.ok(userToAuth)
    }

    @PostMapping
    fun createNewUser(@RequestBody userDTO: UserRegistration):ResponseEntity<String>{

        //validate the user doesn't already exist
        if(!userRepository.findById(userDTO.username).isEmpty){
            return ResponseEntity.badRequest().body("An account with the username ${userDTO.username} already exists")
        }

        try{

            val newUser = User(userDTO.username,userDTO.password,userDTO.email,userDTO.name,false,LocalDateTime.now(ZoneId.of("UTC")),null,null,null)
            newUser.password = BCryptPasswordEncoder().encode(userDTO.password)
            userRepository.save(newUser)

            return ResponseEntity.created(URI.create("/api/v1/users/${newUser.username}")).body("")
        }
        catch(integrityException: DataIntegrityViolationException){
            if(integrityException.stackTraceToString().contains("users_email_key")){

                return ResponseEntity.badRequest().body("An account with the email ${userDTO.email} already exists")
            }

            return ResponseEntity.badRequest().body("")
        }
        catch(e: Exception){
            return ResponseEntity.internalServerError().body(e.stackTraceToString())
        }
    }
}