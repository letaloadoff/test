package com.example.dungeoncrawler.game

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {
    private val mapWidth = 48
    private val mapHeight = 28

    private val engine = GameEngine()

    private val _state = MutableStateFlow(engine.newGame(mapWidth, mapHeight))
    val state: StateFlow<GameState> = _state

    fun newGame() {
        _state.value = engine.newGame(mapWidth, mapHeight)
    }

    fun move(dx: Int, dy: Int) {
        _state.update { current -> engine.playerMove(current, dx, dy) }
    }

    fun waitTurn() {
        _state.update { current -> engine.playerWait(current) }
    }
}