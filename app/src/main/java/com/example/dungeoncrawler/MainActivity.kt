package com.example.dungeoncrawler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dungeoncrawler.game.Entity
import com.example.dungeoncrawler.game.GameViewModel
import com.example.dungeoncrawler.game.Tile
import com.example.dungeoncrawler.game.TileType
import com.example.dungeoncrawler.ui.theme.DungeonTheme

class MainActivity : ComponentActivity() {
    private val vm: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DungeonTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    GameScreen()
                }
            }
        }
    }

    @Composable
    private fun GameScreen(viewModel: GameViewModel = vm) {
        val state by viewModel.state.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "DungeonCrawler",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            DungeonGrid(
                tiles = state.tiles,
                entities = state.entities,
                cellSize = 16.dp
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Dpad(
                    onUp = { viewModel.move(0, -1) },
                    onDown = { viewModel.move(0, 1) },
                    onLeft = { viewModel.move(-1, 0) },
                    onRight = { viewModel.move(1, 0) },
                    onWait = { viewModel.waitTurn() }
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.newGame() }) { Text("New Game") }
            }
        }
    }
}

@Composable
private fun DungeonGrid(
    tiles: List<List<Tile>>,
    entities: List<Entity>,
    cellSize: androidx.compose.ui.unit.Dp
) {
    val entityByPos = entities.associateBy { it.position }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        tiles.forEach { row ->
            Row {
                row.forEach { tile ->
                    val entity = entityByPos[tile.position]
                    val bg = when (tile.type) {
                        TileType.WALL -> Color(0xFF202020)
                        TileType.FLOOR -> Color(0xFF0E0E0E)
                    }
                    val fg = when {
                        entity?.isPlayer == true -> Color(0xFF4CAF50)
                        entity != null -> Color(0xFFFF5722)
                        else -> bg
                    }
                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .background(bg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (entity != null) {
                            Box(
                                modifier = Modifier
                                    .size(cellSize * 0.6f)
                                    .background(fg)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Dpad(
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onWait: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = onUp, modifier = Modifier.width(120.dp)) { Text("Up") }
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.width(200.dp)) {
            Button(onClick = onLeft, modifier = Modifier.width(80.dp)) { Text("Left") }
            Button(onClick = onWait, modifier = Modifier.width(40.dp)) { Text(".") }
            Button(onClick = onRight, modifier = Modifier.width(80.dp)) { Text("Right") }
        }
        Spacer(Modifier.height(4.dp))
        Button(onClick = onDown, modifier = Modifier.width(120.dp)) { Text("Down") }
    }
}