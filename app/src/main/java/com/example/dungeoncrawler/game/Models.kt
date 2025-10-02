package com.example.dungeoncrawler.game

data class Position(val x: Int, val y: Int)

enum class TileType { WALL, FLOOR }

data class Tile(val position: Position, val type: TileType)

data class Entity(
    val id: Int,
    val name: String,
    val position: Position,
    val isPlayer: Boolean = false,
)

data class GameState(
    val tiles: List<List<Tile>>,
    val entities: List<Entity>,
    val playerId: Int
) {
    val player: Entity get() = entities.first { it.id == playerId }
}