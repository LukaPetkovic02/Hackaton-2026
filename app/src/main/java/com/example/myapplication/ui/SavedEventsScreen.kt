package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
fun SavedEventsScreen(
    user: User,
    savedEvents: List<Event>,
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
            text = "Saved Events",
            modifier = Modifier.padding(top = 24.dp),
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEAF6FF)
        )
        Text(
            text = "Hi ${user.firstName}, here are your saved events.",
            modifier = Modifier.padding(top = 8.dp),
            color = Color(0xFFD8EEFF)
        )

        if (savedEvents.isEmpty()) {
            Text(
                text = "You have no saved events yet.",
                modifier = Modifier.padding(top = 16.dp),
                color = Color(0xFFD8EEFF)
            )
        } else {
            savedEvents.forEach { event ->
                EventCard(
                    event = event,
                    averageRating = averageRatings[event.id],
                    onOpenInfo = { onOpenEventInfo(event.id) },
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }

        Text(
            text = "Use Home tab to explore more events.",
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp),
            color = Color(0xFFD8EEFF)
        )
    }
}
