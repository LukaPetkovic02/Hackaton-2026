package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
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
import com.example.myapplication.model.User

@Composable
fun ParticipantsScreen(
    event: Event,
    participants: List<User>,
    currentUserId: Int,
    friendUserIds: Set<Int>,
    sentRequestUserIds: Set<Int>,
    onSendConnectionRequest: (Int) -> Unit,
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
            text = "Participants",
            modifier = Modifier.padding(top = 24.dp),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = event.title,
            modifier = Modifier.padding(top = 8.dp)
        )

        if (participants.isEmpty()) {
            Text(
                text = "No participants saved this event yet.",
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            participants.forEach { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${user.firstName} ${user.lastName}",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = user.email,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        val isSelf = user.id == currentUserId
                        val isFriend = friendUserIds.contains(user.id)
                        val isRequested = sentRequestUserIds.contains(user.id)
                        val actionDisabled = isSelf || isFriend || isRequested

                        Column(horizontalAlignment = Alignment.End) {
                            IconButton(
                                onClick = { onSendConnectionRequest(user.id) },
                                enabled = !actionDisabled
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PersonAdd,
                                    contentDescription = "Send connection request"
                                )
                            }

                            if (isSelf) {
                                Text("You")
                            } else if (isFriend) {
                                Text("Friend")
                            } else if (isRequested) {
                                Text("Requested")
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp)
        ) {
            Text("Back")
        }
    }
}
