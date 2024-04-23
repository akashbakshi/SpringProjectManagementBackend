package com.bubble.projectmanagementapp.repository

import com.bubble.projectmanagementapp.models.Board
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


@Repository
interface BoardRepository: CrudRepository<Board,Long> {

    fun findByProject_ProjectId(projectId: Int): List<Board>
}