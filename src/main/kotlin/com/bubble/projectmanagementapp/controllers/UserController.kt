package com.bubble.projectmanagementapp.controllers

import com.bubble.projectmanagementapp.dtos.UserCredentials
import com.bubble.projectmanagementapp.dtos.UserRegistration
import com.bubble.projectmanagementapp.dtos.UserTokens
import com.bubble.projectmanagementapp.models.Role
import com.bubble.projectmanagementapp.models.Token
import com.bubble.projectmanagementapp.models.User
import com.bubble.projectmanagementapp.repository.RoleRepository
import com.bubble.projectmanagementapp.repository.TokenRepository
import com.bubble.projectmanagementapp.repository.UserRepository
import com.bubble.projectmanagementapp.services.JWTService
import jakarta.persistence.UniqueConstraint
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
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
class UserController(private val userRepository: UserRepository,private val tokenRepository: TokenRepository,private val roleRepository: RoleRepository,private val tokenService: JWTService, @Value("\${user.max_login_attempts}") val maxLoginAttempts: Int) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers():ResponseEntity<List<User>>{
        val allUsers = userRepository.findAll().toList()

        return ResponseEntity.ok(allUsers)
    }

    @GetMapping("{username}")
    fun getUserByUsername(@PathVariable("username") username:String): ResponseEntity<User?>{

        val userToFind = userRepository.findById(username).getOrNull() ?: return ResponseEntity.badRequest().body(null)

        return ResponseEntity.ok(userToFind)
    }

    @GetMapping("search")
    fun getUserByName(@RequestParam("name") name: String?):ResponseEntity<List<User>>{

        if(name.isNullOrBlank()){
            return ResponseEntity.badRequest().body(emptyList())
        }

        val usersMatchingName = userRepository.findByName(name).toList()

        return ResponseEntity.ok(usersMatchingName)
    }

    @PostMapping("logout")
    fun logout(@RequestBody token: UserTokens): ResponseEntity<*>{
        var tokenToInvalidate = tokenRepository.findByAccessToken(token.accessToken) ?: return ResponseEntity.badRequest().body("invalid token")

        tokenToInvalidate.dateInvalidated = LocalDateTime.now()
        tokenToInvalidate.isValid = false

        try{
            tokenRepository.save(tokenToInvalidate)

            return ResponseEntity.ok().body(null)
        }catch(ex: Exception){
            println(ex.message)
            println(ex.stackTrace)
            return ResponseEntity.internalServerError().body("Failed to logout")
        }
    }

    @PostMapping("login")
    fun authenticateUser(@RequestBody credentials: UserCredentials):ResponseEntity<User?>{
        val userToAuth = userRepository.findById(credentials.username).getOrNull() ?: return ResponseEntity.badRequest().body(null)

        if(!BCryptPasswordEncoder().matches(credentials.password,userToAuth.password) || userToAuth.lockout){

            userToAuth.failedAttempts++

            //lock the user's account if they've made too many failed login attempts
            if(userToAuth.failedAttempts >= maxLoginAttempts)
                userToAuth.lockout = true


            userRepository.save(userToAuth)

            return ResponseEntity.badRequest().body(null)
        }

        val accessToken = tokenService.generateAccessToken(userToAuth)
        val refreshToken = tokenService.generateRefreshToken(userToAuth)

        try{
            val newTokenEntry = Token(0,accessToken,refreshToken, LocalDateTime.now(),null,true,userToAuth)
            tokenRepository.save(newTokenEntry)

        }catch (ex: Exception){
            println(ex.message)
            println(ex.stackTrace)
        }

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

            val userRole = roleRepository.findByNormalizedName("USER") // find the default user role

            if(userRole == null){
                println("WARNING: USER role can't be found in the db") // Change to logger once implemented
            }


            val rolesToAdd = mutableSetOf<Role>()

            // add role to mutable set if it exists in our DB
            userRole?.let{
                rolesToAdd.add(it)
            }

            //Create the entity that will go in our DB
            val newUser = User(userDTO.username,userDTO.password,userDTO.email,userDTO.name,false,LocalDateTime.now(ZoneId.of("UTC")),null,null,null,false,0,rolesToAdd.toSet(), emptySet(), emptySet(), emptySet(),emptySet() )
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