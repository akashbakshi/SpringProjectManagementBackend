package com.bubble.projectmanagementapp.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity(name="projects")
class Project (

        @Id
        @Column(name = "project_id")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val projectId:Int,

        @Column(name="project_acronym", unique = true)
        var acronym: String,

        var name: String,
        var thumbnail_url: String?,
        var description: String?,

        @ManyToOne
        @JoinColumn(name = "owner", nullable = false)
        var owner: User,

        @Column(name = "date_created")
        var dateCreated: LocalDateTime,

        @Column(name = "is_archived")
        var isArchived: Boolean,

        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(
                name = "projects_users",
                joinColumns = [JoinColumn(name = "project_id")],
                inverseJoinColumns = [JoinColumn(name = "user_id")])
        var members: Set<User>,

        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(name = "tasks_projects", joinColumns = [JoinColumn(name = "projectId")], inverseJoinColumns = [JoinColumn(name ="taskId")])
        var tasks: Set<Task>,

        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(name= "projects_status", joinColumns = [JoinColumn(name = "projectId")], inverseJoinColumns = [JoinColumn(name = "statusId")])
        var statuses: Set<Status>,

)