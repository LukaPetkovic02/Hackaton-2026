package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.loadEventsFromCsv
import com.example.myapplication.data.loadSavedEvents
import com.example.myapplication.data.loadUsersFromCsv
import com.example.myapplication.data.saveSavedEvents
import com.example.myapplication.model.SavedEvent
import com.example.myapplication.model.User
import com.example.myapplication.ui.HomeScreen
import com.example.myapplication.ui.InfoScreen
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
    var activeTab by remember { mutableStateOf(LoggedInTab.Home) }

    if (loggedInUser == null) {
        LoginScreen(
            users = users,
            onLoginSuccess = { matchedUser ->
                loggedInUser = matchedUser
                activeTab = LoggedInTab.Home
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

        Column(
            modifier = modifier.fillMaxSize()
        ) {
            when (activeTab) {
                LoggedInTab.Home -> HomeScreen(
                    user = currentUser,
                    events = events,
                    savedEventIds = savedEventIds,
                    onToggleSave = onToggleSave,
                    modifier = Modifier.weight(1f)
                )

                LoggedInTab.MyAgenda -> SavedEventsScreen(
                    user = currentUser,
                    savedEvents = savedUserEvents,
                    savedEventIds = savedEventIds,
                    onToggleSave = onToggleSave,
                    modifier = Modifier.weight(1f)
                )

                LoggedInTab.Info -> InfoScreen(
                    user = currentUser,
                    onLogout = { loggedInUser = null },
                    modifier = Modifier.weight(1f)
                )
            }

            NavigationBar {
                NavigationBarItem(
                    selected = activeTab == LoggedInTab.Home,
                    onClick = { activeTab = LoggedInTab.Home },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = activeTab == LoggedInTab.MyAgenda,
                    onClick = { activeTab = LoggedInTab.MyAgenda },
                    icon = { Icon(Icons.Filled.Event, contentDescription = "My Agenda") },
                    label = { Text("My Agenda") }
                )
                NavigationBarItem(
                    selected = activeTab == LoggedInTab.Info,
                    onClick = { activeTab = LoggedInTab.Info },
                    icon = { Icon(Icons.Filled.Info, contentDescription = "Info") },
                    label = { Text("Info") }
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
