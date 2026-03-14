package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Event

@Composable
fun EventCard(
    event: Event,
    averageRating: Double? = null,
    isSaved: Boolean? = null,
    onToggleSave: (() -> Unit)? = null,
    onOpenInfo: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.75f)) {
                Text(
                    text = "${event.startTime} ${event.title}",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = event.location,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "Avg rating: ${averageRating?.let { String.format("%.1f", it) } ?: "N/A"}",
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (onOpenInfo != null) {
                IconButton(
                    onClick = onOpenInfo,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Event info"
                    )
                }
            } else if (onToggleSave != null && isSaved != null) {
                Button(
                    onClick = onToggleSave,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(if (isSaved) "Unsave" else "Save")
                }
            }
        }
    }
}
