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
    @JoinColumn(name = "status_id", referencedColumnName = "status_id")
    var status: Status,

    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "created_by")
    var createdBy: User,

    @ManyToOne
    @JoinColumn(name = "assigned_to", referencedColumnName = "assigned_to")
    var assignedTo: User,

    @JsonBackReference
    @ManyToMany(mappedBy = "tasks")
    var projects: Set<Project>
)