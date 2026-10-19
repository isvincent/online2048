package com.example.engine

import com.example.model.BoardSize
import com.example.model.Direction
import com.example.model.MoveResult
import com.example.model.Tile
import kotlin.random.Random

data class BoardSnapshot(
    val tiles: List<Tile>,
    val score: Int,
    val combo: Int,
    val isGameOver: Boolean,
    val hasWon: Boolean
)

class GameEngine2048(
    var boardSize: BoardSize = BoardSize.SIZE_4X4,
    private val random: Random = Random.Default
) {
    private var nextTileId = 1L
    val size: Int get() = boardSize.size

    var tiles: List<Tile> = emptyList()
        private set
    var score: Int = 0
        private set
    var combo: Int = 0
        private set
    var isGameOver: Boolean = false
        private set
    var hasWon: Boolean = false
        private set
    var continueAfterWin: Boolean = false

    private val history = mutableListOf<BoardSnapshot>()

    init {
        resetGame()
    }

    fun resetGame(seededRandom: Random? = null) {
        val rng = seededRandom ?: random
        tiles = emptyList()
        score = 0
        combo = 0
        isGameOver = false
        hasWon = false
        continueAfterWin = false
        history.clear()

        // Spawn initial two tiles
        spawnTile(rng)
        spawnTile(rng)
    }

    fun spawnTile(rng: Random = random): Boolean {
        val occupied = tiles.map { it.row to it.col }.toSet()
        val emptyPositions = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (!occupied.contains(r to c)) {
                    emptyPositions.add(r to c)
                }
            }
        }

        if (emptyPositions.isEmpty()) return false

        val (r, c) = emptyPositions[rng.nextInt(emptyPositions.size)]
        val value = if (rng.nextFloat() < 0.9f) 2 else 4
        val newTile = Tile(
            id = nextTileId++,
            value = value,
            row = r,
            col = c,
            isNew = true
        )
        tiles = tiles + newTile
        checkGameOver()
        return true
    }

    fun addObstacle(rng: Random = random): Boolean {
        val occupied = tiles.map { it.row to it.col }.toSet()
        val emptyPositions = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (!occupied.contains(r to c)) {
                    emptyPositions.add(r to c)
                }
            }
        }
        if (emptyPositions.isEmpty()) return false
        val (r, c) = emptyPositions[rng.nextInt(emptyPositions.size)]
        val obstacleTile = Tile(
            id = nextTileId++,
            value = 0,
            row = r,
            col = c,
            isObstacle = true
        )
        tiles = tiles + obstacleTile
        checkGameOver()
        return true
    }

    fun clearObstacles(): Int {
        val count = tiles.count { it.isObstacle }
        if (count > 0) {
            tiles = tiles.filterNot { it.isObstacle }
        }
        return count
    }

    fun removeLowestTile(): Boolean {
        val regularTiles = tiles.filterNot { it.isObstacle }
        if (regularTiles.isEmpty()) return false
        val lowest = regularTiles.minByOrNull { it.value } ?: return false
        tiles = tiles.filterNot { it.id == lowest.id }
        return true
    }

    fun move(direction: Direction, rng: Random = random): MoveResult {
        if (isGameOver) return MoveResult(false, 0, emptyList(), getLargestTile())

        // Save snapshot for undo
        saveSnapshot()

        // Create 2D grid representation
        val grid = Array(size) { Array<Tile?>(size) { null } }
        for (tile in tiles) {
            if (tile.row in 0 until size && tile.col in 0 until size) {
                grid[tile.row][tile.col] = tile.copy(isMerged = false, isNew = false)
            }
        }

        var scoreGained = 0
        val mergedList = mutableListOf<Int>()
        val newTiles = mutableListOf<Tile>()

        when (direction) {
            Direction.LEFT -> {
                for (r in 0 until size) {
                    val line = (0 until size).mapNotNull { grid[r][it] }
                    val (mergedLine, lineScore, lineMerged) = processLine(line)
                    scoreGained += lineScore
                    mergedList.addAll(lineMerged)
                    for (c in mergedLine.indices) {
                        newTiles.add(mergedLine[c].copy(row = r, col = c))
                    }
                }
            }
            Direction.RIGHT -> {
                for (r in 0 until size) {
                    val line = (size - 1 downTo 0).mapNotNull { grid[r][it] }
                    val (mergedLine, lineScore, lineMerged) = processLine(line)
                    scoreGained += lineScore
                    mergedList.addAll(lineMerged)
                    for (i in mergedLine.indices) {
                        val c = size - 1 - i
                        newTiles.add(mergedLine[i].copy(row = r, col = c))
                    }
                }
            }
            Direction.UP -> {
                for (c in 0 until size) {
                    val line = (0 until size).mapNotNull { grid[it][c] }
                    val (mergedLine, lineScore, lineMerged) = processLine(line)
                    scoreGained += lineScore
                    mergedList.addAll(lineMerged)
                    for (r in mergedLine.indices) {
                        newTiles.add(mergedLine[r].copy(row = r, col = c))
                    }
                }
            }
            Direction.DOWN -> {
                for (c in 0 until size) {
                    val line = (size - 1 downTo 0).mapNotNull { grid[it][c] }
                    val (mergedLine, lineScore, lineMerged) = processLine(line)
                    scoreGained += lineScore
                    mergedList.addAll(lineMerged)
                    for (i in mergedLine.indices) {
                        val r = size - 1 - i
                        newTiles.add(mergedLine[i].copy(row = r, col = c))
                    }
                }
            }
        }

        // Compare old board and new board to determine if any tile moved or merged
        val oldMap = tiles.associate { (it.row to it.col) to (it.value to it.isObstacle) }
        val newMap = newTiles.associate { (it.row to it.col) to (it.value to it.isObstacle) }
        val moved = oldMap != newMap

        if (moved) {
            tiles = newTiles
            score += scoreGained
            if (mergedList.isNotEmpty()) {
                combo++
                // Combo bonus points
                score += (combo * 2)
            } else {
                combo = 0
            }

            // Check victory condition
            if (!hasWon && !continueAfterWin && tiles.any { it.value >= 2048 }) {
                hasWon = true
            }

            spawnTile(rng)
            checkGameOver()
        } else {
            // Revert snapshot if nothing moved
            history.removeLastOrNull()
        }

        return MoveResult(
            moved = moved,
            scoreEarned = scoreGained,
            mergedValues = mergedList,
            largestTile = getLargestTile()
        )
    }

    private data class ProcessLineResult(
        val tiles: List<Tile>,
        val scoreEarned: Int,
        val mergedValues: List<Int>
    )

    private fun processLine(line: List<Tile>): ProcessLineResult {
        if (line.isEmpty()) return ProcessLineResult(emptyList(), 0, emptyList())

        val result = mutableListOf<Tile>()
        var scoreEarned = 0
        val mergedValues = mutableListOf<Int>()

        var i = 0
        while (i < line.size) {
            val current = line[i]
            if (current.isObstacle) {
                result.add(current)
                i++
                continue
            }

            if (i + 1 < line.size && !line[i + 1].isObstacle && line[i + 1].value == current.value) {
                // Merge two tiles
                val mergedVal = current.value * 2
                scoreEarned += mergedVal
                mergedValues.add(mergedVal)
                result.add(
                    current.copy(
                        id = nextTileId++,
                        value = mergedVal,
                        isMerged = true
                    )
                )
                i += 2
            } else {
                result.add(current)
                i++
            }
        }

        return ProcessLineResult(result, scoreEarned, mergedValues)
    }

    private fun checkGameOver() {
        val occupied = tiles.map { it.row to it.col }.toSet()
        // If there is any empty cell, game is not over
        if (occupied.size < size * size) {
            isGameOver = false
            return
        }

        // Board is full: check adjacent cells for possible merges
        val grid = Array(size) { IntArray(size) { 0 } }
        for (tile in tiles) {
            grid[tile.row][tile.col] = if (tile.isObstacle) -1 else tile.value
        }

        for (r in 0 until size) {
            for (c in 0 until size) {
                val current = grid[r][c]
                if (current == -1) continue // obstacle cannot merge

                // Check right neighbor
                if (c + 1 < size && grid[r][c + 1] == current) {
                    isGameOver = false
                    return
                }
                // Check down neighbor
                if (r + 1 < size && grid[r + 1][c] == current) {
                    isGameOver = false
                    return
                }
            }
        }

        isGameOver = true
    }

    fun canUndo(): Boolean = history.isNotEmpty()

    fun undo(): Boolean {
        val last = history.removeLastOrNull() ?: return false
        tiles = last.tiles
        score = last.score
        combo = last.combo
        isGameOver = last.isGameOver
        hasWon = last.hasWon
        return true
    }

    private fun saveSnapshot() {
        if (history.size >= 10) {
            history.removeAt(0)
        }
        history.add(
            BoardSnapshot(
                tiles = tiles.map { it.copy() },
                score = score,
                combo = combo,
                isGameOver = isGameOver,
                hasWon = hasWon
            )
        )
    }

    fun getLargestTile(): Int {
        return tiles.filterNot { it.isObstacle }.maxOfOrNull { it.value } ?: 0
    }

    fun getBoardMatrix(): Array<IntArray> {
        val matrix = Array(size) { IntArray(size) { 0 } }
        for (tile in tiles) {
            if (tile.row in 0 until size && tile.col in 0 until size) {
                matrix[tile.row][tile.col] = if (tile.isObstacle) -1 else tile.value
            }
        }
        return matrix
    }
}
