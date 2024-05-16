package org.example.userinterface.views

import androidx.compose.foundation.*
import models.DatabaseManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import controller.UserController
import userinterface.UserViewModel
import userinterface.components.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Info
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource

import models.User
import userinterface.ViewEvent


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import userinterface.components.clickButton
import kotlinx.coroutines.launch
import models.MessageData


@Composable
fun AddFriendSection(
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel,
    userController: UserController,
    databaseManager: DatabaseManager,
) {
    var friendUserName by remember { mutableStateOf("") }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = friendUserName,
            onValueChange = { friendUserName = it },
            label = { Text("Friend's Username") },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = {
                userController.invoke(
                    ViewEvent.FriendRequestEvent,
                    MessageData.FriendRequest(
                        userViewModel.currentUser.value!!.userName,
                        friendUserName
                    )
                )
                friendUserName = ""
            },
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Text("Send Request")
        }
    }
}

@Composable
fun UserScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userViewModel: UserViewModel,
    userController: UserController,
    databaseManager: DatabaseManager,
) {
    if (userViewModel.currentUser.value == null) {
        // User is not logged in
        var isRegisterScreen by remember { mutableStateOf(false) }
        var onSwitchScreen = { isRegisterScreen = !isRegisterScreen }

        if (isRegisterScreen) {
            RegisterScreen(
                navController=navController,
                userViewModel=userViewModel,
                userController=userController,
                databaseManager=databaseManager,
                onSwitchScreen=onSwitchScreen
            )
        } else {
            LoginScreen(
                navController=navController,
                userViewModel=userViewModel,
                userController=userController,
                databaseManager=databaseManager,
                onSwitchScreen=onSwitchScreen
            )
        }

    } else {
        // User is logged in
        AccountPage(
            userViewModel=userViewModel,
            userController=userController,
            databaseManager=databaseManager,
            navController=navController
        )
    }
}


@Composable
fun AccountPage(
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel,
    userController: UserController,
    navController: NavController,
    databaseManager: DatabaseManager,
) {
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.border(width = 3.dp, color = Color.LightGray.copy(0.3f)))
    {
        Column(
            modifier = modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfilePhotoSection(userViewModel, userController, navController)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    elevation = 8.dp
                ) { FriendsListSection(userViewModel, databaseManager, userController) }

                Card(
                    modifier = Modifier.weight(2f),
                    elevation = 8.dp
                ) { UserStatsInfoSection(userViewModel, databaseManager) }
            }
        }
    }
}

@Composable
fun ProfilePhotoSection(
    userViewModel: UserViewModel,
    userController: UserController,
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource("default-profile.png"),
            contentDescription = "Profile Photo",
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.height(150.dp).padding(16.dp)
        ) {
            Text(
                userViewModel.currentUser.value!!.displayName,
                style = MaterialTheme.typography.h3
            )
            Text(
                userViewModel.currentUser.value!!.userName,
                style = MaterialTheme.typography.h4
            )
        }
        Spacer(Modifier.weight(1f))
        Box (
            modifier = Modifier.align(Alignment.Top).height(85.dp)
        ) {
            clickButton("Logout", modifier = Modifier.aspectRatio(1.1f), onClick =  {
                userController.invoke(ViewEvent.UserLogoutEvent, null)
                navController.setNavigationEnabled(false)
            })
        }
    }
}

@Composable
fun FriendsListSection(
    userViewModel: UserViewModel,
    databaseManager: DatabaseManager,
    userController: UserController,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("Friends List", style = MaterialTheme.typography.h5)
        }
        Spacer(modifier = Modifier.height(8.dp))
        AddFriendSection(userViewModel = userViewModel, userController = userController, databaseManager = databaseManager)
        Spacer(modifier = Modifier.height(16.dp))
        var friends by remember { mutableStateOf(emptyList<User>()) }
        // Function to fetch friends list from the database
        fun fetchFriends() {
            friends = databaseManager.getFriendships(userViewModel.model.currentUser!!.userName)
        }
        fetchFriends()
        friends.forEach { friend ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource("default-profile.png"),
                    contentDescription = friend.displayName,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                ) {
                    Text(text = friend.displayName, style = MaterialTheme.typography.body1)
                    Text(text = friend.userName, style = MaterialTheme.typography.body2)
                }
                IconButton(
                    onClick = {
                        userViewModel.currentUser.value?.let {
                            databaseManager.removeFriendship(
                                it.userName, friend.userName
                            )
                        }
                        fetchFriends()
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove friend")
                }
            }
        }
    }
}

@Composable
fun UserStatsInfoSection(
    userViewModel: UserViewModel,
    databaseManager: DatabaseManager,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("User Stats/Info", style = MaterialTheme.typography.h5)
        }
        Spacer(modifier = Modifier.height(12.dp))
        val metrics = databaseManager.getUserMetrics(userViewModel.currentUser.value!!.userName)

        if (metrics != null) {
            Text("Total Completed Problems:", style = MaterialTheme.typography.h6)
//            Text("100", style = MaterialTheme.typography.h6)
            Text("${metrics.totalCompleted}", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Lifetime Hints: ${metrics.lifetimeHints}")
            Text("Submitted Problems: ${metrics.submittedProblems}")
        }

        val pieChartData = listOf(
            PieSlices(value = metrics?.easyCompleted ?: 0, color = Color.Red, "Easy Completed"),
            PieSlices(value = metrics?.mediumCompleted ?: 0, color = Color.Green, "Medium Completed"),
            PieSlices(value = metrics?.hardCompleted ?: 0, color = Color.Blue, "Hard Completed"),
//            PieSlices(value = 15, color = Color.Red, "Easy Completed"),
//            PieSlices(value = 50, color = Color.Green, "Medium Completed"),
//            PieSlices(value = 35, color = Color.Blue, "Hard Completed"),
        )
        Spacer(Modifier.weight(1f))
        Canvas(
            modifier = Modifier.size(200.dp)
        ) {
            val total = metrics?.totalCompleted ?: 0
//            val total = 100
            var startAngle = 0f

            val strokeWidth = 8.dp.toPx()
            val radius = size.minDimension / 2 - strokeWidth / 2
            val centerOffset = Offset(4f, 4f)

            // Draw the gray border circle
            drawCircle(
                color = Color.Gray.copy(alpha = 0.2f),
                radius = radius,
                center = center + centerOffset,
                style = Stroke(width = strokeWidth)
            )

            if (total != 0) {
                pieChartData.forEach { datum ->
                    val angle = (datum.value.toFloat() / total) * 360f

                    drawArc(
                        color = datum.color.copy(alpha = 0.5f),
                        startAngle = startAngle,
                        sweepAngle = angle,
                        useCenter = true,
                        topLeft = Offset(5f, 5f)
                    )

                    // Gradient for depth
                    val gradient = Brush.radialGradient(
                        colors = listOf(datum.color, datum.color.copy(alpha = 0.85f), datum.color.copy(alpha = 0.7f),),
                        center = Offset.Zero,
                        radius = size.minDimension / 2f
                    )

                    // Draws actual pie chart
                    drawArc(
                        brush = gradient,
                        startAngle = startAngle,
                        sweepAngle = angle,
                        useCenter = true
                    )
                    startAngle += angle
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row {
            pieChartData.forEach { slice ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(slice.color),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${slice.descriptor}: ${slice.value}")
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Divider(
            color = MaterialTheme.colors.primary.copy(alpha = 0.2f),
            modifier = Modifier.fillMaxWidth().height(1.dp)
        )
        Spacer(Modifier.height(8.dp))
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (metrics != null) {
                Text("Top Puzzle Score:", style = MaterialTheme.typography.h6)
                Text("${metrics.topPuzzleScore}", style = MaterialTheme.typography.h6)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

data class PieSlices(val value: Int, val color: Color, val descriptor: String)
