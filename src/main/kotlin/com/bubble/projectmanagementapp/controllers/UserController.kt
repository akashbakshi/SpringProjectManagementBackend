package com.bubble.projectmanagementapp.controllers

import com.bubble.projectmanagementapp.dtos.UserCredentials
import com.bubble.projectmanagementapp.dtos.UserRegistration
import com.bubble.projectmanagementapp.models.User
import com.bubble.projectmanagementapp.repository.UserRepository
import com.bubble.projectmanagementapp.services.JWTService
import jakarta.persistence.UniqueConstraint
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
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
class UserController(private val userRepository: UserRepository,private val tokenService: JWTService, @Value("\${user.max_login_attempts}") val maxLoginAttempts: Int) {

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

        // TODO: replace with JWT generation after authenticating user
        if(!BCryptPasswordEncoder().matches(credentials.password,userToAuth.password) || userToAuth.lockout){

            userToAuth.failedAttemps++

            //lock the user's account if they've made too many failed login attempts
            if(userToAuth.failedAttemps >= maxLoginAttempts)
                userToAuth.lockout = true


            userRepository.save(userToAuth)

            return ResponseEntity.badRequest().body(null)
        }

        val accessToken = tokenService.generateAccessToken(userToAuth)
        val refreshToken = tokenService.generateRefreshToken(userToAuth)

        val headers = HttpHeaders()

        headers.add("accessToken",accessToken)
        headers.add("refreshToken",refreshToken)

        return ResponseEntity(userToAuth,headers, HttpStatus.OK)
    }

    @PostMapping
    fun createNewUser(@RequestBody userDTO: UserRegistration):ResponseEntity<String>{

        //validate the user doesn't already exist
        if(!userRepository.findById(userDTO.username).isEmpty){
            return ResponseEntity.badRequest().body("An account with the username ${userDTO.username} already exists")
        }

        try{

            //Create the entity that will go in our DB
            val newUser = User(userDTO.username,userDTO.password,userDTO.email,userDTO.name,false,LocalDateTime.now(ZoneId.of("UTC")),null,null,null,false,0)
            newUser.password = BCryptPasswordEncoder().encode(userDTO.password) // encrypt the password
            userRepository.save(newUser)

            return ResponseEntity.created(URI.create("/api/v1/users/${newUser.username}")).body("")
        }
        catch(integrityException: DataIntegrityViolationException){
            //check exceptions for duplicate email exception
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