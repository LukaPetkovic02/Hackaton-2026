package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppContent(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AppContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val users = remember { loadUsersFromCsv(context) }
    var loggedInUser by remember { mutableStateOf<User?>(null) }

    if (loggedInUser == null) {
        LoginScreen(
            users = users,
            onLoginSuccess = { matchedUser -> loggedInUser = matchedUser },
            modifier = modifier
        )
    } else {
        HomeScreen(
            user = loggedInUser!!,
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
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
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
                Text(text = "09:00 Opening & Registration", modifier = Modifier.padding(top = 12.dp))
                Text(text = "10:00 Expert Session: Fiscalization Trends", modifier = Modifier.padding(top = 6.dp))
                Text(text = "11:30 Networking Break", modifier = Modifier.padding(top = 6.dp))
                Text(text = "13:00 Lunch", modifier = Modifier.padding(top = 6.dp))
                Text(text = "14:00 POS Technology Panel", modifier = Modifier.padding(top = 6.dp))
                Text(text = "16:00 Consultations", modifier = Modifier.padding(top = 6.dp))
                Text(text = "18:00 Evening Networking", modifier = Modifier.padding(top = 6.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Event info",
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "A full day of talks, networking, and practical consultations focused on fiscalization and POS technology.",
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
                User("Demo", "User", "demo@example.com", "1234")
            ),
            onLoginSuccess = {}
        )
    }
}

data class User(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String
)

fun loadUsersFromCsv(context: Context): List<User> {
    return try {
        context.assets.open("users.csv").bufferedReader().useLines { lines ->
            lines.drop(1).mapNotNull { line ->
                val fields = line.split(",").map { it.trim() }
                if (fields.size < 4) return@mapNotNull null
                User(
                    firstName = fields[0],
                    lastName = fields[1],
                    email = fields[2],
                    password = fields[3]
                )
            }.toList()
        }
    } catch (_: Exception) {
        emptyList()
    }
}
