package com.bubble.projectmanagementapp.dtos

data class ProjectDto(
        val acronym: String,
        var name: String,
        var description: String?,
        var thumbnail_url: String?,
        var createdBy: String,
        var members: Set<String>

)