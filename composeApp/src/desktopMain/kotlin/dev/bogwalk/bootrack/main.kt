package dev.bogwalk.bootrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material.icons.sharp.Minimize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.*
import bootrack.composeapp.generated.resources.Res
import bootrack.composeapp.generated.resources.app_logo
import dev.bogwalk.bootrack.client.AppClient
import dev.bogwalk.bootrack.client.MainViewModel
import dev.bogwalk.bootrack.client.storage.AppCache
import dev.bogwalk.bootrack.components.buttons.ElevatedIconButton
import dev.bogwalk.bootrack.style.*
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension

fun main() = application {
    val scope = rememberCoroutineScope()
    val client by remember { mutableStateOf(AppClient()) }
    val cache by remember { mutableStateOf(AppCache()) }
    val api by remember { mutableStateOf(MainViewModel(client, cache, scope)) }

    val state = rememberWindowState(
        position = WindowPosition(Alignment.Center),
        width = windowWidth,
        height = windowHeight
    )

    LaunchedEffect("initial load") {
        api.loadInitialState()
    }

    Window(
        onCloseRequest = {},
        state = state,
        icon = painterResource(Res.drawable.app_logo),
        undecorated = true,
        transparent = true,
        // https://youtrack.jetbrains.com/issue/CMP-6031
        resizable = true,
    ) {
        setMinimumWindowSize()

        BTTheme {
            WindowDraggableArea {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingMedium + borderSmall, end = paddingLarge + paddingSmall + borderSmall)
                        .background(MaterialTheme.colorScheme.background)
                    ,
                    contentAlignment = Alignment.Center,
                ) {
                    App(api, cache)

                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = paddingLarge + paddingSmall, y = -paddingMedium)
                    ) {
                        ElevatedIconButton(
                            onClick = {
                                api.cleanUp()
                                exitApplication()
                            },
                            icon = Icons.Sharp.Close,
                        )
                        ElevatedIconButton(
                            onClick = { state.isMinimized = true },
                            icon = Icons.Sharp.Minimize,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FrameWindowScope.setMinimumWindowSize() {
    val scaledWidth = minWindowWidth / 1.2f
    val scaledHeight = minWindowHeight / 1.2f
    // https://youtrack.jetbrains.com/issue/CMP-2285
    with(LocalDensity.current) {
        LaunchedEffect(this) {
            window.minimumSize = Dimension(scaledWidth.toPx().toInt(), scaledHeight.toPx().toInt())
        }
    }
}
