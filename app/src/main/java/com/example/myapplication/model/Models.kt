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

data class EventRating(
    val userId: Int,
    val eventId: Int,
    val rating: Int,
    val ratedAt: String,
    val comment: String
)

data class ConnectionRequest(
    val fromUserId: Int,
    val toUserId: Int,
    val requestedAt: String
)

data class FriendConnection(
    val userAId: Int,
    val userBId: Int,
    val connectedAt: String
)

data class Expert(
    val id: Int,
    val name: String,
    val title: String,
    val expertise: String
)

data class ConsultationSlot(
    val id: Int,
    val expertId: Int,
    val startTime: String,
    val endTime: String
)

data class ConsultationBooking(
    val userId: Int,
    val slotId: Int,
    val bookedAt: String
)
