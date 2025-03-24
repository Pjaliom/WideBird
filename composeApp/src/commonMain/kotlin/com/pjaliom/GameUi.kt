package com.pjaliom

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.imageResource
import widebird.composeapp.generated.resources.Bird
import widebird.composeapp.generated.resources.Res


@Composable
fun GameScreen(modifier: Modifier, game: Game) {

    val state = game.state
    val playState = state.playState

    LaunchedEffect(state) {
        if (state is Game.State.Playing) {
            while (true) {
                game.updateState()
                delay(100)
            }
        }
    }

    Box(modifier){

        GameCanvas(
            Modifier.matchParentSize()
                .clickable(indication = null, interactionSource = null) {
                    if(state is Game.State.Playing){
                        game.jump()
                    }else {
                        game.startGame()
                    }
                },
            playState,
            game.groundRHeight
        )

        if(state is Game.State.Playing){
            Text(
                modifier = Modifier.padding(top = 5.dp).align(Alignment.TopCenter),
                text = playState.score.toString(),
                color = Color(96, 95, 95, 100),
                fontSize = 40.sp
            )
        }

        if(state is Game.State.End){
            Box(Modifier.matchParentSize().background(Color(0, 0, 0, 100)), contentAlignment = Alignment.Center){
                Column(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(250 ,233 , 178, 200)
                ).padding(15.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Score : ${playState.score}", color = Color(255, 93, 0), fontSize = 20.sp)
                    Text("Best Score : ${state.maxScore}", color = Color(253, 120, 47), fontSize = 20.sp)
                    Button(
                        modifier = Modifier.widthIn(min = 100.dp),
                        onClick = { game.startGame() },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(253, 174, 4),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Play")
                    }
                }
            }
        }
    }

}



@Composable
fun GameCanvas(modifier: Modifier, state: Game.PlayState, groundRHeight: Float) {

    val birdImage = imageResource(Res.drawable.Bird)
    val birdAngle by animateFloatAsState( if (state.flaying) 30f else 0f )
    val offsetX = state.rect.topLeft.x

    Canvas(modifier) {

        val factor = size.height

        fun Rect.reFactor() = run {
            copy(
                left = (left - offsetX) * factor,
                top = (1-bottom) * factor,
                right = (right - offsetX) * factor,
                bottom = (1-top) * factor
            )
        }

        drawBackground(groundRHeight)

        translate(left = 60f){

            state.blocks.forEach { block ->

                val blockRect = block.rect.reFactor()

                drawRect(
                    color = Color(208, 172, 0),
                    topLeft = blockRect.topLeft,
                    size = blockRect.size,
                    style = Fill
                )
            }

            val rect = state.rect.reFactor()

            println(rect.height)

            scale(rect.height/birdImage.height*1.35f, pivot = rect.topLeft){
                rotate(birdAngle, pivot = rect.topRight){
                    drawImage(
                        image = birdImage,
                        topLeft = rect.topLeft,
                    )
                }
            }

        }

    }
}

fun DrawScope.drawBackground(groundRHeight: Float) {
    // sky
    drawRect(
        color = Color(234, 229, 217),
        size = size.copy(height = size.height * (1-groundRHeight))
    )

    // ground
    drawRect(
        color = Color(82,51 ,37),
        topLeft = Offset(0f, size.height * (1-groundRHeight)),
        size = size.copy(height = size.height * groundRHeight)
    )

    // sun
    drawCircle(
        color = Color.Yellow,
        radius = 50f,
        center = Offset(size.width - 100f, 100f)
    )

}

