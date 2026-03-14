package com.example.myapplication.data

import android.content.Context
import com.example.myapplication.model.Event
import com.example.myapplication.model.EventRating
import com.example.myapplication.model.SavedEvent
import com.example.myapplication.model.User
import java.io.File

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
                    ratedAt = fields[3]
                )
            }.toList()
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun saveEventRatings(context: Context, ratings: List<EventRating>) {
    val csvContent = buildString {
        appendLine("user_id,event_id,rating,rated_at")
        ratings.forEach { rating ->
            appendLine("${rating.userId},${rating.eventId},${rating.rating},${rating.ratedAt}")
        }
    }
    getEventRatingsFile(context).writeText(csvContent)
}

fun getEventRatingsFile(context: Context): File {
    return File(context.filesDir, "event_ratings.csv")
}
