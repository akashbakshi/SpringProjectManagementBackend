package com.bubble.projectmanagementapp.models

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity(name = "users")
class User (
    @Id
    val username:String,

    @JsonIgnore
    var password: String,
    var email: String,
    var name: String,

    @Column(name = "is_archived")
    var isArchived: Boolean,

    @JsonIgnore
    @Column(name = "created_on")
    var createdOn: LocalDateTime,

    @Column(name = "last_active")
    var lastActive: LocalDateTime?,

    @Column(name = "password_reset_token")
    var passwordResetToken: String?,

    @Column(name = "password_reset_date")
    var passwordResetDate: LocalDateTime?,
    var lockout: Boolean,

    @Column(name ="failed_attempts")
    var failedAttempts: Int,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = [JoinColumn(name = "userid")],
            inverseJoinColumns = [JoinColumn(name = "roleid")])
    var roles: Set<Role>,

    @OneToMany(mappedBy = "username")
    @JsonBackReference
    var tokens: Set<Token>,

    @OneToMany(mappedBy = "owner")
    @JsonBackReference
    var projects: Set<Project>,

    @OneToMany(mappedBy = "createdBy")
    @JsonBackReference
    var tasksCreated: Set<Task>,

    @OneToMany(mappedBy = "assignedTo")
    @JsonBackReference
    var assignedTasks: Set<Task>

)