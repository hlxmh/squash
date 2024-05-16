package userinterface.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import models.MessageData
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.key.*

/**
 * We can store potentially re-usable Composables here
 */

/**
 * NavController Class
 * all related items courtesy of https://github.com/itheamc/navigation-for-compose-for-desktop
 * TODO: (made) replace with voyager for multiplatform support
 */

class NavController(
    private val startDestination: String,
    private var backStackScreens: MutableSet<String> = mutableSetOf(),
) {
    // Variable to check if Nav Bar is enabled
    private val _navigationEnabled = mutableStateOf(false)
    val navigationEnabled: State<Boolean> = _navigationEnabled

    // Variable to store the state of the current screen
    var currentScreen: MutableState<String> = mutableStateOf(startDestination)

    // Function to handle the navigation between the screen
    fun navigate(route: String) {
        if (route != currentScreen.value) {
            if (backStackScreens.contains(currentScreen.value) && currentScreen.value != startDestination) {
                backStackScreens.remove(currentScreen.value)
            }

            if (route == startDestination) {
                backStackScreens = mutableSetOf()
            } else {
                backStackScreens.add(currentScreen.value)
            }

            currentScreen.value = route
        }
    }

    // Function to handle the back
    fun navigateBack() {
        if (backStackScreens.isNotEmpty()) {
            currentScreen.value = backStackScreens.last()
            backStackScreens.remove(currentScreen.value)
        }
    }

    // Getters and setters for _navigationEnabled
    fun getNavigationEnabled(): Boolean {
        return _navigationEnabled.value
    }
    fun setNavigationEnabled(enabled: Boolean) {
        _navigationEnabled.value = enabled
    }
}


/**
 * Composable to remember the state of the navcontroller
 */
@Composable
fun rememberNavController(
    startDestination: String,
    backStackScreens: MutableSet<String> = mutableSetOf(),
): MutableState<NavController> = rememberSaveable {
    mutableStateOf(NavController(startDestination, backStackScreens))
}

/**
 * NavigationHost class
 */
class NavigationHost(
    val navController: NavController,
    val contents: @Composable NavigationGraphBuilder.() -> Unit
) {

    @Composable
    fun build() {
        NavigationGraphBuilder().renderContents()
    }

    inner class NavigationGraphBuilder(
        val navController: NavController = this@NavigationHost.navController
    ) {
        @Composable
        fun renderContents() {
            this@NavigationHost.contents(this)
        }
    }
}


/**
 * Composable to build the Navigation Host
 */
@Composable
fun NavigationHost.NavigationGraphBuilder.composable(
    route: String,
    content: @Composable () -> Unit
) {
    if (navController.currentScreen.value == route) {
        content()
    }

}


@Composable
fun ResponseDialog(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    title: String,
    text: String
) {
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                onDecline()
                showDialog = false
            },
            title = {
                Text(text = title)
            },
            text = {
                Text(text)
            },
            buttons = {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            onAccept()
                            showDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Green
                        )
                    ) {
                        Text("Accept")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onDecline()
                            showDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Red
                        )
                    ) {
                        Text("Decline")
                    }
                }
            }
        )
    }
}

// Utility function for tabbing
fun handleTabKeyEvent(enterKeyAction: () -> Unit,focusManager: FocusManager, isLastTextField: Boolean = false, ): (KeyEvent) -> Boolean {
    return { keyEvent: KeyEvent ->
        if (keyEvent.key == Key.Tab && keyEvent.type == KeyEventType.KeyDown) {
            if (!isLastTextField) {
                focusManager.moveFocus(FocusDirection.Down)
            } else {
                focusManager.clearFocus()
                focusManager.moveFocus(FocusDirection.Down)
            }
            true // Event handled, stop it from propagating
        } else if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyDown) {
            enterKeyAction()
            true
        } else {
            false // Not our event, let it propagate
        }
    }
}
