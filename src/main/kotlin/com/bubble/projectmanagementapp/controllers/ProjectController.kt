package com.bubble.projectmanagementapp.controllers

import com.bubble.projectmanagementapp.dtos.ProjectDto
import com.bubble.projectmanagementapp.models.Project
import com.bubble.projectmanagementapp.models.User
import com.bubble.projectmanagementapp.models.Status
import com.bubble.projectmanagementapp.dtos.TaskDto
import com.bubble.projectmanagementapp.models.Task
import com.bubble.projectmanagementapp.repository.ProjectRepository
import com.bubble.projectmanagementapp.repository.StatusRepository
import com.bubble.projectmanagementapp.repository.TaskRepository
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
class ProjectController(private val projectRepository: ProjectRepository,private val userRepository: UserRepository,private val taskRepository: TaskRepository,private val jwtService: JWTService,private val statusRepository: StatusRepository) {

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


            val claimsFromToken = jwtService.getClaimsFromToken(token.replace("Bearer ",""))

            val username = claimsFromToken.subject


            if(projectToFind.members.map { it.username }.contains(username) || projectToFind.owner.username == username){
                return ResponseEntity.ok(projectToFind)
            }else{
                return ResponseEntity.badRequest().body("You do not have permission to view this project")
            }



        }catch(ex:Exception){

            return ResponseEntity.internalServerError().body("Failed to fetch project with key: '$key' due to server error")
        }
    }

    @GetMapping("{key}/tasks")
    fun getProjectTasks(@PathVariable("key") key: String, @RequestHeader(name="Authorization") token:String):ResponseEntity<*>{
        try{
            val projectToFind = projectRepository.findByAcronym(key)

            if(projectToFind == null)
                return ResponseEntity.badRequest().body("Cannot find project with key: $key")


            val claimsFromToken = jwtService.getClaimsFromToken(token.replace("Bearer ",""))

            val username = claimsFromToken.subject


            if(projectToFind.members.map { it.username }.contains(username) || projectToFind.owner.username == username){

                val tasksInProject = taskRepository.findByProjectId_Acronym(key)
                return ResponseEntity.ok(tasksInProject)
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

            val standardStatuses = statusRepository.findStandardStatuses()



            val project = Project(0,newProject.acronym.uppercase(),newProject.name,newProject.thumbnail_url,newProject.description,userToFind, LocalDateTime.now(),false,members,
                emptySet(),
                standardStatuses
            )

            projectRepository.save(project)

            return ResponseEntity.created(URI.create("/api/v1/projects/${newProject.acronym}")).body(project)
        }catch(ex: Exception){

            return ResponseEntity.internalServerError().body("Failed to create new project due to server error")
        }
    }

    @PutMapping
    fun updateProject(@RequestBody updatedProject: ProjectDto):ResponseEntity<*>{
        try{

            val projectToFind = projectRepository.findByAcronym(updatedProject.acronym) ?: return ResponseEntity.badRequest().body("Error: Cannot update project because a project with the key '${updatedProject.acronym}' cannot be found")

            if( updatedProject.name != projectToFind.name)
                projectToFind.name = updatedProject.name

            if(updatedProject.thumbnail_url != projectToFind.thumbnail_url){
                //TODO: upload image
                projectToFind.thumbnail_url = updatedProject.thumbnail_url
            }

            if(updatedProject.description != null && updatedProject.description != projectToFind.description){
                projectToFind.description = updatedProject.description
            }

            if(updatedProject.members.isNotEmpty()){
                val existingMembers = projectToFind.members.toMutableSet() // convert the existing members to a mutable set

                updatedProject.members.forEach {m->
                    val memberToAdd = userRepository.findById(m).getOrNull() // try and find the members using the username provided from the front end

                    if(memberToAdd != null && !projectToFind.members.contains(memberToAdd)){ // make sure the member profile is found and isn't already associated to this project
                        existingMembers.add(memberToAdd)
                    }
                }

                projectToFind.members = existingMembers // add our updated list of members
            }

            projectRepository.save(projectToFind) // save the updated project

            return ResponseEntity.ok(projectToFind)
        }catch(e: Exception){
            return ResponseEntity.badRequest().body("Failed to update project because of an exception")
        }
    }

    @PostMapping("tasks")
    fun createNewTask(@RequestBody newTask: TaskDto): ResponseEntity<*>{

        var createdBy: User? = null
        var assignedTo: User? = null

        try{
            createdBy = userRepository.findById(newTask.createdByStr).getOrNull()

            val project = projectRepository.findById(newTask.projectId).getOrNull()

            if(project == null)
                return ResponseEntity.badRequest().body("Cannot find project with ID '${newTask.projectId}'")

            if(newTask.assignedToStr != null)
                assignedTo =  userRepository.findById(newTask.assignedToStr).getOrNull()

            if(createdBy == null)
                return ResponseEntity.badRequest().body("Cannot find created by user with username '${newTask.createdByStr}'")

            if(assignedTo == null && !newTask.assignedToStr.isNullOrEmpty())
                return ResponseEntity.badRequest().body("Cannot find assigned to user with username '${newTask.assignedToStr}'")

            val defaultStartStatus:Status? = statusRepository.findStandardStatuses().toList().first { it.title.uppercase() == "TO DO" }

            if(defaultStartStatus == null){
                return ResponseEntity.internalServerError().body("Failed to get default status of 'TO DO'")
            }

            val task = Task(0,newTask.name,newTask.description, LocalDateTime.now(),defaultStartStatus,createdBy,assignedTo,project)

            taskRepository.save(task)

            project.tasks.plus(task)
            projectRepository.save(project)

            return ResponseEntity.ok(task)
        }catch (ex: Exception){

            println(ex.message)
            println(ex.stackTraceToString())

            return ResponseEntity.internalServerError().body("Failed to create new task due to server error")
        }

    }

    @PutMapping("tasks/{id}")
    fun updateTask(@PathVariable("id") id:Int,@RequestBody updatedTask: TaskDto): ResponseEntity<*>{
        val currentTask = taskRepository.findById(id).getOrNull()

        if(currentTask == null)
            return ResponseEntity.badRequest().body("Could not find task with ID $id")

        if(!currentTask.title.isNullOrEmpty() && currentTask.title != updatedTask.name )
            currentTask.title = updatedTask.name

        if(!currentTask.description.isNullOrEmpty() && currentTask.description != updatedTask.description)
            currentTask.description = updatedTask.description

        if(currentTask.assignedTo != null && currentTask.assignedTo?.username != updatedTask.assignedToStr)
        {
            val assignee = userRepository.findById(updatedTask.assignedToStr ?: "").getOrNull()

            if(assignee != null)
                currentTask.assignedTo = assignee
        }


        if(currentTask.status.id != updatedTask.status ){
            val newStatus = statusRepository.findById(updatedTask.status).getOrNull()

            if(newStatus == null)
                return ResponseEntity.badRequest().body("Could not find status ${updatedTask.status}")

            currentTask.status = newStatus
        }


        taskRepository.save(currentTask)
        return ResponseEntity.ok(currentTask)
    }

    @PatchMapping("tasks/{id}/assignee")
    fun assignUserToTask(@PathVariable id: Int,@RequestBody taskDto: TaskDto):ResponseEntity<*>{

        val project = projectRepository.findById(taskDto.projectId)

        if(project == null)
            return ResponseEntity.badRequest().body("Could not find project with ID '${taskDto.projectId}'")

        val taskToFind = taskRepository.findById(id).getOrNull()


        if(taskToFind == null)
            return ResponseEntity.badRequest().body("Could not find task with ID'$id'")

        if(taskDto.assignedToStr != null){
            val assignee = userRepository.findById(taskDto.assignedToStr).getOrNull()

            if(assignee == null)
                return ResponseEntity.badRequest().body("Could not find user '$assignee' as the assignee")

            taskToFind.assignedTo = assignee
        }else{
            taskToFind.assignedTo = null
        }

        taskRepository.save(taskToFind)

        return ResponseEntity.ok(taskToFind)
    }

    @PatchMapping("tasks/{id}/status")
    fun updateTaskStatus(@PathVariable("id") id: Int,@RequestBody taskDto: TaskDto):ResponseEntity<*>{
        val currentTask = taskRepository.findById(id).getOrNull()

        if(currentTask == null)
            return ResponseEntity.badRequest().body("Could not find task with ID $id")

        val status = statusRepository.findById(taskDto.status).getOrNull()

        if(status == null)
            return ResponseEntity.badRequest().body("Could not find status ${taskDto.status}")

        currentTask.status = status

        taskRepository.save(currentTask)

        return ResponseEntity.ok(currentTask)
    }
}