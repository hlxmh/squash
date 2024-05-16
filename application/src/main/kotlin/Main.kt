import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import controller.UserController
import models.DatabaseManager
import models.UserModel
import userinterface.UserView
import userinterface.UserViewModel
import userinterface.theme.ProvideAppTheme
import java.awt.Dimension

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        state = WindowState(width = 1280.dp, height = 768.dp),
        title = "Squash v4.0.0",
        resizable = true
    ) {
        window.minimumSize = Dimension(1210, 730)
        ProvideAppTheme{
            App()
        }
    }
}

@Composable
fun App() {
    val databaseManager = DatabaseManager()
    val userModel = UserModel()
    val userViewModel = UserViewModel(userModel)
    val userController = UserController(userModel)
    MaterialTheme {
        UserView(userViewModel, userController, databaseManager)
    }
}