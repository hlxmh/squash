package org.example.userinterface.views

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import controller.UserController
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxLanguage
import dev.snipme.kodeview.view.CodeEditText
import kotlinx.coroutines.launch
import models.Problem
import userinterface.ViewEvent
import userinterface.UserViewModel
import com.example.service.Judge0Service
import models.DatabaseManager
import models.UserMetrics
import userinterface.components.primarySelectableButton

/**
 * Composable for the problem screen
 */
@Composable
fun ProblemScreen(
    modifier: Modifier = Modifier,
    databaseManager: DatabaseManager,
    userViewModel: UserViewModel,
    userController: UserController,
) {
    val problem = userViewModel.selectedProblem!!

    val languages = listOf("C", "Kotlin", "Java")
    val language = problem.tags.find { it in languages }

    println("Displaying problem ${problem.id}")
    val coroutineScope = rememberCoroutineScope() // Remember a CoroutineScope tied to the Composable's lifecycle

    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.border(width = 3.dp, color = Color.LightGray.copy(0.3f)))
    {
        Column(
            modifier = modifier.fillMaxSize(),
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f).shadow(1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = problem.title,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier
                            .padding(16.dp)
                    )
                    if (language != null) {
                        Card(
                            modifier = Modifier,
                            backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.75f)
                        ) {
                            Text(
                                text = language,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Surface (
                            modifier = Modifier
                                .padding(8.dp)
                                .border(BorderStroke(2.dp, MaterialTheme.colors.primary),),
                            elevation = 8.dp
                        ) {
                            IconButton(onClick = {
                                coroutineScope.launch { // Launch a new coroutine in the scope
                                    userController.invokeTestSubmission(problem.code) // Call the suspend function within the coroutine
                                }
                            }){
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Run")
                            }
                        }
                    }
                }
//                Row(
//                    modifier = Modifier
//                        .border(BorderStroke(2.dp, Color.Black))
//                        .background(Color.hsv(0F, 0F, 0F, 0.3F, ColorSpaces.Acescg))
//                ) {
//                    IconButton(onClick = { userController.invoke(ViewEvent.SubmitSolutionEvent, problem.code) }) {
//                        Icon(Icons.Filled.PlayArrow, contentDescription = "Run")
//                    }
//                IconButton(onClick = {
//                }) {
//                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Submit")
//                }
//                }
            }
            Row(
                modifier = Modifier.fillMaxSize(),
            ) {
                ResizeableDesc(description = problem.description)
                Column(modifier = Modifier.fillMaxSize()) {
                    CodeEditor(
                        modifier = Modifier.weight(1.0f).shadow(1.dp).background(color = Color.White),
                        code = problem.code,
                        language = language,
                        userController = userController,
                    )
                    ResizeableTests(
                        databaseManager = databaseManager,
                        userController = userController,
                        userViewModel = userViewModel,
                    )
                }
            }
        }
    }
}

@Composable
fun CodeEditor(
    modifier: Modifier,
    code: String,
    language: String?,
    userController: UserController
) {

    val syntaxLang = when (language) {
        "C" -> SyntaxLanguage.C
        "Kotlin" -> SyntaxLanguage.KOTLIN
        "Java" -> SyntaxLanguage.JAVA
        else -> SyntaxLanguage.PYTHON
    }

    val highlights = remember {
        mutableStateOf(
            Highlights
                .Builder(
                    code = code,
                    language = syntaxLang
                )
                .build()
        )
    }

    val coroutineScope = rememberCoroutineScope() // Remember a CoroutineScope tied to the Composable's lifecycle

    MaterialTheme {
        CodeEditText(
            modifier = modifier,
            highlights = highlights.value,
            onValueChange = { textValue ->
                highlights.value = highlights.value.getBuilder()
                    .code(textValue)
                    .build()
                userController.invoke(ViewEvent.CodeUpdateEvent, textValue)
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Transparent,
            )
        )
    }
}

@Composable
fun ResizeableDesc(initialWidth: Float = 200f, description : String) {
    var width by remember { mutableStateOf(initialWidth.dp) }
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .shadow(2.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // MutableInteractionSource to track changes of the component's interactions (like "hovered")
            val interactionSource = remember { MutableInteractionSource() }
            val isHovered by interactionSource.collectIsHoveredAsState()

            // text container
            SelectionContainer() {
                Text(
                    text = description,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .width(width - 10.dp)
                        .padding(6.dp)
                        .verticalScroll(rememberScrollState()),
                )
            }

            // resize bar
            Column(
                modifier = Modifier
                    .width(10.dp)
                    .fillMaxHeight()
                    .background(if (isHovered) Color.hsl(204F, 1F, 0.69F) else Color.Transparent)
                    .hoverable(interactionSource = interactionSource)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            width += dragAmount.x.toDp()
                            // TODO: (made) make this stuff constants, and figure out max screen size
                            if (width < 10.dp) {
                                width = 10.dp
                            } else if (width > 1000.dp) {
                                width = 1000.dp
                            }
                            change.consume()
                        }
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(20.dp)
                        .border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
                )
            }
        }

    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ResizeableTests(
    initialHeight: Float = 100f,
    databaseManager: DatabaseManager,
    userController: UserController,
    userViewModel: UserViewModel,
) {
    var height by remember { mutableStateOf(initialHeight.dp) }
//    var testResults = emptyList<Pair<String,String>>()
    var status by remember { mutableStateOf<Int>(0) } //todo: if doesn't work, add a state var
    LaunchedEffect(userViewModel.codeExecutionStatus) {
        println("launched effect")
    }

    Box(
        modifier = Modifier
            .height(height)
            .fillMaxWidth()
            .background(color = Color.LightGray.copy(alpha = 0.4f))
            .shadow(1.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // MutableInteractionSource to track changes of the component's interactions (like "hovered")
            val interactionSource = remember { MutableInteractionSource() }
            val isHovered by interactionSource.collectIsHoveredAsState()

            // resize bar
            Row(
                modifier = Modifier
                    .height(10.dp)
                    .fillMaxWidth()
                    .background(if (isHovered) Color.hsl(204F, 1F, 0.69F) else Color.Transparent)
                    .hoverable(interactionSource = interactionSource)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            height -= dragAmount.y.toDp()
                            // TODO: (made) make this stuff constants, and figure out max screen size
                            if (height < 10.dp) {
                                height = 10.dp
                            } else if (height > 500.dp) {
                                height = 500.dp
                            }
                            change.consume()
                        }
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .width(20.dp)
                        .border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
                )
            }

            // actual content
            Column {

                var testScreen by remember { mutableStateOf(0) }
                var testCases = userViewModel.testCases.value.toMutableList()
//                var testResults = remember { mutableStateListOf<String>()}
                var displayCase by remember { mutableStateOf(0) }

                Row(
                    modifier = Modifier
                ) {
                    Spacer(Modifier.width(3.dp))
                    primarySelectableButton("Test Cases", testScreen == 0,) { testScreen = 0 }
                    Spacer(Modifier.width(3.dp))
                    primarySelectableButton("Test Result", testScreen == 1, ) { testScreen = 1 }
                }
                // test case screen
                if (testScreen == 0) {
                    val scrollState = rememberLazyListState()
                    val coroutineScope = rememberCoroutineScope()

                    LazyRow(
                        state = scrollState,
                        modifier = Modifier
                            .draggable(
                                orientation = Orientation.Horizontal,
                                state = rememberDraggableState { delta ->
                                    coroutineScope.launch {
                                        scrollState.scrollBy(-delta)
                                    }
                                },
                            )
                    ) {
                        for ((idx, case) in testCases.withIndex()) {
                            item {
                                Spacer(Modifier.width(3.dp))
                                Card(
                                    backgroundColor = if (displayCase == idx) MaterialTheme.colors.secondary else MaterialTheme.colors.secondaryVariant,
                                    onClick = {
                                        println("change to test case $idx")
                                        displayCase = idx
                                    }
                                ) {
                                    Text(
                                        text = "Case $idx",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        color = Color.Black,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        item {
                            TextButton(onClick = {
                                println("adding test case")
                                userController.invokeTestAdd(testCases.last())
                            }
                            ) {
                                Text("+", color = MaterialTheme.colors.secondary)
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(3.dp))
                        TextField(
                            value = testCases[displayCase].input,
                            onValueChange = { userController.invokeTestUpdate(displayCase, it) },
                        )
                        // going by Leetcode, this is fine as long as on refresh, it goes to default
                        if (testCases.size > 1) {
                            IconButton(
                                onClick = {
                                    val toRemove = displayCase
                                    if (displayCase == testCases.lastIndex) displayCase--
                                    userController.invokeTestDelete(toRemove)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Localized description"
                                )
                            }
                        }

                    }

                // results screen
                } else if (testScreen == 1) {
//                    if (testResults.isEmpty()) {
                      if (userViewModel.codeExecutionStatus == 0) {
                        Text(
                            text = "Please run your code by clicking the \"Run\" button located in the top right corner of your screen.",
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier
                                .height(height - 10.dp)
                                .padding(6.dp)
                                .verticalScroll(rememberScrollState()),
                        )
                      } else {
                          val testResults = userViewModel.testCases.value.map { testCase ->
                              Pair(testCase.userOutput, testCase.expectedOutput)
                          }

                          println(testResults) // This will print the entire list of results to the console.
                          if (testResults[0].first == "Error") {
                              Text(
                                  text = "Code execution was unsuccessful. The code may be unrecognizable, or the API quota may have been exceeded.",
                                  style = MaterialTheme.typography.body1
                              )
                          }
                          // Now let's display the results in the UI.
                          Column(
                              modifier = Modifier
                                  .height(height - 10.dp)
                                  .verticalScroll(rememberScrollState())
                          ) {
                              val scrollState = rememberLazyListState()
                              val coroutineScope = rememberCoroutineScope()

                              LazyRow(
                                  state = scrollState,
                                  modifier = Modifier
                                      .draggable(
                                          orientation = Orientation.Horizontal,
                                          state = rememberDraggableState { delta ->
                                              coroutineScope.launch {
                                                  scrollState.scrollBy(-delta)
                                              }
                                          },
                                      )
                              ) {
                                  testResults.forEachIndexed { idx, (userOutput, expectedOutput) ->
                                      item {
                                          Spacer(Modifier.width(3.dp))
                                          Card(
                                              backgroundColor = if (displayCase == idx) MaterialTheme.colors.secondary else MaterialTheme.colors.secondaryVariant,
                                              onClick = {
                                                  println("change to test case $idx")
                                                  displayCase = idx
                                              }
                                          ) {
                                              Text(
                                                  text = "Case $idx",
                                                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                  color = Color.Black,
                                                  textAlign = TextAlign.Center
                                              )
                                          }

                                      }
                                  }
                              }
                              Text(
                                  text = "Expected: ${testResults[displayCase].second}\nActual: ${testResults[displayCase].first}",
                                  style = MaterialTheme.typography.body1
                              )

                              if (testResults.all { (userOutput, expectedOutput) -> userOutput == expectedOutput
                                          && userOutput != "Error" && expectedOutput != "Error" }) {
                                  Text(
                                      text = "All test cases passed! You SQUASHED this Problem!\nYour score is updated. ",
                                      style = MaterialTheme.typography.body1
                                  )
                              } else {
                                  Text(
                                      text = "Some test cases failed.",
                                      style = MaterialTheme.typography.body1
                                  )
                              }

                              if (checkIfCompleted(testResults)) {
                                  val userMetrics = userViewModel.currentUser.value?.let { databaseManager.getUserMetrics(it.userName) }
                                  val selectedProblem = userViewModel.selectedProblem
                                  val difficultyTag = selectedProblem?.tags?.let { findProblemDifficulty(it) }

                                  if (userMetrics != null && selectedProblem != null && difficultyTag != null) {
                                      val newUserMetrics : UserMetrics
                                      when (difficultyTag) {
                                          Difficulty.EASY -> {
                                              newUserMetrics = userMetrics.copy(totalCompleted = userMetrics.totalCompleted + 1, easyCompleted = userMetrics.easyCompleted + 1)
                                              databaseManager.updateUserMetrics(newUserMetrics)
                                          }
                                          Difficulty.MEDIUM -> {
                                              newUserMetrics = userMetrics.copy(totalCompleted = userMetrics.totalCompleted + 1, mediumCompleted = userMetrics.mediumCompleted + 1)
                                              databaseManager.updateUserMetrics(newUserMetrics)
                                          }
                                          Difficulty.HARD -> {
                                              newUserMetrics = userMetrics.copy(totalCompleted = userMetrics.totalCompleted + 1, hardCompleted = userMetrics.hardCompleted + 1)
                                              databaseManager.updateUserMetrics(newUserMetrics)
                                          }
                                          else -> {
                                              println("Unknown problem type.")
                                          }
                                      }
                                  } else {
                                      println("User metrics, selected problem, or difficulty tag is null.")
                                  }
                              }
                          }
                      }
                }
                }
            }
        }
    }

fun checkIfCompleted(testResults: List<Pair<String, String>>) : Boolean {
    testResults.forEach { (userOutput, expectedOutput) ->
        if (userOutput == "Error" || userOutput != expectedOutput) {
            return false
        }
    }
    return true
}

fun findProblemDifficulty(tags: List<String>) : Difficulty {
    tags.forEach {
        if (it == "Easy") {
            return Difficulty.EASY
        }
        if (it == "Medium") {
            return Difficulty.MEDIUM
        }
        if (it == "Hard") {
            return Difficulty.HARD
        }
    }
    return Difficulty.ERROR
}

enum class Difficulty {
    ERROR, EASY, MEDIUM, HARD
}