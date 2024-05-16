package userinterface.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun primaryButtonColors() = ButtonDefaults.buttonColors(
    backgroundColor = MaterialTheme.colors.primary,
    contentColor = MaterialTheme.colors.onPrimary
)
@Composable
fun primaryInvertButtonColors() = ButtonDefaults.buttonColors(
    backgroundColor = MaterialTheme.colors.primaryVariant,
    contentColor = MaterialTheme.colors.primary
)

@Composable
fun secondaryButtonColors() = ButtonDefaults.buttonColors(
    backgroundColor = MaterialTheme.colors.secondary,
    contentColor = MaterialTheme.colors.onSecondary
)
@Composable
fun secondaryInvertButtonColors() = ButtonDefaults.buttonColors(
    backgroundColor = MaterialTheme.colors.secondaryVariant,
    contentColor = MaterialTheme.colors.secondary
)

@Composable
fun clickButton(
    name: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    colors: ButtonColors = primaryButtonColors()) {
    Button(
        onClick = onClick,
        colors = colors,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        enabled = enabled,
    ) {
        Text(text = name)
    }
}

@Composable
fun primarySelectableButton(name: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = if (isSelected) primaryButtonColors() else primaryInvertButtonColors(),
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.elevation(4.dp),
    ) {
        Text(text = name)
    }
}
@Composable
fun secondarySelectableButton(name: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = if (isSelected) secondaryButtonColors() else secondaryInvertButtonColors(),
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.elevation(4.dp),
    ) {
        Text(text = name)
    }
}

@Composable
fun CountdownTimer(
    timeLeft: Int,
    updateTimeLeft: (Int) -> Unit
) {
    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000L) // Wait for 1 second
            updateTimeLeft(timeLeft - 1) // Decrement the counter
        }
    }
}