package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.loadEventsFromCsv
import com.example.myapplication.data.loadSavedEvents
import com.example.myapplication.data.loadUsersFromCsv
import com.example.myapplication.data.saveSavedEvents
import com.example.myapplication.model.SavedEvent
import com.example.myapplication.model.User
import com.example.myapplication.ui.HomeScreen
import com.example.myapplication.ui.LoginScreen
import com.example.myapplication.ui.SavedEventsScreen
import com.example.myapplication.util.currentTimestamp

@Composable
fun AppContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val users = remember { loadUsersFromCsv(context) }
    val events = remember { loadEventsFromCsv(context) }
    var savedEvents by remember { mutableStateOf(loadSavedEvents(context)) }
    var loggedInUser by remember { mutableStateOf<User?>(null) }
    var activePage by remember { mutableStateOf(LoggedInPage.Home) }

    if (loggedInUser == null) {
        LoginScreen(
            users = users,
            onLoginSuccess = { matchedUser ->
                loggedInUser = matchedUser
                activePage = LoggedInPage.Home
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

        when (activePage) {
            LoggedInPage.Home -> HomeScreen(
                user = currentUser,
                events = events,
                savedEventIds = savedEventIds,
                onToggleSave = { eventId ->
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
                },
                onOpenSavedEvents = { activePage = LoggedInPage.SavedEvents },
                onLogout = { loggedInUser = null },
                modifier = modifier
            )

            LoggedInPage.SavedEvents -> SavedEventsScreen(
                user = currentUser,
                savedEvents = savedUserEvents,
                savedEventIds = savedEventIds,
                onToggleSave = { eventId ->
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
                },
                onBack = { activePage = LoggedInPage.Home },
                modifier = modifier
            )
        }
    }
}

private enum class LoggedInPage {
    Home,
    SavedEvents
}
