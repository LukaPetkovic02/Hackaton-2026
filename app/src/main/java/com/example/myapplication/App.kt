package com.example.myapplication

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.loadEventRatings
import com.example.myapplication.data.loadEventsFromCsv
import com.example.myapplication.data.loadExpertsFromCsv
import com.example.myapplication.data.loadFriends
import com.example.myapplication.data.loadSavedEvents
import com.example.myapplication.data.loadConsultationBookings
import com.example.myapplication.data.loadConsultationSlots
import com.example.myapplication.data.loadUsersFromCsv
import com.example.myapplication.data.loadConnectionRequests
import com.example.myapplication.data.saveConsultationBookings
import com.example.myapplication.data.saveEventRatings
import com.example.myapplication.data.saveConnectionRequests
import com.example.myapplication.data.saveFriends
import com.example.myapplication.data.saveSavedEvents
import com.example.myapplication.model.ConnectionRequest
import com.example.myapplication.model.ConsultationBooking
import com.example.myapplication.model.EventRating
import com.example.myapplication.model.FriendConnection
import com.example.myapplication.model.SavedEvent
import com.example.myapplication.model.User
import com.example.myapplication.notification.cancelSavedEventReminder
import com.example.myapplication.notification.scheduleConsultationReminders
import com.example.myapplication.notification.scheduleSavedEventReminders
import com.example.myapplication.ui.EventDetailsScreen
import com.example.myapplication.ui.HomeScreen
import com.example.myapplication.ui.InfoScreen
import com.example.myapplication.ui.LoginScreen
import com.example.myapplication.ui.ConsultationsScreen
import com.example.myapplication.ui.MyRequestsScreen
import com.example.myapplication.ui.ParticipantsScreen
import com.example.myapplication.ui.SavedEventsScreen
import com.example.myapplication.util.currentTimestamp
import com.example.myapplication.ui.theme.GlassBlueBottom
import com.example.myapplication.ui.theme.GlassBlueTop

@Composable
fun AppContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val users = remember { loadUsersFromCsv(context) }
    val events = remember { loadEventsFromCsv(context) }
    val experts = remember { loadExpertsFromCsv(context) }
    val consultationSlots = remember { loadConsultationSlots(context) }
    var savedEvents by remember { mutableStateOf(loadSavedEvents(context)) }
    var eventRatings by remember { mutableStateOf(loadEventRatings(context)) }
    var consultationBookings by remember { mutableStateOf(loadConsultationBookings(context)) }
    var connectionRequests by remember { mutableStateOf(loadConnectionRequests(context)) }
    var friends by remember { mutableStateOf(loadFriends(context)) }
    var loggedInUserId by remember { mutableStateOf(getPersistedLoggedInUserId(context)) }
    var activeTab by remember { mutableStateOf(LoggedInTab.Home) }
    var selectedEventId by remember { mutableStateOf<Int?>(null) }
    var eventSubPage by remember { mutableStateOf(EventSubPage.Details) }
    val loggedInUser = users.find { it.id == loggedInUserId }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GlassBlueTop, GlassBlueBottom)
                )
            )
    ) {
        if (loggedInUser == null) {
            LoginScreen(
                users = users,
                onLoginSuccess = { matchedUser ->
                    loggedInUserId = matchedUser.id
                    persistLoggedInUserId(context, matchedUser.id)
                    activeTab = LoggedInTab.Home
                    selectedEventId = null
                    eventSubPage = EventSubPage.Details
                },
                modifier = Modifier.fillMaxSize()
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
        val sentRequests = connectionRequests.filter { it.fromUserId == currentUser.id }
        val receivedRequests = connectionRequests.filter { it.toUserId == currentUser.id }
        val connections = users
            .filter { friendUserIds.contains(it.id) }
            .sortedWith(compareBy({ it.lastName }, { it.firstName }))

        val onToggleSave: (Int) -> Unit = { eventId ->
            val alreadySaved = savedEvents.any {
                it.userId == currentUser.id && it.eventId == eventId
            }
            val updated = if (alreadySaved) {
                cancelSavedEventReminder(context, currentUser.id, eventId)
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

        val onAcceptRequest: (ConnectionRequest) -> Unit = { request ->
            if (request.toUserId == currentUser.id) {
                val updatedRequests = connectionRequests.filterNot {
                    it.fromUserId == request.fromUserId && it.toUserId == request.toUserId
                }
                connectionRequests = updatedRequests
                saveConnectionRequests(context, updatedRequests)

                val alreadyFriends = friends.any {
                    (it.userAId == request.fromUserId && it.userBId == request.toUserId) ||
                        (it.userAId == request.toUserId && it.userBId == request.fromUserId)
                }
                if (!alreadyFriends) {
                    val updatedFriends = friends + FriendConnection(
                        userAId = minOf(request.fromUserId, request.toUserId),
                        userBId = maxOf(request.fromUserId, request.toUserId),
                        connectedAt = currentTimestamp()
                    )
                    friends = updatedFriends
                    saveFriends(context, updatedFriends)
                }
            }
        }

        val onDeclineRequest: (ConnectionRequest) -> Unit = { request ->
            if (request.toUserId == currentUser.id) {
                val updatedRequests = connectionRequests.filterNot {
                    it.fromUserId == request.fromUserId && it.toUserId == request.toUserId
                }
                connectionRequests = updatedRequests
                saveConnectionRequests(context, updatedRequests)
            }
        }

        val onBookConsultationSlot: (Int) -> Boolean = { slotId ->
            val alreadyBooked = consultationBookings.any { it.slotId == slotId }
            if (alreadyBooked) {
                false
            } else {
                val updatedBookings = consultationBookings + ConsultationBooking(
                    userId = currentUser.id,
                    slotId = slotId,
                    bookedAt = currentTimestamp()
                )
                consultationBookings = updatedBookings
                saveConsultationBookings(context, updatedBookings)
                true
            }
        }

        LaunchedEffect(
            currentUser.id,
            events,
            savedEvents,
            experts,
            consultationSlots,
            consultationBookings
        ) {
            scheduleSavedEventReminders(context, currentUser.id, events, savedEvents)
            scheduleConsultationReminders(
                context = context,
                userId = currentUser.id,
                experts = experts,
                slots = consultationSlots,
                bookings = consultationBookings
            )
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xB20C3457)
                ) {
                    val bottomNavColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFEAF6FF),
                        unselectedIconColor = Color(0xFFBFDFFF),
                        selectedTextColor = Color(0xFFEAF6FF),
                        unselectedTextColor = Color(0xFFBFDFFF),
                        indicatorColor = Color(0x553B79A8)
                    )
                    NavigationBarItem(
                        selected = activeTab == LoggedInTab.Home,
                        onClick = {
                            activeTab = LoggedInTab.Home
                            selectedEventId = null
                            eventSubPage = EventSubPage.Details
                        },
                        colors = bottomNavColors,
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home", modifier = Modifier.size(20.dp)) },
                        label = { Text("Home", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == LoggedInTab.MyAgenda,
                        onClick = {
                            activeTab = LoggedInTab.MyAgenda
                            selectedEventId = null
                            eventSubPage = EventSubPage.Details
                        },
                        colors = bottomNavColors,
                        icon = { Icon(Icons.Filled.Event, contentDescription = "My Agenda", modifier = Modifier.size(20.dp)) },
                        label = { Text("Agenda", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == LoggedInTab.Info,
                        onClick = {
                            activeTab = LoggedInTab.Info
                            selectedEventId = null
                            eventSubPage = EventSubPage.Details
                        },
                        colors = bottomNavColors,
                        icon = { Icon(Icons.Filled.Info, contentDescription = "Info", modifier = Modifier.size(20.dp)) },
                        label = { Text("Info", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == LoggedInTab.MyRequests,
                        onClick = {
                            activeTab = LoggedInTab.MyRequests
                            selectedEventId = null
                            eventSubPage = EventSubPage.Details
                        },
                        colors = bottomNavColors,
                        icon = { Icon(Icons.Filled.Person, contentDescription = "My Requests", modifier = Modifier.size(20.dp)) },
                        label = { Text("Social", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == LoggedInTab.Consultations,
                        onClick = {
                            activeTab = LoggedInTab.Consultations
                            selectedEventId = null
                            eventSubPage = EventSubPage.Details
                        },
                        colors = bottomNavColors,
                        icon = { Icon(Icons.Filled.EventAvailable, contentDescription = "Consultations", modifier = Modifier.size(20.dp)) },
                        label = { Text("Consult", fontSize = 10.sp) }
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
                    onLogout = {
                        loggedInUserId = null
                        persistLoggedInUserId(context, null)
                    },
                    modifier = Modifier.padding(innerPadding)
                )

                LoggedInTab.MyRequests -> MyRequestsScreen(
                    sentRequests = sentRequests,
                    receivedRequests = receivedRequests,
                    connections = connections,
                    userNamesById = userNamesById,
                    onAccept = onAcceptRequest,
                    onDecline = onDeclineRequest,
                    modifier = Modifier.padding(innerPadding)
                )

                LoggedInTab.Consultations -> ConsultationsScreen(
                    currentUserId = currentUser.id,
                    experts = experts,
                    slots = consultationSlots,
                    bookings = consultationBookings,
                    onBookSlot = onBookConsultationSlot,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
    }
}

private fun getPersistedLoggedInUserId(context: Context): Int? {
    val prefs = context.getSharedPreferences("app_session", Context.MODE_PRIVATE)
    val userId = prefs.getInt("logged_in_user_id", -1)
    return if (userId == -1) null else userId
}

private fun persistLoggedInUserId(context: Context, userId: Int?) {
    val prefs = context.getSharedPreferences("app_session", Context.MODE_PRIVATE)
    prefs.edit().apply {
        if (userId == null) remove("logged_in_user_id") else putInt("logged_in_user_id", userId)
    }.apply()
}

private enum class LoggedInTab {
    Home,
    MyAgenda,
    Info,
    MyRequests,
    Consultations
}

private enum class EventSubPage {
    Details,
    Participants
}
