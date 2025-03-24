package com.pjaliom

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent

@Composable
fun App() {
    val game = remember { Game() }

    GameScreen(
        modifier = Modifier
            .onKeyEvent(game::onKeyEvent)
            .fillMaxSize(),
        game = game
    )
}
