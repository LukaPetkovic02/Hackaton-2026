package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.ConnectionRequest
import com.example.myapplication.model.User

@Composable
fun MyRequestsScreen(
    sentRequests: List<ConnectionRequest>,
    receivedRequests: List<ConnectionRequest>,
    connections: List<User>,
    userNamesById: Map<Int, String>,
    onAccept: (ConnectionRequest) -> Unit,
    onDecline: (ConnectionRequest) -> Unit,
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
            text = "My Requests",
            modifier = Modifier.padding(top = 24.dp),
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Received",
            modifier = Modifier.padding(top = 16.dp),
            fontWeight = FontWeight.SemiBold
        )

        if (receivedRequests.isEmpty()) {
            Text(
                text = "No received requests.",
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            receivedRequests.forEach { request ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = userNamesById[request.fromUserId] ?: "User #${request.fromUserId}",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Requested at: ${request.requestedAt}",
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Row(modifier = Modifier.padding(top = 10.dp)) {
                            Button(onClick = { onAccept(request) }) {
                                Text("Accept")
                            }
                            Button(
                                onClick = { onDecline(request) },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text("Decline")
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "Sent",
            modifier = Modifier.padding(top = 20.dp),
            fontWeight = FontWeight.SemiBold
        )

        if (sentRequests.isEmpty()) {
            Text(
                text = "No sent requests.",
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            sentRequests.forEach { request ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = userNamesById[request.toUserId] ?: "User #${request.toUserId}",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Sent at: ${request.requestedAt}",
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = "Connections",
            modifier = Modifier.padding(top = 20.dp),
            fontWeight = FontWeight.SemiBold
        )

        if (connections.isEmpty()) {
            Text(
                text = "No connections yet.",
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )
        } else {
            connections.forEach { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "${user.firstName} ${user.lastName}",
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = user.email,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
