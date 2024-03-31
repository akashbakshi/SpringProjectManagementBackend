package com.bubble.projectmanagementapp.models

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany

@Entity(name = "roles")
class Role (
    @Id
    val role_id: Int,
    var name: String,
    @Column(name = "normalize_name")
    var normalizedName: String,

    @JsonBackReference
    @ManyToMany(mappedBy = "roles")
    var users: Set<User>
)