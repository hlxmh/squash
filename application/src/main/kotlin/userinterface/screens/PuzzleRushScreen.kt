package userinterface

import models.DatabaseManager
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import controller.UserController
import kotlinx.coroutines.launch
import models.LobbyStatus
import models.PlayerStatus
import models.User
import userinterface.components.CountdownTimer
import userinterface.components.clickButton
import userinterface.components.NavController
import userinterface.components.primarySelectableButton

const val MAX_PUZZLE_PER_GAME = 5

@Composable
fun PuzzleRushScreen(
    databaseManager: DatabaseManager,
    userViewModel: UserViewModel,
    navController: NavController,
    userController: UserController
    )
{
    if (userViewModel.gameStatus.value.playerStatus == PlayerStatus.IN_LOBBY ||
        userViewModel.gameStatus.value.playerStatus == PlayerStatus.IN_GAME) {
        navController.setNavigationEnabled(false)
    } else {
        navController.setNavigationEnabled(true)
    }

    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.border(width = 3.dp, color = Color.LightGray.copy(0.3f)))
    {
        Row {
            // Column for ProgressSection
            Column(
                modifier = Modifier.weight(2f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface (
                    modifier = Modifier.padding(12.dp).shadow(elevation = 8.dp),
                ) {
                    PuzzleSection(
                        lobby = userViewModel.lobbyStatus.value,
                        matchStart = userViewModel.gameStatus.value.playerStatus === PlayerStatus.IN_GAME,
                        databaseManager = databaseManager,
                        userController = userController,
                        userViewModel = userViewModel
                    )
                }
            }
            // Column for InfoSection
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                InfoSection(databaseManager,
                    userViewModel.lobbyStatus.value.id != null,
                    userViewModel.lobbyStatus.value.users.size == 4,
                    userController,
                    userViewModel)
            }
        }
    }
}

@Composable
fun PuzzleSection(
    lobby: LobbyStatus,
    matchStart: Boolean,
    databaseManager: DatabaseManager,
    userController: UserController,
    userViewModel: UserViewModel,
    )
= if (!matchStart) {
    PuzzleWaiting(lobby)
}
else {
    PuzzleGame(databaseManager, userController, userViewModel)
}

@Composable
fun InfoSection(
    databaseManager: DatabaseManager,
    inLobby: Boolean,
    lobbyFull: Boolean,
    userController: UserController,
    userViewModel: UserViewModel,
    )
{
    val friends = databaseManager.getFriendships(userViewModel.model.currentUser!!.userName)
    var board by remember { mutableStateOf(Board.EMPTY) }
    val userMetrics = userViewModel.currentUser.value?.let { databaseManager.getUserMetrics(it.userName) }

    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.weight(1.5f),
            verticalAlignment = Alignment.CenterVertically,
        )
        {
            Card (
                modifier = Modifier.padding(12.dp).fillMaxSize(),
                border = BorderStroke(2.dp, SolidColor(MaterialTheme.colors.primary)),
                elevation = 8.dp
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth()
                ) {
                    var timeLeft by remember { mutableStateOf<Int>(0) }
                    var prevRound by remember { mutableStateOf<Int>(0) }
                    val puzzleRound = userViewModel.gameStatus.value.round
                    if (prevRound != MAX_PUZZLE_PER_GAME && prevRound != puzzleRound) {
                        timeLeft = 14
                    }
                    prevRound = puzzleRound

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.White)
                            .padding(4.dp)
                            .border(
                                width = 1.dp,
                                brush = SolidColor(MaterialTheme.colors.primaryVariant),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    )
                    {
                        val puzzlesLeft = (MAX_PUZZLE_PER_GAME - puzzleRound).coerceAtLeast(0)
                        val text = if (!lobbyFull) {
                            "Puzzles Per Session:\n${MAX_PUZZLE_PER_GAME}"
                        } else {
                            "Puzzles Left:\n${puzzlesLeft}"
                        }

                        Text(
                            text = text, // TODO: (made) need to actually show best today scores.
                            style = MaterialTheme.typography.h6,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            color = MaterialTheme.colors.primary,
                        )
                    }
                    Divider(
                        color = MaterialTheme.colors.primary.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxHeight().width(1.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.White)
                            .padding(4.dp)
                            .border(
                                width = 1.dp,
                                brush = SolidColor(MaterialTheme.colors.primaryVariant),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    )
                    {
                        val topPuzzleScore = userMetrics?.topPuzzleScore ?: 0
                        val text: String
                        if (!lobbyFull) {
                            text = "Top Score:\n${topPuzzleScore}"
                        } else {
                            CountdownTimer(timeLeft, updateTimeLeft = { timeLeft = it })
                            text = "Time Left:\n${timeLeft}"
                        }
                        Text(
                            text = text, // TODO: (made) need to actually show top scores.
                            style = MaterialTheme.typography.h6,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            color = MaterialTheme.colors.primary,
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier.weight(3f),
                horizontalAlignment = Alignment.CenterHorizontally,
            )
            {
                primarySelectableButton("Invite Friends", board == Board.FRIENDBOARD, Modifier.fillMaxWidth()) {
                    board = Board.FRIENDBOARD
                }
            }
            Spacer(Modifier.width(4.dp))
            Column(
                modifier = Modifier.weight(3f),
                horizontalAlignment = Alignment.CenterHorizontally,
            )
            {
                primarySelectableButton("Leaderboard", board == Board.LEADERBOARD, Modifier.fillMaxWidth()) {
                    board = Board.LEADERBOARD
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.weight(4f)) {
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier
                    .weight(6f)
                    .fillMaxSize()
                    .background(color = MaterialTheme.colors.secondaryVariant)
                    .border(2.dp, MaterialTheme.colors.primary, RoundedCornerShape(8.dp))
                    .verticalScroll(rememberScrollState())
            ) {
                if (board == Board.FRIENDBOARD) {
                    friends.forEach { user ->
                        FriendItem(user = user, enabled = !lobbyFull && inLobby, onClick = {
                            println("Invited user: ${user.userName}")
                            coroutineScope.launch() {
                                userController.invoke(
                                    ViewEvent.LobbyInviteEvent,
                                    user.userName
                                )
                            }
                        })
                    }
                }
                if (board == Board.LEADERBOARD) {
                    friends.sortedByDescending { databaseManager.getUserMetrics(it.displayName)?.topPuzzleScore }
                        .forEach { user ->
                            LeaderboardItem(databaseManager, user)
                        }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier.weight(6f),
                verticalArrangement = Arrangement.Center,
            ) {
                clickButton("Match!", Modifier.fillMaxWidth(), !inLobby,
                    {
                        userController.invoke(ViewEvent.MatchGameEvent, null)
                    })
            }
            Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.weight(0.25f))
    }
}
@Composable
fun PuzzleWaiting(lobby: LobbyStatus) {
    val text = "${lobby.users.size}/4\nWaiting..."
    Row(modifier = Modifier.fillMaxSize().background(Color.LightGray.copy(alpha = 0.6f))) {
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight(0.3f)
                .background(Color.LightGray.copy(alpha = 0.4f))
                .padding(8.dp)
                .shadow(
                    elevation = 4.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.h3,
                textAlign = TextAlign.Center,
                fontSize = 40.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

// GAME FLOW:
// - each round lasts 15 seconds
// - when you select an answer, you will wait for the entirety of the duration of the round
// - after the round ends, there is a 5 second review period to check the right answer and your updated score
@Composable
fun PuzzleGame(databaseManager: DatabaseManager, userController: UserController, userViewModel: UserViewModel) {
    if (userViewModel.gameStatus.value.round <= MAX_PUZZLE_PER_GAME) {
        val currentPuzzle = userViewModel.gameStatus.value.puzzle!!

        Row(modifier = Modifier.fillMaxSize().background(Color.LightGray.copy(alpha = 0.6f))) {
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(8f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(7f)
                        .background(Color.LightGray.copy(alpha = 0.4f))
                        .padding(8.dp)
                        .shadow(elevation = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentPuzzle.description,
                        style = MaterialTheme.typography.h3,
                        fontSize = 16.sp // TODO: (made) some descriptions are not displaying very nicely.
                    )
                }

                Spacer(modifier = Modifier.weight(0.5f))

                Column(modifier = Modifier.weight(2f)) {
                    if (userViewModel.gameStatus.value.correctAnswer == -1) { // answering period
                        Row(modifier = Modifier.weight(1f)) {
                            clickButton(currentPuzzle.choices[0], modifier = Modifier.weight(1f), enabled = userViewModel.gameStatus.value.playerAnswer == -1, onClick = {
                                userController.invokeAnswerQuestion(userViewModel.gameStatus.value.round, 0)
                            })
                            clickButton(currentPuzzle.choices[1], modifier = Modifier.weight(1f),  enabled = userViewModel.gameStatus.value.playerAnswer == -1, onClick = {
                                userController.invokeAnswerQuestion(userViewModel.gameStatus.value.round, 1)
                            })
                        }
                        Row(modifier = Modifier.weight(1f)) {
                            clickButton(currentPuzzle.choices[2], modifier = Modifier.weight(1f),  enabled = userViewModel.gameStatus.value.playerAnswer == -1, onClick = {
                                userController.invokeAnswerQuestion(userViewModel.gameStatus.value.round, 2)
                            })
                            clickButton(currentPuzzle.choices[3], modifier = Modifier.weight(1f),  enabled = userViewModel.gameStatus.value.playerAnswer == -1, onClick = {
                                userController.invokeAnswerQuestion(userViewModel.gameStatus.value.round, 3)
                            })
                        }
                    } else { // review period
                        val correct = userViewModel.gameStatus.value.correctAnswer
                        val correctColor = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Green,
                            contentColor = MaterialTheme.colors.onPrimary)

                        val incorrectColor = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Red,
                            contentColor = MaterialTheme.colors.onPrimary)

                        val normalColor = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary,
                            contentColor = MaterialTheme.colors.onPrimary)

                        val text = "Your score:\n${userViewModel.gameStatus.value.score}"

                        Text(
                            text = text,
                            style = MaterialTheme.typography.h3,
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp
                        )

                        // TODO: keep the color but also disable buttons
                        Row(modifier = Modifier.weight(1f)) {
                            clickButton(currentPuzzle.choices[0], modifier = Modifier.weight(1f), enabled = true, onClick = {
                            }, colors = if (correct == 0) correctColor else if (userViewModel.gameStatus.value.playerAnswer == 0) incorrectColor else normalColor)
                            clickButton(currentPuzzle.choices[1], modifier = Modifier.weight(1f), enabled = true, onClick = {
                            }, colors = if (correct == 1) correctColor else if (userViewModel.gameStatus.value.playerAnswer == 1) incorrectColor else normalColor)
                        }
                        Row(modifier = Modifier.weight(1f)) {
                            clickButton(currentPuzzle.choices[2], modifier = Modifier.weight(1f), enabled = true, onClick = {
                            }, colors = if (correct == 2) correctColor else if (userViewModel.gameStatus.value.playerAnswer == 2) incorrectColor else normalColor)
                            clickButton(currentPuzzle.choices[3], modifier = Modifier.weight(1f), enabled = true, onClick = {
                            }, colors = if (correct == 3) correctColor else if (userViewModel.gameStatus.value.playerAnswer == 3) incorrectColor else normalColor)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
    else {
        val finalScore = userViewModel.gameStatus.value.score
        val text = "Your score:\n${finalScore}"
        val userMetrics = userViewModel.currentUser.value?.let { databaseManager.getUserMetrics(it.userName) }
        if (userMetrics != null) {
            if (finalScore > userMetrics.topPuzzleScore) {
                val newUserMetrics = userMetrics.copy(topPuzzleScore = finalScore)
                databaseManager.updateUserMetrics(newUserMetrics)
            }
        }
        Row(modifier = Modifier.fillMaxSize().background(Color.LightGray.copy(alpha = 0.6f))) {
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(0.3f)
                    .background(Color.LightGray.copy(alpha = 0.4f))
                    .padding(8.dp)
                    .shadow(
                        elevation = 4.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.h3,
                    textAlign = TextAlign.Center,
                    fontSize = 40.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            clickButton("OK", modifier = Modifier, onClick = {
                userController.invoke(ViewEvent.ResetGameEvent, null)
            })
        }
    }
}

@Composable
fun FriendItem(user: User, enabled: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(0.75f)
        ) {
            Text(text = user.displayName, style = MaterialTheme.typography.h6)
        }
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(0.25f),
            horizontalAlignment = Alignment.End
        ) {
            Button(
                onClick = onClick,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                    contentColor = MaterialTheme.colors.primary
                )
            ) {
                Text(text = "Invite")
            }
        }
    }
}

@Composable
fun LeaderboardItem(databaseManager: DatabaseManager, user: User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(0.75f)
        ) {
            Text(text = user.displayName, style = MaterialTheme.typography.h6)
        }
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(0.25f),
            horizontalAlignment = Alignment.End
        ) {
            Text(text = databaseManager.getUserMetrics(user.userName)?.topPuzzleScore.toString(), color = MaterialTheme.colors.primary)
        }
    }
}

enum class Board {
    EMPTY, FRIENDBOARD, LEADERBOARD
}
