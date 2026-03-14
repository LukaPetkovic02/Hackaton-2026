package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Event
import com.example.myapplication.ui.components.StarRatingDisplay
import com.example.myapplication.ui.components.StarRatingInput

@Composable
fun EventDetailsScreen(
    event: Event,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    averageRating: Double?,
    userRating: Int?,
    onRateEvent: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = event.title,
            modifier = Modifier.padding(top = 24.dp),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${event.startTime} - ${event.endTime}",
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Location: ${event.location}",
            modifier = Modifier.padding(top = 8.dp)
        )
        if (event.speaker.isNotBlank()) {
            Text(
                text = "Speaker: ${event.speaker}",
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Text(
            text = "Category: ${event.category}",
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = event.description,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "Average rating",
            modifier = Modifier.padding(top = 12.dp),
            fontWeight = FontWeight.SemiBold
        )
        StarRatingDisplay(
            rating = averageRating,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = averageRating?.let { String.format("%.1f/5", it) } ?: "N/A",
            modifier = Modifier.padding(top = 4.dp)
        )

        if (userRating == null) {
            Text(
                text = "Rate this event",
                modifier = Modifier.padding(top = 12.dp)
            )
            StarRatingInput(
                onRate = onRateEvent,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            Text(
                text = "Your rating",
                modifier = Modifier.padding(top = 12.dp)
            )
            StarRatingDisplay(
                rating = userRating.toDouble(),
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "$userRating/5",
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Button(
            onClick = onToggleSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            Text(if (isSaved) "Unsave Event" else "Save Event")
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
