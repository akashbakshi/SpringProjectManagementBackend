package com.bubble.projectmanagementapp.repository

import com.bubble.projectmanagementapp.models.Task
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository: CrudRepository<Task,Int> {

    fun findByProjectId_Acronym(key: String): List<Task>
}