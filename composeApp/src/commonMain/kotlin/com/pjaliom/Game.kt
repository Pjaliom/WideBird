package com.pjaliom

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.*
import kotlinx.datetime.Clock
import kotlin.math.max
import kotlin.random.Random

class Game(
    val groundRHeight: Float = 0.1f,
    private val birdInitROffset: Offset = Offset(0.1f, 0.5f),
    private val birdRSize: Size = Size(0.05f, 0.05f),
    private val blockPairNumber: Int = 5,
    private val blockRWidth: Float = 0.065f,
    private val blockRSpacing: Float = 0.25f,
    private val blockRGap: Float = 0.3f,
    private val blockCreationRSpacing: Float = 0.8f,
    private val moveSpeedRXOffset: Float = 0.002f,
    private val moveSpeedRYOffset: Float = 0.01f,
    private val moveSpeedJumpRYOffset: Float = 0.001f,
    private val jumpRYOffset: Float = 0.02f
){

    data class Block(
        val rect: Rect,
    )

    data class PlayState(
        val rect: Rect,
        val score: Int = 0,
        val flaying: Boolean = false,
        val blocks: List<Block> = emptyList()
    )

    sealed interface State {

        val playState: PlayState

        data class Waiting(override val playState: PlayState) : State

        data class Playing(override val playState: PlayState) : State

        data class End(
            override val playState: PlayState,
            val score: Int,
            val maxScore: Int
        ) : State
    }

    var state : State by mutableStateOf(waitingState())
        private set


    private var endTime = 0L

    private var maxScore = 0

    private var jumpOffset = 0f

    private fun defaultRect() = Rect(birdInitROffset, birdRSize)

    private fun waitingState() = State.Waiting(PlayState(defaultRect()))

    private fun createBlocks(
        xOffset: Float = blockCreationRSpacing,
        n: Int = blockPairNumber,
        gap: Float = blockRGap,
        spacing: Float = blockRSpacing,
        width: Float = blockRWidth
    ): List<Block> =
        arrayListOf<Block>().apply {

            repeat(n) { i ->

                val randomHeight = (0.2f + Random.nextFloat() * (0.3f))

                val xOff = xOffset + (i * spacing)

                add(
                    Block(
                        rect = Rect(
                            offset = Offset(x = xOff, y = (randomHeight + gap)),
                            size = Size(width = width, height = 1 - (randomHeight + gap))
                        )
                    )
                )

                add(
                    Block(
                        rect = Rect(Offset(xOff, 0f), Size(width, randomHeight)),
                    )
                )
            }
        }



    fun startGame() {
        jumpOffset = 0f
        state = State.Playing(
            playState = PlayState(
                blocks = createBlocks(),
                rect = defaultRect()
            )
        )
    }

    fun updateState() {
        val playState = state.playState

        val score = playState.blocks.count {
            it.rect.topLeft.x < playState.rect.topLeft.x
        } / 2


        if (playState.rect.topLeft.y < groundRHeight) {
            endGame(score); return
        }

        if (playState.blocks.any { block -> block.rect.overlaps(playState.rect) }) {
            endGame(score); return
        }

        val blocks = if (playState.blocks.size < playState.rect.topLeft.x * blockPairNumber*2)
            createBlocks(xOffset = playState.rect.topLeft.x + blockCreationRSpacing)
        else emptyList()


        state = State.Playing(
            playState.copy(
                rect = Rect(
                    offset = playState.rect.topLeft.run {
                        copy(
                            x = x + moveSpeedRXOffset,
                            y = (y + jumpOffset - moveSpeedRYOffset).coerceIn(0f..1f)
                        )
                    },
                    size = playState.rect.size
                ),
                blocks = playState.blocks + blocks,
                flaying = jumpOffset > 0,
                score = score
            )
        )

        jumpOffset = (jumpOffset - moveSpeedJumpRYOffset).coerceIn(0f..1f)

    }

    private fun endGame(score: Int) {
        endTime = Clock.System.now().toEpochMilliseconds()
        maxScore = max(score, maxScore)
        state = State.End(
            playState = state.playState,
            score = score,
            maxScore = maxScore
        )
    }

    fun jump() {
        if(state is State.Playing){
            jumpOffset += jumpRYOffset
        }
    }

    fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.type == KeyEventType.KeyUp) {
            if (event.key == Key.Spacebar) {
                if (state is State.Playing) {
                    jump()
                    return true
                }else if(Clock.System.now().toEpochMilliseconds()-endTime > 1000){
                    startGame()
                    return true
                }
            }
        }
        return false
    }

}






