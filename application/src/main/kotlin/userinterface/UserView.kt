package userinterface

import models.DatabaseManager
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import controller.UserController
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Face
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import org.example.userinterface.views.ProblemScreen
import org.example.userinterface.views.SelectionScreen
import org.example.userinterface.views.UserScreen
import userinterface.components.*

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import models.MessageData
import models.Response

/**
 * Screens
 */
enum class Screen(
    val label: String,
    val icon: ImageVector
) {
    SelectionScreen(
        label = "Search",
        icon = Icons.Filled.Search
    ),
    ProblemScreen(
        label = "Problem",
        icon = Icons.Filled.Edit
    ),
    PuzzleRushScreen(
        label = "PuzzleRush",
        icon = Icons.Filled.Star
    ),
    UserScreen(
        label = "User",
        icon = Icons.Filled.Face
    ),
}
enum class ViewEvent {
    SelectProblemEvent,
    CodeUpdateEvent,
    UserLoginEvent,
    UserLogoutEvent,
    FriendRequestEvent,
    FriendRequestResponseEvent,
    LobbyInviteEvent,
    LobbyInviteResponseEvent,
    SelectTestCases,
    ResetGameEvent,
    MatchGameEvent
}

suspend fun processInviteResponse(
    userViewModel: UserViewModel,
    userController: UserController,
    response: Response
) {
    userViewModel.model.lobbyInvites -= 1
    val invite = userViewModel.pendingLobbyInvitations.removeLast()
    userController.invoke(
        ViewEvent.LobbyInviteResponseEvent,
        MessageData.LobbyInviteResponse(
            invite.to, invite.from, invite.lobbyId, response
        )
    )
}

suspend fun processFriendResponse(
    userViewModel: UserViewModel,
    userController: UserController,
    response: Response
) {
    userViewModel.model.friendRequests -= 1
    val invite = userViewModel.pendingFriendRequests.removeLast()
    userController.invoke(
        ViewEvent.FriendRequestResponseEvent,
        MessageData.FriendRequestResponse(
            invite.to, invite.from, response
        )
    )
}

@Composable
fun UserView(
    userViewModel: UserViewModel,
    userController: UserController,
    databaseManager: DatabaseManager,
) {
    val viewModel by remember { mutableStateOf(userViewModel) }
    val controller by remember { mutableStateOf(userController) }
    val screens = Screen.entries.toList()
    val navController by rememberNavController(Screen.UserScreen.name)
    val currentScreen by remember {
        navController.currentScreen
    }
    Row(
        modifier = Modifier.fillMaxSize(),
    ) {
        NavigationRail(
        ) {
            val textColor = if (navController.getNavigationEnabled()) Color.Unspecified else Color.Gray
            screens.forEach {
                NavigationRailItem(
                    selected = currentScreen == it.name,
                    icon = {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = it.label,
                            tint = if (navController.getNavigationEnabled()) Color.Unspecified else Color.Gray
                        )
                    },

                    label = {
                        Text(it.label, color=textColor)

                    },
                    alwaysShowLabel = false,
                    onClick = {
                        if (navController.getNavigationEnabled()) {
                            navController.navigate(it.name)
                        }
                    },
                    enabled = navController.getNavigationEnabled(),
                )
            }
        }

        val coroutineScope = rememberCoroutineScope() // rememberCoroutineScope is used to launch coroutines in Compose

        if (userViewModel.selectedProblem == null) {
            userController.invoke(
                ViewEvent.SelectProblemEvent,
                databaseManager.getRandomProblem(),
            )
        }

        repeat(userViewModel.lobbyInvites.value) { i ->
            val invite = userViewModel.pendingLobbyInvitations[i]
            val title = "Puzzle Rush Invitation"
            val text = "You have a lobby invitation from ${invite.from}. Do you want to accept it?"
            ResponseDialog(
                onAccept = {
                    coroutineScope.launch {
                        processInviteResponse(userViewModel, userController, Response.ACCEPTED)
                        if (navController.getNavigationEnabled()) {
                            navController.navigate(Screen.PuzzleRushScreen.name)
                        }
                    }
                },
                onDecline = {
                    coroutineScope.launch {
                        processInviteResponse(userViewModel, userController, Response.DECLINED)
                    }
                },
                title, text
            )
        }

        repeat(userViewModel.friendRequests.value) { i ->
            val request = userViewModel.pendingFriendRequests[i]
            val title = "Friend Request"
            val text = "You have a friend request from ${request.from}. Do you want to accept it?"
            ResponseDialog(
                onAccept = {
                    coroutineScope.launch {
                        processFriendResponse(userViewModel, userController, Response.ACCEPTED)
                        databaseManager.createFriendship(request.from, request.to)
                    }
                },
                onDecline = {
                    coroutineScope.launch {
                        processFriendResponse(userViewModel, userController, Response.DECLINED)
                    }
                },
                title, text
            )
        }

        MainNavigationHost(
            navController = navController,
            userViewModel = userViewModel,
            userController = userController,
            databaseManager = databaseManager,
        )
    }
}
@Composable
fun MainNavigationHost(
    navController: NavController,
    userViewModel: UserViewModel,
    userController: UserController,
    databaseManager: DatabaseManager,
) {
    NavigationHost(navController) {
        composable(Screen.SelectionScreen.name) {
            SelectionScreen(
                navController = navController,
                databaseManager = databaseManager,
                userController = userController,
            )
        }

        composable(Screen.ProblemScreen.name) {
            ProblemScreen(
                databaseManager = databaseManager,
                userViewModel = userViewModel,
                userController = userController,
            )
        }

        composable(Screen.PuzzleRushScreen.name) {
            PuzzleRushScreen(
                databaseManager = databaseManager,
                userViewModel = userViewModel,
                navController = navController,
                userController = userController
            )
        }

        composable(Screen.UserScreen.name) {
            UserScreen(
                navController = navController,
                userViewModel = userViewModel,
                userController = userController,
                databaseManager = databaseManager
            )
        }
    }.build()
}

