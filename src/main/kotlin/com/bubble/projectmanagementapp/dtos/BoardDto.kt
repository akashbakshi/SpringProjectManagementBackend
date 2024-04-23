package com.bubble.projectmanagementapp.dtos

import java.time.LocalDateTime

data class BoardDto(var name:String, var goal:String, var targetDueDate: LocalDateTime?, var projectId: Int,var isActive: Boolean)
