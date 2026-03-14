package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Event
import com.example.myapplication.model.EventRating
import com.example.myapplication.ui.components.StarRatingDisplay
import com.example.myapplication.ui.components.StarRatingInput

@Composable
fun EventDetailsScreen(
    event: Event,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    averageRating: Double?,
    userRating: Int?,
    eventRatings: List<EventRating>,
    userNamesById: Map<Int, String>,
    onRateEvent: (Int, String) -> Unit,
    onOpenParticipants: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var commentText by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = event.title,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEAF6FF)
            )
            IconButton(onClick = onToggleSave) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = if (isSaved) "Unsave event" else "Save event",
                    tint = Color(0xFFEAF6FF)
                )
            }
        }
        Text(
            text = "${event.startTime} - ${event.endTime}",
            modifier = Modifier.padding(top = 8.dp),
            color = Color(0xFFD8EEFF)
        )
        Text(
            text = "Location: ${event.location}",
            modifier = Modifier.padding(top = 8.dp),
            color = Color(0xFFD8EEFF)
        )
        if (event.speaker.isNotBlank()) {
            Text(
                text = "Speaker: ${event.speaker}",
                modifier = Modifier.padding(top = 8.dp),
                color = Color(0xFFD8EEFF)
            )
        }
        Text(
            text = "Category: ${event.category}",
            modifier = Modifier.padding(top = 8.dp),
            color = Color(0xFFD8EEFF)
        )
        Text(
            text = event.description,
            modifier = Modifier.padding(top = 12.dp),
            color = Color(0xFFD8EEFF)
        )
        Text(
            text = "Average rating",
            modifier = Modifier.padding(top = 12.dp),
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFEAF6FF)
        )
        StarRatingDisplay(
            rating = averageRating,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = averageRating?.let { String.format("%.1f/5", it) } ?: "N/A",
            modifier = Modifier.padding(top = 4.dp),
            color = Color(0xFFD8EEFF)
        )

        if (userRating == null) {
            Text(
                text = "Rate this event",
                modifier = Modifier.padding(top = 12.dp),
                color = Color(0xFFEAF6FF)
            )
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = { Text("Comment (optional)") },
                minLines = 2
            )
            StarRatingInput(
                onRate = { rating ->
                    onRateEvent(rating, commentText)
                    commentText = ""
                },
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            Text(
                text = "Your rating",
                modifier = Modifier.padding(top = 12.dp),
                color = Color(0xFFEAF6FF)
            )
            StarRatingDisplay(
                rating = userRating.toDouble(),
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "$userRating/5",
                modifier = Modifier.padding(top = 4.dp),
                color = Color(0xFFD8EEFF)
            )
        }

        Text(
            text = "Individual ratings",
            modifier = Modifier.padding(top = 16.dp),
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFEAF6FF)
        )

        if (eventRatings.isEmpty()) {
            Text(
                text = "No ratings yet.",
                modifier = Modifier.padding(top = 8.dp),
                color = Color(0xFFD8EEFF)
            )
        } else {
            eventRatings
                .sortedByDescending { it.ratedAt }
                .forEach { ratingItem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xCC0F3E67)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = userNamesById[ratingItem.userId] ?: "User #${ratingItem.userId}",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF0F9FF)
                            )
                            StarRatingDisplay(rating = ratingItem.rating.toDouble())
                            if (ratingItem.comment.isNotBlank()) {
                                Text(
                                    text = ratingItem.comment,
                                    modifier = Modifier.padding(top = 6.dp),
                                    color = Color(0xFFD4EBFF)
                                )
                            }
                        }
                    }
                }
        }

        Button(
            onClick = onOpenParticipants,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            Text("Participants")
        }

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 24.dp)
        ) {
            Text("Back")
        }
    }
}
