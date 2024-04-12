package com.bubble.projectmanagementapp.models

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity(name = "tasks")
class Task (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    val taskId: Int,

    var title: String,
    var description: String?,

    @Column(name = "date_created")
    var dateCreated: LocalDateTime,

    @ManyToOne
    @JoinColumn(name = "status", nullable = false)
    var status: Status,

    @ManyToOne
    @JoinColumn(name = "createdBy", nullable = false)
    var createdBy: User,

    @ManyToOne
    @JoinColumn(name = "assignedTo", nullable = true)
    var assignedTo: User?,

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "projectId", nullable = false)
    var projectId: Project
)