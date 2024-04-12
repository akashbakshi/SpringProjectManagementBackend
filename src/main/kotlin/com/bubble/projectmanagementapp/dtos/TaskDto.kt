package com.bubble.projectmanagementapp.dtos

data class TaskDto(val projectId: Int, val name: String, var status: Long, val description: String,val createdByStr:String,val assignedToStr: String?)
