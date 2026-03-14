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
import com.example.myapplication.model.User

@Composable
fun InfoScreen(
    user: User,
    onLogout: () -> Unit,
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
            text = "Community Day Info",
            modifier = Modifier.padding(top = 24.dp),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Hello ${user.firstName}, welcome to Community Day 2026.",
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Community Day brings together experts, businesses, and technology partners to discuss fiscalization, POS innovation, and practical implementation challenges.",
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "Venue: Main Hall & Conference Rooms",
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "Support desk: Open all day",
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = "Tip: Use My Agenda tab to quickly access your saved sessions.",
            modifier = Modifier.padding(top = 12.dp)
        )

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 24.dp)
        ) {
            Text("Logout")
        }
    }
}
