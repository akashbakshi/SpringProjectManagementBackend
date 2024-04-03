package com.bubble.projectmanagementapp.models

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(name = "tokens")
class Token (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,

    @Column(name = "access_token")
    var accessToken:String,

    @Column(name = "refresh_token")
    var refreshToken:String,

    @Column(name = "date_created")
    var dateCreated: LocalDateTime,

    @Column(name = "date_invalidated")
    var dateInvalidated: LocalDateTime?,

    @Column(name = "is_valid")
    var isValid: Boolean,

    @ManyToOne
    @JoinColumn(name="username", nullable=false)
    var username: User
)