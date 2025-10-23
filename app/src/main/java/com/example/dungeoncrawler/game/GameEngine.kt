package com.example.dungeoncrawler.game

import kotlin.math.abs
import kotlin.random.Random

class GameEngine {
    private val random = Random(System.currentTimeMillis())

    fun newGame(width: Int, height: Int): GameState {
        val tiles = generateDungeon(width, height)
        var nextId = 1
        val player = Entity(id = nextId++, name = "Hero", position = findOpenTile(tiles), isPlayer = true)

        val enemies = buildList {
            repeat(8) {
                add(Entity(id = nextId++, name = "Slime", position = findOpenTile(tiles)))
            }
        }

        return GameState(
            tiles = tiles,
            entities = listOf(player) + enemies,
            playerId = player.id
        )
    }

    fun playerMove(state: GameState, dx: Int, dy: Int): GameState {
        val player = state.player
        val target = Position(player.position.x + dx, player.position.y + dy)
        if (!isWalkable(state.tiles, target)) return state
        val occupied = state.entities.any { it.position == target && it.id != player.id }
        val movedState = if (occupied) state else state.copy(
            entities = state.entities.map { entity ->
                if (entity.id == player.id) entity.copy(position = target) else entity
            }
        )
        return enemiesAct(movedState)
    }

    fun playerWait(state: GameState): GameState = enemiesAct(state)

    private fun enemiesAct(state: GameState): GameState {
        val player = state.player
        val updated = state.entities.map { entity ->
            if (entity.isPlayer) return@map entity
            val step = chaseStep(entity.position, player.position, state.tiles)
            val nextPos = Position(entity.position.x + step.first, entity.position.y + step.second)
            if (nextPos == player.position) entity // touch = no damage for now
            else if (isWalkable(state.tiles, nextPos) && state.entities.none { it.position == nextPos })
                entity.copy(position = nextPos)
            else entity
        }
        return state.copy(entities = updated)
    }

    private fun chaseStep(from: Position, to: Position, tiles: List<List<Tile>>): Pair<Int, Int> {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val stepX = dx.sign()
        val stepY = dy.sign()
        // Try axis with greater distance first
        if (abs(dx) >= abs(dy)) {
            if (isWalkable(tiles, Position(from.x + stepX, from.y))) return stepX to 0
            if (isWalkable(tiles, Position(from.x, from.y + stepY))) return 0 to stepY
        } else {
            if (isWalkable(tiles, Position(from.x, from.y + stepY))) return 0 to stepY
            if (isWalkable(tiles, Position(from.x + stepX, from.y))) return stepX to 0
        }
        return 0 to 0
    }

    private fun Int.sign(): Int = when {
        this > 0 -> 1
        this < 0 -> -1
        else -> 0
    }

    private fun isWalkable(tiles: List<List<Tile>>, p: Position): Boolean {
        return p.y in tiles.indices && p.x in tiles[0].indices && tiles[p.y][p.x].type == TileType.FLOOR
    }

    private fun findOpenTile(tiles: List<List<Tile>>): Position {
        while (true) {
            val x = random.nextInt(1, tiles[0].size - 1)
            val y = random.nextInt(1, tiles.size - 1)
            if (tiles[y][x].type == TileType.FLOOR) return Position(x, y)
        }
    }

    private fun generateDungeon(width: Int, height: Int): List<List<Tile>> {
        val grid = Array(height) { y ->
            Array(width) { x -> Tile(Position(x, y), TileType.WALL) }
        }

        // Carve rooms
        val roomCount = 10
        val rooms = mutableListOf<Rect>()
        repeat(roomCount) {
            val w = random.nextInt(5, 10)
            val h = random.nextInt(4, 8)
            val x = random.nextInt(1, width - w - 1)
            val y = random.nextInt(1, height - h - 1)
            val rect = Rect(x, y, w, h)
            if (rooms.none { it.intersects(rect) }) {
                rooms.add(rect)
                carveRoom(grid, rect)
            }
        }

        // Connect rooms
        rooms.sortedBy { it.cx }.zipWithNext { a, b ->
            connectRooms(grid, a, b)
        }

        return grid.map { row -> row.map { it } }
    }

    private fun carveRoom(grid: Array<Array<Tile>>, r: Rect) {
        for (y in r.y until r.y + r.h) {
            for (x in r.x until r.x + r.w) {
                grid[y][x] = grid[y][x].copy(type = TileType.FLOOR)
            }
        }
    }

    private fun connectRooms(grid: Array<Array<Tile>>, a: Rect, b: Rect) {
        var x = a.cx
        var y = a.cy
        while (x != b.cx) {
            grid[y][x] = grid[y][x].copy(type = TileType.FLOOR)
            x += if (b.cx > x) 1 else -1
        }
        while (y != b.cy) {
            grid[y][x] = grid[y][x].copy(type = TileType.FLOOR)
            y += if (b.cy > y) 1 else -1
        }
    }

    private data class Rect(val x: Int, val y: Int, val w: Int, val h: Int) {
        val cx: Int get() = x + w / 2
        val cy: Int get() = y + h / 2
        fun intersects(other: Rect): Boolean =
            x < other.x + other.w && x + w > other.x && y < other.y + other.h && y + h > other.y
    }
}