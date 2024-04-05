package com.bubble.projectmanagementapp.repository

import com.bubble.projectmanagementapp.models.Project
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


@Repository
interface ProjectRepository: CrudRepository<Project,Int> {

    fun findByAcronym(acronym: String): Project?
}