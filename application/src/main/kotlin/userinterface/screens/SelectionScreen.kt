package org.example.userinterface.views

import models.DatabaseManager
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import userinterface.Screen
import userinterface.components.NavController
import models.Problem
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import controller.UserController
import kotlinx.coroutines.launch
import userinterface.ViewEvent
import userinterface.components.primarySelectableButton

@Composable
fun SelectionScreen(
    navController: NavController,
    databaseManager: DatabaseManager,
    userController: UserController,
    modifier: Modifier = Modifier
) {
    val tags = listOf("C", "Kotlin", "Java", "Easy", "Medium", "Hard")

    var searchText by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<String?>(null) }

    val problems = databaseManager.getProblems()
    val coroutineScope = rememberCoroutineScope()

    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.border(width = 3.dp, color = Color.LightGray.copy(0.3f)))
    {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TextField(
                    value = searchText,
                    onValueChange = { it: String -> searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(4.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colors.onPrimary)
                        .padding(16.dp),
                    textStyle = MaterialTheme.typography.body1,
                    singleLine = true,
                    maxLines = 1,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                )

                TagButtons(tags = tags, selectedTag = selectedTag) { newTag ->
                    selectedTag = newTag
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    problems.filter { it.title.contains(searchText, ignoreCase = true) && (selectedTag == null || it.tags.contains(selectedTag)) }
                        .forEach { problem ->
                            ProblemItem(problem = problem, onClick = {
                                userController.invoke(ViewEvent.SelectProblemEvent, problem)
                                val tests = databaseManager.getTestCasesByProblemId(problem.id)
                                if (tests.isNotEmpty()) {
                                    userController.invoke(
                                        ViewEvent.SelectTestCases,
                                        databaseManager.getTestCasesByProblemId(problem.id)
                                    )
                                }
                                navController.navigate(Screen.ProblemScreen.name)
                            })
                        }
                }
            }
        }
    }
}

@Composable
fun ProblemItem(problem: Problem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = problem.title, style = MaterialTheme.typography.h6)
            Text(text = "Tags: ${problem.tags.joinToString(", ")}", style = MaterialTheme.typography.body2)
        }
    }
}

@Composable
fun TagButtons(tags: List<String>, selectedTag: String?, onTagSelected: (String?) -> Unit) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            primarySelectableButton(name = "All", isSelected = selectedTag == null) {
                onTagSelected(null)
            }
        }

        tags.forEach { tag ->
            item {
                primarySelectableButton(name = tag, isSelected = selectedTag == tag) {
                    onTagSelected(tag)
                }
            }
        }
    }
}
