package org.example.userinterface.views

import models.DatabaseManager
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import controller.UserController
import models.User
import userinterface.UserViewModel
import userinterface.ViewEvent
import userinterface.components.clickButton
import userinterface.components.NavController
import userinterface.components.handleTabKeyEvent

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userViewModel: UserViewModel,
    userController: UserController,
    databaseManager: DatabaseManager,
    onSwitchScreen: () -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val emailRegex = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}")
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val registerUser: () -> Unit = {
        if (username.isBlank()) {
            message = "Username cannot be blank"
        } else if (displayName.isBlank()) {
            message = "Display name cannot be blank"
        } else if (!email.matches(emailRegex)) {
            message = "Improper email address format"
        } else if (password.isBlank()) {
            message = "Invalid password"
        } else if (password != confirmPassword) {
            message = "Passwords do not match"
        } else {
            val user = databaseManager.getUser(username)
            if (user != null) {
                message = "Username already exists"
            } else {
                println("Registration successful")
                var newUser = User(
                    username, displayName, User.hashPassword(password), email
                )
                databaseManager.createUser(newUser)
                navController.setNavigationEnabled(true)
                userController.invoke(ViewEvent.UserLoginEvent, newUser)
            }
        }
    }

    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.border(width = 3.dp, color = Color.LightGray.copy(0.3f)))
    {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Register",
                style = MaterialTheme.typography.h4,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .onPreviewKeyEvent(handleTabKeyEvent(registerUser, focusManager))
            )

            TextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .onPreviewKeyEvent(handleTabKeyEvent(registerUser, focusManager))
            )

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .onPreviewKeyEvent(handleTabKeyEvent(registerUser, focusManager))
            )

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .onPreviewKeyEvent(handleTabKeyEvent(registerUser, focusManager))
            )

            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .onPreviewKeyEvent(handleTabKeyEvent(registerUser, focusManager, true))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val buttonModifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .weight(1f)
                    .padding(end = 8.dp)

                clickButton("Register", buttonModifier, onClick = { registerUser() })
                clickButton("Already have an account? Login!", buttonModifier, onClick = onSwitchScreen)
            }
            Text(
                text = message,
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}