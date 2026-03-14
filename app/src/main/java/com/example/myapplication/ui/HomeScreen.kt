package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Event
import com.example.myapplication.model.User
import com.example.myapplication.ui.components.EventCard

@Composable
fun HomeScreen(
    user: User,
    events: List<Event>,
    savedEventIds: Set<Int>,
    averageRatings: Map<Int, Double>,
    onOpenEventInfo: (Int) -> Unit,
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
            text = "Community Day 2026",
            modifier = Modifier.padding(top = 24.dp),
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEAF6FF)
        )
        Text(
            text = "Welcome, ${user.firstName}!",
            modifier = Modifier.padding(top = 8.dp),
            color = Color(0xFFD8EEFF)
        )

        Text(
            text = "Today's schedule",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 16.dp),
            color = Color(0xFFEAF6FF)
        )

        events.forEach { event ->
            EventCard(
                event = event,
                averageRating = averageRatings[event.id],
                onOpenInfo = { onOpenEventInfo(event.id) },
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Event info",
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFEAF6FF)
        )
        Text(
            text = "A full day of talks, networking, and practical consultations focused on fiscalization and POS technology. You saved ${savedEventIds.size} event(s).",
            modifier = Modifier.padding(top = 4.dp),
            color = Color(0xFFD8EEFF)
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}
