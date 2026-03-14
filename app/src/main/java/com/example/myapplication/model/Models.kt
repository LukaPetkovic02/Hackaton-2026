package com.example.myapplication.model

data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)

data class Event(
    val id: Int,
    val title: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val speaker: String,
    val description: String,
    val category: String
)

data class SavedEvent(
    val userId: Int,
    val eventId: Int,
    val savedAt: String
)
