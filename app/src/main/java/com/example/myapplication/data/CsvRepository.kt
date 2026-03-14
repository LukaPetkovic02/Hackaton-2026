package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.model.Event
import com.example.myapplication.model.EventRating
import com.example.myapplication.model.FriendConnection
import com.example.myapplication.model.SavedEvent
import com.example.myapplication.model.User
import com.example.myapplication.model.ConnectionRequest
import java.io.File
import com.example.myapplication.model.ConsultationBooking
import com.example.myapplication.model.ConsultationSlot
import com.example.myapplication.model.Expert

fun loadUsersFromCsv(context: Context): List<User> {
    return try {
        context.assets.open("users.csv").bufferedReader().useLines { lines ->
            val rows = lines.toList()
            if (rows.isEmpty()) return@useLines emptyList()

            val headerFields = rows.first().split(",").map { it.trim().removePrefix("\uFEFF") }
            val hasUserId = headerFields.firstOrNull().equals("user_id", ignoreCase = true)

            rows.drop(1).mapIndexedNotNull { index, line ->
                val fields = line.split(",").map { it.trim() }
                if (hasUserId) {
                    if (fields.size < 5) return@mapIndexedNotNull null
                    User(
                        id = fields[0].toIntOrNull() ?: (index + 1),
                        firstName = fields[1],
                        lastName = fields[2],
                        email = fields[3],
                        password = fields[4]
                    )
                } else {
                    if (fields.size < 4) return@mapIndexedNotNull null
                    User(
                        id = index + 1,
                        firstName = fields[0],
                        lastName = fields[1],
                        email = fields[2],
                        password = fields[3]
                    )
                }
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun loadEventsFromCsv(context: Context): List<Event> {
    return try {
        context.assets.open("events.csv").bufferedReader().useLines { lines ->
            lines.drop(1).mapNotNull { line ->
                val fields = line.split(",").map { it.trim() }
                if (fields.size < 8) return@mapNotNull null
                Event(
                    id = fields[0].toIntOrNull() ?: return@mapNotNull null,
                    title = fields[1],
                    startTime = fields[2],
                    endTime = fields[3],
                    location = fields[4],
                    speaker = fields[5],
                    description = fields[6],
                    category = fields[7]
                )
            }.toList()
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun loadSavedEvents(context: Context): List<SavedEvent> {
    val file = getSavedEventsFile(context)
    return try {
        if (!file.exists()) {
            val initialContent = context.assets.open("saved_events.csv").bufferedReader().use { it.readText() }
            file.writeText(initialContent)
        }

        file.bufferedReader().useLines { lines ->
            lines.drop(1).mapNotNull { line ->
                val fields = line.split(",").map { it.trim() }
                if (fields.size < 3) return@mapNotNull null
                SavedEvent(
                    userId = fields[0].toIntOrNull() ?: return@mapNotNull null,
                    eventId = fields[1].toIntOrNull() ?: return@mapNotNull null,
                    savedAt = fields[2]
                )
            }.toList()
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun saveSavedEvents(context: Context, savedEvents: List<SavedEvent>) {
    val csvContent = buildString {
        appendLine("user_id,event_id,saved_at")
        savedEvents.forEach { savedEvent ->
            appendLine("${savedEvent.userId},${savedEvent.eventId},${savedEvent.savedAt}")
        }
    }
    getSavedEventsFile(context).writeText(csvContent)
}

fun getSavedEventsFile(context: Context): File {
    return File(context.filesDir, "saved_events.csv")
}

fun loadEventRatings(context: Context): List<EventRating> {
    val file = getEventRatingsFile(context)
    return try {
        if (!file.exists()) {
            val initialContent = context.assets.open("event_ratings.csv").bufferedReader().use { it.readText() }
            file.writeText(initialContent)
        }

        file.bufferedReader().useLines { lines ->
            lines.drop(1).mapNotNull { line ->
                val fields = line.split(",").map { it.trim() }
                if (fields.size < 4) return@mapNotNull null
                val parsedRating = fields[2].toIntOrNull() ?: return@mapNotNull null
                if (parsedRating !in 1..5) return@mapNotNull null
                EventRating(
                    userId = fields[0].toIntOrNull() ?: return@mapNotNull null,
                    eventId = fields[1].toIntOrNull() ?: return@mapNotNull null,
                    rating = parsedRating,
                    ratedAt = fields[3],
                    comment = if (fields.size >= 5) fields.drop(4).joinToString(",").trim() else ""
                )
            }.toList()
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun saveEventRatings(context: Context, ratings: List<EventRating>) {
    val csvContent = buildString {
        appendLine("user_id,event_id,rating,rated_at,comment")
        ratings.forEach { rating ->
            val safeComment = sanitizeCsvField(rating.comment)
            appendLine("${rating.userId},${rating.eventId},${rating.rating},${rating.ratedAt},$safeComment")
        }
    }
    getEventRatingsFile(context).writeText(csvContent)
}

fun getEventRatingsFile(context: Context): File {
    return File(context.filesDir, "event_ratings.csv")
}

private fun sanitizeCsvField(value: String): String {
    return value.replace(",", ";").replace("\n", " ").replace("\r", " ").trim()
}

fun loadConnectionRequests(context: Context): List<ConnectionRequest> {
    val file = getConnectionRequestsFile(context)
    return try {
        if (!file.exists()) {
            val initialContent = context.assets.open("connection_requests.csv").bufferedReader().use { it.readText() }
            file.writeText(initialContent)
        }

        file.bufferedReader().useLines { lines ->
            lines.drop(1).mapNotNull { line ->
                val fields = line.split(",").map { it.trim() }
                if (fields.size < 3) return@mapNotNull null
                ConnectionRequest(
                    fromUserId = fields[0].toIntOrNull() ?: return@mapNotNull null,
                    toUserId = fields[1].toIntOrNull() ?: return@mapNotNull null,
                    requestedAt = fields[2]
                )
            }.toList()
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun saveConnectionRequests(context: Context, requests: List<ConnectionRequest>) {
    val csvContent = buildString {
        appendLine("from_user_id,to_user_id,requested_at")
        requests.forEach { request ->
            appendLine("${request.fromUserId},${request.toUserId},${request.requestedAt}")
        }
    }
    getConnectionRequestsFile(context).writeText(csvContent)
}

fun getConnectionRequestsFile(context: Context): File {
    return File(context.filesDir, "connection_requests.csv")
}

fun loadFriends(context: Context): List<FriendConnection> {
    val file = getFriendsFile(context)
    return try {
        if (!file.exists()) {
            val initialContent = context.assets.open("friends.csv").bufferedReader().use { it.readText() }
            file.writeText(initialContent)
        }

        file.bufferedReader().useLines { lines ->
            lines.drop(1).mapNotNull { line ->
                val fields = line.split(",").map { it.trim() }
                if (fields.size < 3) return@mapNotNull null
                FriendConnection(
                    userAId = fields[0].toIntOrNull() ?: return@mapNotNull null,
                    userBId = fields[1].toIntOrNull() ?: return@mapNotNull null,
                    connectedAt = fields[2]
                )
            }.toList()
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun getFriendsFile(context: Context): File {
    return File(context.filesDir, "friends.csv")
}

fun saveFriends(context: Context, friends: List<FriendConnection>) {
    val csvContent = buildString {
        appendLine("user_a_id,user_b_id,connected_at")
        friends.forEach { connection ->
            appendLine("${connection.userAId},${connection.userBId},${connection.connectedAt}")
        }
    }
    getFriendsFile(context).writeText(csvContent)
}

fun loadExpertsFromCsv(context: Context): List<Expert> {
    return try {
        context.assets.open("experts.csv").bufferedReader().useLines { lines ->
            lines.drop(1).mapNotNull { line ->
                val fields = line.split(",").map { it.trim() }
                if (fields.size < 4) return@mapNotNull null
                Expert(
                    id = fields[0].toIntOrNull() ?: return@mapNotNull null,
                    name = fields[1],
                    title = fields[2],
                    expertise = fields.drop(3).joinToString(",").trim()
                )
            }.toList()
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun loadConsultationSlots(context: Context): List<ConsultationSlot> {
    return try {
        context.assets.open("consultation_slots.csv").bufferedReader().useLines { lines ->
            lines.drop(1).mapNotNull { line ->
                val fields = line.split(",").map { it.trim() }
                if (fields.size < 4) return@mapNotNull null
                ConsultationSlot(
                    id = fields[0].toIntOrNull() ?: return@mapNotNull null,
                    expertId = fields[1].toIntOrNull() ?: return@mapNotNull null,
                    startTime = fields[2],
                    endTime = fields[3]
                )
            }.toList()
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun loadConsultationBookings(context: Context): List<ConsultationBooking> {
    val file = getConsultationBookingsFile(context)
    return try {
        if (!file.exists()) {
            val initialContent = context.assets.open("consultation_bookings.csv").bufferedReader().use { it.readText() }
            file.writeText(initialContent)
        }

        file.bufferedReader().useLines { lines ->
            lines.drop(1).mapNotNull { line ->
                val fields = line.split(",").map { it.trim() }
                if (fields.size < 3) return@mapNotNull null
                ConsultationBooking(
                    userId = fields[0].toIntOrNull() ?: return@mapNotNull null,
                    slotId = fields[1].toIntOrNull() ?: return@mapNotNull null,
                    bookedAt = fields[2]
                )
            }.toList()
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun saveConsultationBookings(context: Context, bookings: List<ConsultationBooking>) {
    val csvContent = buildString {
        appendLine("user_id,slot_id,booked_at")
        bookings.forEach { booking ->
            appendLine("${booking.userId},${booking.slotId},${booking.bookedAt}")
        }
    }
    getConsultationBookingsFile(context).writeText(csvContent)
}

fun getConsultationBookingsFile(context: Context): File {
    return File(context.filesDir, "consultation_bookings.csv")
}
