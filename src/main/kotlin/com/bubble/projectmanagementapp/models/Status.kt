package com.bubble.projectmanagementapp.models

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*

@Entity(name="status")
class Status(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    val id: Long,

    var title: String,

    @OneToMany(mappedBy = "status", cascade = [(CascadeType.ALL)])
    @JsonBackReference
    var task: Set<Task>,

    @JsonBackReference
    @ManyToMany(mappedBy = "statuses")
    var projects: Set<Project>

)