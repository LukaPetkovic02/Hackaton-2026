package com.example.myapplication

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.loadEventRatings
import com.example.myapplication.data.loadEventsFromCsv
import com.example.myapplication.data.loadFriends
import com.example.myapplication.data.loadSavedEvents
import com.example.myapplication.data.loadUsersFromCsv
import com.example.myapplication.data.loadConnectionRequests
import com.example.myapplication.data.saveEventRatings
import com.example.myapplication.data.saveConnectionRequests
import com.example.myapplication.data.saveSavedEvents
import com.example.myapplication.model.ConnectionRequest
import com.example.myapplication.model.EventRating
import com.example.myapplication.model.SavedEvent
import com.example.myapplication.model.User
import com.example.myapplication.ui.EventDetailsScreen
import com.example.myapplication.ui.HomeScreen
import com.example.myapplication.ui.InfoScreen
import com.example.myapplication.ui.LoginScreen
import com.example.myapplication.ui.ParticipantsScreen
import com.example.myapplication.ui.SavedEventsScreen
import com.example.myapplication.util.currentTimestamp

@Composable
fun AppContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val users = remember { loadUsersFromCsv(context) }
    val events = remember { loadEventsFromCsv(context) }
    var savedEvents by remember { mutableStateOf(loadSavedEvents(context)) }
    var eventRatings by remember { mutableStateOf(loadEventRatings(context)) }
    var connectionRequests by remember { mutableStateOf(loadConnectionRequests(context)) }
    val friends = remember { loadFriends(context) }
    var loggedInUser by remember { mutableStateOf<User?>(null) }
    var activeTab by remember { mutableStateOf(LoggedInTab.Home) }
    var selectedEventId by remember { mutableStateOf<Int?>(null) }
    var eventSubPage by remember { mutableStateOf(EventSubPage.Details) }

    if (loggedInUser == null) {
        LoginScreen(
            users = users,
            onLoginSuccess = { matchedUser ->
                loggedInUser = matchedUser
                activeTab = LoggedInTab.Home
                selectedEventId = null
                eventSubPage = EventSubPage.Details
            },
            modifier = modifier
        )
    } else {
        val currentUser = loggedInUser!!
        val savedEventIds = savedEvents
            .filter { it.userId == currentUser.id }
            .map { it.eventId }
            .toSet()
        val savedUserEvents = events.filter { savedEventIds.contains(it.id) }
        val userNamesById = users.associate { it.id to "${it.firstName} ${it.lastName}".trim() }
        val eventAverageRatings = eventRatings
            .groupBy { it.eventId }
            .mapValues { (_, ratings) -> ratings.map { it.rating }.average() }
        val userRatingsByEvent = eventRatings
            .filter { it.userId == currentUser.id }
            .associate { it.eventId to it.rating }
        val friendUserIds = friends
            .filter { it.userAId == currentUser.id || it.userBId == currentUser.id }
            .map { if (it.userAId == currentUser.id) it.userBId else it.userAId }
            .toSet()
        val sentRequestUserIds = connectionRequests
            .filter { it.fromUserId == currentUser.id }
            .map { it.toUserId }
            .toSet()

        val onToggleSave: (Int) -> Unit = { eventId ->
            val alreadySaved = savedEvents.any {
                it.userId == currentUser.id && it.eventId == eventId
            }
            val updated = if (alreadySaved) {
                savedEvents.filterNot {
                    it.userId == currentUser.id && it.eventId == eventId
                }
            } else {
                savedEvents + SavedEvent(
                    userId = currentUser.id,
                    eventId = eventId,
                    savedAt = currentTimestamp()
                )
            }
            savedEvents = updated
            saveSavedEvents(context, updated)
        }

        val onRateEvent: (Int, Int, String) -> Unit = { eventId, rating, comment ->
            val alreadyRated = eventRatings.any {
                it.userId == currentUser.id && it.eventId == eventId
            }
            if (!alreadyRated && rating in 1..5) {
                val updatedRatings = eventRatings + EventRating(
                    userId = currentUser.id,
                    eventId = eventId,
                    rating = rating,
                    ratedAt = currentTimestamp(),
                    comment = comment.trim()
                )
                eventRatings = updatedRatings
                saveEventRatings(context, updatedRatings)
            }
        }

        val onSendConnectionRequest: (Int) -> Unit = { targetUserId ->
            val isSelf = targetUserId == currentUser.id
            val isFriend = friendUserIds.contains(targetUserId)
            val alreadyRequested = sentRequestUserIds.contains(targetUserId)
            if (!isSelf && !isFriend && !alreadyRequested) {
                val updatedRequests = connectionRequests + ConnectionRequest(
                    fromUserId = currentUser.id,
                    toUserId = targetUserId,
                    requestedAt = currentTimestamp()
                )
                connectionRequests = updatedRequests
                saveConnectionRequests(context, updatedRequests)
            }
        }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = activeTab == LoggedInTab.Home,
                        onClick = {
                            activeTab = LoggedInTab.Home
                            selectedEventId = null
                            eventSubPage = EventSubPage.Details
                        },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = activeTab == LoggedInTab.MyAgenda,
                        onClick = {
                            activeTab = LoggedInTab.MyAgenda
                            selectedEventId = null
                            eventSubPage = EventSubPage.Details
                        },
                        icon = { Icon(Icons.Filled.Event, contentDescription = "My Agenda") },
                        label = { Text("My Agenda") }
                    )
                    NavigationBarItem(
                        selected = activeTab == LoggedInTab.Info,
                        onClick = {
                            activeTab = LoggedInTab.Info
                            selectedEventId = null
                            eventSubPage = EventSubPage.Details
                        },
                        icon = { Icon(Icons.Filled.Info, contentDescription = "Info") },
                        label = { Text("Info") }
                    )
                }
            }
        ) { innerPadding ->
            when (activeTab) {
                LoggedInTab.Home -> {
                    val selectedEvent = events.find { it.id == selectedEventId }
                    if (selectedEvent != null) {
                        if (eventSubPage == EventSubPage.Participants) {
                            val participantIds = savedEvents
                                .filter { it.eventId == selectedEvent.id }
                                .map { it.userId }
                                .toSet()
                            val participants = users.filter { participantIds.contains(it.id) }
                            ParticipantsScreen(
                                event = selectedEvent,
                                participants = participants,
                                currentUserId = currentUser.id,
                                friendUserIds = friendUserIds,
                                sentRequestUserIds = sentRequestUserIds,
                                onSendConnectionRequest = onSendConnectionRequest,
                                onBack = { eventSubPage = EventSubPage.Details },
                                modifier = Modifier.padding(innerPadding)
                            )
                        } else {
                            EventDetailsScreen(
                                event = selectedEvent,
                                isSaved = savedEventIds.contains(selectedEvent.id),
                                onToggleSave = { onToggleSave(selectedEvent.id) },
                                averageRating = eventAverageRatings[selectedEvent.id],
                                userRating = userRatingsByEvent[selectedEvent.id],
                                eventRatings = eventRatings.filter { it.eventId == selectedEvent.id },
                                userNamesById = userNamesById,
                                onRateEvent = { rating, comment -> onRateEvent(selectedEvent.id, rating, comment) },
                                onOpenParticipants = { eventSubPage = EventSubPage.Participants },
                                onBack = { selectedEventId = null },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    } else {
                        HomeScreen(
                            user = currentUser,
                            events = events,
                            savedEventIds = savedEventIds,
                            averageRatings = eventAverageRatings,
                            onOpenEventInfo = { eventId ->
                                selectedEventId = eventId
                                eventSubPage = EventSubPage.Details
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }

                LoggedInTab.MyAgenda -> {
                    val selectedAgendaEvent = savedUserEvents.find { it.id == selectedEventId }
                    if (selectedAgendaEvent != null) {
                        if (eventSubPage == EventSubPage.Participants) {
                            val participantIds = savedEvents
                                .filter { it.eventId == selectedAgendaEvent.id }
                                .map { it.userId }
                                .toSet()
                            val participants = users.filter { participantIds.contains(it.id) }
                            ParticipantsScreen(
                                event = selectedAgendaEvent,
                                participants = participants,
                                currentUserId = currentUser.id,
                                friendUserIds = friendUserIds,
                                sentRequestUserIds = sentRequestUserIds,
                                onSendConnectionRequest = onSendConnectionRequest,
                                onBack = { eventSubPage = EventSubPage.Details },
                                modifier = Modifier.padding(innerPadding)
                            )
                        } else {
                            EventDetailsScreen(
                                event = selectedAgendaEvent,
                                isSaved = savedEventIds.contains(selectedAgendaEvent.id),
                                onToggleSave = { onToggleSave(selectedAgendaEvent.id) },
                                averageRating = eventAverageRatings[selectedAgendaEvent.id],
                                userRating = userRatingsByEvent[selectedAgendaEvent.id],
                                eventRatings = eventRatings.filter { it.eventId == selectedAgendaEvent.id },
                                userNamesById = userNamesById,
                                onRateEvent = { rating, comment -> onRateEvent(selectedAgendaEvent.id, rating, comment) },
                                onOpenParticipants = { eventSubPage = EventSubPage.Participants },
                                onBack = { selectedEventId = null },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    } else {
                        SavedEventsScreen(
                            user = currentUser,
                            savedEvents = savedUserEvents,
                            averageRatings = eventAverageRatings,
                            onOpenEventInfo = { eventId ->
                                selectedEventId = eventId
                                eventSubPage = EventSubPage.Details
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }

                LoggedInTab.Info -> InfoScreen(
                    user = currentUser,
                    onLogout = { loggedInUser = null },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

private enum class LoggedInTab {
    Home,
    MyAgenda,
    Info
}

private enum class EventSubPage {
    Details,
    Participants
}
