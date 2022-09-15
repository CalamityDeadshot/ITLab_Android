package ru.rtuitlab.itlab.presentation.ui.components

import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import ru.rtuitlab.itlab.presentation.screens.reports.duration
import ru.rtuitlab.itlab.presentation.ui.components.shared_elements.FadeMode
import ru.rtuitlab.itlab.presentation.ui.components.shared_elements.LocalSharedElementsRootScope
import ru.rtuitlab.itlab.presentation.ui.components.shared_elements.SharedElement
import ru.rtuitlab.itlab.presentation.ui.components.shared_elements.utils.ProgressThresholds
import ru.rtuitlab.itlab.presentation.ui.components.shared_elements.utils.SharedElementsTransitionSpec

@Composable
fun TransitionFloatingActionButton(
    key: Any,
    screenKey: Any,
    icon: ImageVector,
    contentDescription: String? = null,
    onClick: () -> Unit,
    transitionProgressSetter: (Float) -> Unit
) {
    val scope = LocalSharedElementsRootScope.current
    SharedElement(
        key = key,
        screenKey = screenKey,
        transitionSpec = SharedElementsTransitionSpec(
            durationMillis = duration,
            fadeMode = FadeMode.Through,
            fadeProgressThresholds = ProgressThresholds(.001f, .8f),
            scaleProgressThresholds = ProgressThresholds(.2f, 1f)
        ),
        onFractionChanged = transitionProgressSetter
    ) {
        FloatingActionButton(
            onClick = {
                if (scope?.isRunningTransition == true) return@FloatingActionButton
                onClick()
            },
            backgroundColor = MaterialTheme.colors.secondary,
            contentColor = MaterialTheme.colors.onSecondary
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription
            )
        }
    }
}