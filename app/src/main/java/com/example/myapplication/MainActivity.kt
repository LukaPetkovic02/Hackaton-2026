package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AppContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val users = remember { loadUsersFromCsv(context) }
    val events = remember { loadEventsFromCsv(context) }
    var savedEvents by remember { mutableStateOf(loadSavedEvents(context)) }
    var loggedInUser by remember { mutableStateOf<User?>(null) }

    if (loggedInUser == null) {
        LoginScreen(
            users = users,
            onLoginSuccess = { matchedUser -> loggedInUser = matchedUser },
            modifier = modifier
        )
    } else {
        val currentUser = loggedInUser!!
        val savedEventIds = savedEvents
            .filter { it.userId == currentUser.id }
            .map { it.eventId }
            .toSet()

        HomeScreen(
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
            onLogout = { loggedInUser = null },
            modifier = modifier
        )
    }
}

@Composable
fun LoginScreen(
    users: List<User>,
    onLoginSuccess: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var statusMessage by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Login")

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true,
            visualTransformation = if (showPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Button(
            onClick = {
                val normalizedEmail = email.trim()
                val normalizedPassword = password.trim()
                statusMessage = if (normalizedEmail.isBlank() || normalizedPassword.isBlank()) {
                    "Please enter both email and password."
                } else {
                    val matchedUser = users.find {
                        it.email.equals(normalizedEmail, ignoreCase = true) &&
                            it.password == normalizedPassword
                    }
                    if (matchedUser != null) {
                        onLoginSuccess(matchedUser)
                        "Login successful."
                    } else {
                        "Invalid email or password."
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(text = "Login")
        }

        Button(
            onClick = { showPassword = !showPassword },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(if (showPassword) "Hide password" else "Show password")
        }

        if (statusMessage.isNotBlank()) {
            Text(
                text = statusMessage,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun HomeScreen(
    user: User,
    events: List<Event>,
    savedEventIds: Set<Int>,
    onToggleSave: (Int) -> Unit,
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
            text = "Community Day 2026",
            modifier = Modifier.padding(top = 24.dp),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Welcome, ${user.firstName}!",
            modifier = Modifier.padding(top = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Today's schedule", fontWeight = FontWeight.SemiBold)

                events.forEach { event ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${event.startTime} ${event.title}",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = event.location,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        val isSaved = savedEventIds.contains(event.id)
                        Button(
                            onClick = { onToggleSave(event.id) },
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            Text(if (isSaved) "Unsave" else "Save")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Event info",
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "A full day of talks, networking, and practical consultations focused on fiscalization and POS technology. You saved ${savedEventIds.size} event(s).",
            modifier = Modifier.padding(top = 4.dp)
        )
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp)
        ) {
            Text("Logout")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MyApplicationTheme {
        LoginScreen(
            users = listOf(
                User(1, "Demo", "User", "demo@example.com", "1234")
            ),
            onLoginSuccess = {}
        )
    }
}

data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)

data class Event(
    val id: Int,
    val title: String,
    val startTime: String,
    val endTime: String,
    val location: String,
    val speaker: String,
    val description: String,
    val category: String
)

data class SavedEvent(
    val userId: Int,
    val eventId: Int,
    val savedAt: String
)

fun loadUsersFromCsv(context: Context): List<User> {
    return try {
        context.assets.open("users.csv").bufferedReader().useLines { lines ->
            val rows = lines.toList()
            if (rows.isEmpty()) return@useLines emptyList()

            val headerFields = rows.first().split(",").map { it.trim().removePrefix("\uFEFF") }
            val hasUserId = headerFields.firstOrNull().equals("user_id", ignoreCase = true)

            rows.drop(1).mapIndexedNotNull { index, line ->
                val fields = line.split(",").map { it.trim() }
                if (hasUserId) {
                    if (fields.size < 5) return@mapIndexedNotNull null
                    User(
                        id = fields[0].toIntOrNull() ?: (index + 1),
                        firstName = fields[1],
                        lastName = fields[2],
                        email = fields[3],
                        password = fields[4]
                    )
                } else {
                    if (fields.size < 4) return@mapIndexedNotNull null
                    User(
                        id = index + 1,
                        firstName = fields[0],
                        lastName = fields[1],
                        email = fields[2],
                        password = fields[3]
                    )
                }
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun loadEventsFromCsv(context: Context): List<Event> {
    return try {
        context.assets.open("events.csv").bufferedReader().useLines { lines ->
            lines.drop(1).mapNotNull { line ->
                val fields = line.split(",").map { it.trim() }
                if (fields.size < 8) return@mapNotNull null
                Event(
                    id = fields[0].toIntOrNull() ?: return@mapNotNull null,
                    title = fields[1],
                    startTime = fields[2],
                    endTime = fields[3],
                    location = fields[4],
                    speaker = fields[5],
                    description = fields[6],
                    category = fields[7]
                )
            }.toList()
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun loadSavedEvents(context: Context): List<SavedEvent> {
    val file = getSavedEventsFile(context)
    return try {
        if (!file.exists()) {
            val initialContent = context.assets.open("saved_events.csv").bufferedReader().use { it.readText() }
            file.writeText(initialContent)
        }

        file.bufferedReader().useLines { lines ->
            lines.drop(1).mapNotNull { line ->
                val fields = line.split(",").map { it.trim() }
                if (fields.size < 3) return@mapNotNull null
                SavedEvent(
                    userId = fields[0].toIntOrNull() ?: return@mapNotNull null,
                    eventId = fields[1].toIntOrNull() ?: return@mapNotNull null,
                    savedAt = fields[2]
                )
            }.toList()
        }
    } catch (_: Exception) {
        emptyList()
    }
}

fun saveSavedEvents(context: Context, savedEvents: List<SavedEvent>) {
    val csvContent = buildString {
        appendLine("user_id,event_id,saved_at")
        savedEvents.forEach { savedEvent ->
            appendLine("${savedEvent.userId},${savedEvent.eventId},${savedEvent.savedAt}")
        }
    }
    getSavedEventsFile(context).writeText(csvContent)
}

fun getSavedEventsFile(context: Context): File {
    return File(context.filesDir, "saved_events.csv")
}

fun currentTimestamp(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    return formatter.format(Date())
}
