package com.bubble.projectmanagementapp.repository

import org.springframework.data.repository.CrudRepository
import com.bubble.projectmanagementapp.models.Status
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface StatusRepository: CrudRepository<Status, Long> {

    @Query("select s from status s where upper(s.title) IN ('TO DO','IN PROGRESS','DONE','ARCHIVE')")
    fun findStandardStatuses(): Set<Status>
}