package com.example.myapplication.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Row

private val GoogleYellow = Color(0xFFFBBC04)

@Composable
fun StarRatingDisplay(
    rating: Double?,
    modifier: Modifier = Modifier
) {
    val safeRating = rating ?: 0.0
    Row(modifier = modifier) {
        for (index in 1..5) {
            val icon = when {
                safeRating >= index -> Icons.Filled.Star
                safeRating >= index - 0.5 -> Icons.Filled.StarHalf
                else -> Icons.Filled.StarBorder
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoogleYellow
            )
        }
    }
}

@Composable
fun StarRatingInput(
    onRate: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selected by remember { mutableStateOf(0) }
    Row(modifier = modifier) {
        for (index in 1..5) {
            IconButton(
                onClick = {
                    selected = index
                    onRate(index)
                }
            ) {
                Icon(
                    imageVector = if (index <= selected) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "Rate $index stars",
                    tint = GoogleYellow
                )
            }
        }
    }
}
