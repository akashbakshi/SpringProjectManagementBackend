package com.bubble.projectmanagementapp.models

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity(name = "boards")
class Board (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id", nullable = false)
    val id: Long,

    var name: String,
    var goal: String,

    @Column(name = "date_created", nullable = false)
    var dateCreated: LocalDateTime,


    @Column(name = "target_due_date", nullable = true)
    var targetDueDate: LocalDateTime?,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean,

    @OneToMany(mappedBy = "boardId")
    var tasks: Set<Task>,

    @ManyToOne
    @JoinColumn(name = "projectId", nullable = true)
    @JsonBackReference
    var project: Project,
)