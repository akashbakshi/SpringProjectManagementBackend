package com.bubble.projectmanagementapp.controllers

import com.bubble.projectmanagementapp.dtos.ProjectDto
import com.bubble.projectmanagementapp.models.Project
import com.bubble.projectmanagementapp.models.User
import com.bubble.projectmanagementapp.repository.ProjectRepository
import com.bubble.projectmanagementapp.repository.UserRepository
import com.bubble.projectmanagementapp.services.JWTService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull


@RestController
@RequestMapping("/api/v1/projects/")
class ProjectController(private val projectRepository: ProjectRepository,private val userRepository: UserRepository,private val jwtService: JWTService) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllProjects(): ResponseEntity<List<Project>>{
        val allProjects = projectRepository.findAll().toList()
        return ResponseEntity.ok(allProjects)
    }

    @GetMapping("{key}")
    fun getProjectByKey(@PathVariable("key") key: String, @RequestHeader(name="Authorization") token:String): ResponseEntity<*>{
        try{
            val projectToFind = projectRepository.findByAcronym(key)

            if(projectToFind == null)
                return ResponseEntity.badRequest().body("Cannot find project with key: $key")


            val claimsFromToken = jwtService.getClaimsFromToken(token)

            val username = claimsFromToken.subject


            if(projectToFind.members.map { it.username }.contains(username)){
                return ResponseEntity.ok(projectToFind)
            }else{
                return ResponseEntity.badRequest().body("You do not have permission to view this project")
            }



        }catch(ex:Exception){

            return ResponseEntity.internalServerError().body("Failed to fetch project with key: '$key' due to server error")
        }
    }

    @PostMapping
    fun createNewProject(@RequestBody newProject: ProjectDto):ResponseEntity<*>{
        try{

            val userToFind = userRepository.findById(newProject.createdBy).getOrNull()
            if(userToFind == null){
                return ResponseEntity.badRequest().body("Could not create project because user '${newProject.createdBy}' does not exist")
            }


            val projectToFind = projectRepository.findByAcronym(newProject.acronym)

            if(projectToFind != null){
                return ResponseEntity.badRequest().body("Could not create project because a project already exist with the acronym '${newProject.acronym}'. Please select a different acronym")
            }



            val failedMembers = mutableSetOf<String>()

            val members = mutableSetOf<User>()
            newProject.members.forEach { m->
                val memberToAdd = userRepository.findById(m).getOrNull()

                if(memberToAdd == null)
                    failedMembers.add(m)
                else
                    members.add(memberToAdd)

            }


            val project = Project(0,newProject.acronym,newProject.name,newProject.thumbnail_url,newProject.description,userToFind, LocalDateTime.now(),false,members)

            projectRepository.save(project)

            return ResponseEntity.created(URI.create("/api/v1/projects/${newProject.acronym}")).body(project)
        }catch(ex: Exception){

            return ResponseEntity.internalServerError().body("Failed to create new project due to server error")
        }
    }
}