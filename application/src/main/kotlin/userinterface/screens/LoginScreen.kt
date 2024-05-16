package org.example.userinterface.views

import models.DatabaseManager
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import controller.UserController
import models.User
import userinterface.Screen
import userinterface.UserViewModel
import userinterface.ViewEvent
import userinterface.components.clickButton
import userinterface.components.NavController
import userinterface.components.handleTabKeyEvent

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userViewModel: UserViewModel,
    userController: UserController,
    databaseManager: DatabaseManager,
    onSwitchScreen: () -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val loginUser: () -> Unit = {
        if (username.isBlank()) {
            message = "Invalid username"
        } else {
            val user = databaseManager.getUser(username)
            if (user == null) {
                message = "User not found"
            } else if (User.hashPassword(password) != user.hashedPassword) {
                message = "Incorrect password"
            } else {
                println("Login successful")
                navController.setNavigationEnabled(true)
                userController.invoke(ViewEvent.UserLoginEvent, user)
                navController.navigate(Screen.UserScreen.name)
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
                text = "Login",
                style = MaterialTheme.typography.h4,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                colors = TextFieldDefaults.textFieldColors(textColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .onPreviewKeyEvent(handleTabKeyEvent(loginUser, focusManager))
            )

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                colors = TextFieldDefaults.textFieldColors(textColor = Color.Black),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .onPreviewKeyEvent(handleTabKeyEvent(loginUser, focusManager, true))
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

                clickButton("Login", buttonModifier, onClick = { loginUser() })
                clickButton("Don't have an account? Register!", buttonModifier, onClick = onSwitchScreen)
            }
            Text(
                text = message,
                color = Color.Red,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

}
