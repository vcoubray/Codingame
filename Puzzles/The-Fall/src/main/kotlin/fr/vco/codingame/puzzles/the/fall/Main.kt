package fr.vco.codingame.puzzles.the.fall

import java.util.*
import kotlin.system.measureTimeMillis

enum class Direction(val x: Int, val y: Int) {
    TOP(0, 1),
    RIGHT(-1, 0),
    LEFT(1, 0),
    NONE(0, 0)
}

data class Position(val index: Int, val dir: Direction)

enum class Rotation { RIGHT, LEFT }

sealed interface Action {
    object Wait : Action {
        override fun toString() = "WAIT"
    }

    class Rotate(val x: Int, val y: Int, val rotation: Rotation) : Action {
        override fun toString() = "$x $y $rotation"
    }
}

data class Entrance(
    val position: Position,
//    val defaultPath: List<Int>,
    val paths: List<Path>,
    val used: Boolean = false,
)

class Rock(
    val start: Position,
    val paths: List<Path>,
) {


//    fun getRockPaths(indyPath: Path) : List<RockPath> {
//        paths.mapNotNull { path ->
//            val offset = path.getCrossingOffset(indyPath)
//            if (offset < 0) return null
//            val realPaths = path.toRealPath(indyPath.path)
//            realPaths.forEach{it ->
//
//            }
//        }
//
//        return emptyList()
//    }

}

class RockPath(
    val offset: Int,
    val cellTypes: List<Int>,
    val path: List<Int>,
) {
    val size = path.size + offset
    fun getCellTypeAt(i: Int) = cellTypes.getOrNull(i) ?: -1
    fun getCellAt(i: Int) = path.getOrNull(i) ?: -1
}

class Board(
    private val width: Int,
    private val height: Int,
    private val exit: Int,
    val cellTypes: MutableList<Int>,
    val fixedMap: List<Boolean>,
    private val neighbors: Map<Direction, List<List<Pair<Direction, Int>>>>,
) {

    val entrances = initEntrances()

    private fun initEntrances() =
        cellTypes.mapIndexedNotNull { i, cellType ->
            val x = i % width
            val y = i / width
            when {
                y == 0 && CELL_TYPES[cellType].exitDirection(Direction.TOP) != Direction.NONE ->
                    Position(i, Direction.TOP)

                x == 0 && CELL_TYPES[cellType].exitDirection(Direction.LEFT) != Direction.NONE ->
                    Position(i, Direction.LEFT)

                x == width - 1 && CELL_TYPES[cellType].exitDirection(Direction.RIGHT) != Direction.NONE ->
                    Position(i, Direction.RIGHT)

                else -> null
            }
        }.map { pos ->
            Entrance(pos, findRockPaths(pos))
        }


    fun findIndyPaths(start: Position): List<Path> {

        val validPaths = mutableListOf<Path>()
        val toVisit = ArrayDeque<Path>().apply { add(Path(start.index, start.dir)) }

        while (toVisit.isNotEmpty()) {
            val current = toVisit.pop()

            if (current.currentCell == exit) {
                validPaths.add(current)
                continue
            }

            neighbors[current.currentDirection]!![current.currentCell]
                .filterNot { (dir, _) -> dir == Direction.NONE }
                .forEach { (dir, cell) ->
                    val path = current.path + cell
                    val directions = current.directions + dir
                    toVisit.add(Path(cell, dir, path, directions))
                }
        }
        return validPaths
    }

    fun update(action: Action) {
        if (action is Action.Rotate) {
            val type = cellTypes[action.y * width + action.x]
            cellTypes[action.y * width + action.x] = CELL_TYPES[type].rotate[action.rotation]!!
        }
    }

    fun findRockPaths(start: Position): List<Path> {
        val validPaths = mutableListOf<Path>()
        val toVisit = ArrayDeque<Path>().apply { add(Path(start.index, start.dir)) }

        while (toVisit.isNotEmpty()) {
            val current = toVisit.pop()

            if (current.currentDirection == Direction.NONE) {
                if (current.path.size > 1) validPaths.add(current)
                continue
            }

            neighbors[current.currentDirection]!![current.currentCell]
                .forEach { (dir, cell) ->
                    val path = current.path + cell
                    val directions = current.directions + dir
                    toVisit.add(Path(cell, dir, path, directions))
                }
        }
        return validPaths
    }

    fun isValidRealPathCombination(indyPath: RealPath, rockPaths: List<RealPath>): Boolean {
        val workingCellTypes = this.cellTypes.toMutableList()
        var rockCount = rockPaths.size
        val rocks = rockPaths.toTypedArray()

        var actionCount = 0
        for (i in 0 until indyPath.cellTypes.size) {
            val cellIndex = indyPath.path[i]
            val originType = workingCellTypes[cellIndex]
            val targetType = indyPath.cellTypes[i]

            if (targetType != originType) {
                actionCount += getRotations(originType, targetType).size
                workingCellTypes[cellIndex] = targetType
            }

            for (j in 0 until rockCount) {
                val rockCellIndex = rocks[j].path[i]

                val originRockType = workingCellTypes[rockCellIndex]
                val targetRockType = rocks[j].cellTypes[i]
                if (cellIndex == rockCellIndex && targetType != targetRockType) return false
                if (i > 0) {
                    val cellIndexPrev = indyPath.path[i - 1]
                    val rockCellIndexPrev = rocks[j].path[i - 1]
                    if (cellIndex == rockCellIndexPrev && cellIndexPrev == rockCellIndex) return false
                }
                if (originRockType != targetRockType) {
                    actionCount = getRotations(originRockType, targetRockType).size
                    workingCellTypes[rockCellIndex] = targetRockType
                }
            }

            // remove rocks that crash into another one
            for (j in rockCount - 1 downTo 0) {
                if (j >= 1) {
                    for (k in j - 1 downTo 0)
                        if (rocks[j].path[i] == rocks[k].path[i]) {
                            rocks[j] = rocks[rockCount - 1]
                            rocks[k] = rocks[rockCount - 2]
                            rockCount -= 2
                        }
                }
            }
            // Remove rocks that crash into a wall
            for (j in rockCount - 1 downTo 0) {
                if (rocks[j].cellTypes.size <= i + 1) {
                    rocks[j] = rocks[rockCount - 1]
                    rockCount--
                }
            }

            if (actionCount > i + 1) return false

        }
        return true
    }

    fun getActions(indyPath: RealPath, rockPaths: List<RealPath>): List<Action> {
        var rockCount = rockPaths.size
        val rocks = rockPaths.toTypedArray()

        val actions = mutableListOf<Action>()

        for (i in 0 until indyPath.cellTypes.size) {
            val cellIndex = indyPath.path[i]
            val targetType = indyPath.cellTypes[i]
            val originType = cellTypes[cellIndex]

            if (targetType != originType) {
                getRotations(originType, targetType).forEach {
                    actions.add(
                        Action.Rotate(
                            cellIndex % width,
                            cellIndex / width,
                            it
                        )
                    )
                }
            }

            repeat(rockCount) { ir ->
                val rockCellIndex = rocks[ir].path[i]
                val rocKTargetType = rocks[ir].cellTypes[i]
                val rockOriginType = cellTypes[rockCellIndex]

                if (rocKTargetType != rockOriginType) {
                    getRotations(rockOriginType, rocKTargetType).forEach {
                        actions.add(
                            Action.Rotate(
                                rockCellIndex % width,
                                rockCellIndex / width,
                                it
                            )
                        )
                    }
                }

                if (rocks[ir].cellTypes.size <= i + 1) {
                    rocks[ir] = rocks[rockCount - 1]
                    rockCount--
                }
            }
        }
        return actions
    }
}


fun main() {
    var maxTime = 0L
    val board = GameReader.readBoard()
//    board.entrances.forEach(System.err::println)
    while (true) {
        val indy = GameReader.readIndy()
        measureTimeMillis {
            val rocks = GameReader.readRocks()
                .map { pos -> Rock(pos, board.findRockPaths(pos)) }

            val indyPaths = board.findIndyPaths(indy)
            val indyRealPaths =
                indyPaths.flatMap { it.toRealPath(board.cellTypes) { cellIndex, _ -> board.fixedMap[cellIndex] } }

            var actions: List<Action> = emptyList()

            for (indyRealPath in indyRealPaths) {
                System.err.println("Indy Path : ${indyRealPath.path}")
                System.err.println("Indy Cell Type : ${indyRealPath.cellTypes}")
                val realRocksPaths = rocks
//                    .filter { it.isDangerous(indyRealPath) }
//                   .onEach{ rock -> System.err.println(rock.paths.joinToString("\n"){"${it.path}"}) }
                    .map { rock ->
                        rock.paths.flatMap {
                            it.toRealPath(board.cellTypes) { cellIndex, pathIndex ->
                                board.fixedMap[cellIndex]
                            }
                        }
                    }
                System.err.println("Dangerous Rocks :: ")
//                rocks.forEach { System.err.println("${it.start} -> ${it.isDangerous(indyRealPath)}") }
//                realRocksPaths.forEach{
//                    System.err.println(it.joinToString("\n"){"${it.path}"})
//                    System.err.println(it.joinToString("\n"){"${it.cellTypes}"})
//                    System.err.println()
//                }
                val totalRockCombination = realRocksPaths.getCombinationCount()
                var i = 0L
                while (!board.isValidRealPathCombination(indyRealPath, realRocksPaths.getCombination(i))
                    && i < totalRockCombination
                ) {
                    i++
                }
                if (i < totalRockCombination) {

                    System.err.println("find a valid combination :")

                    realRocksPaths.getCombination(i).forEach {
                        System.err.println(it.path.joinToString())
                    }
                    actions = board.getActions(indyRealPath, realRocksPaths.getCombination(i))
                    break
                }
            }

            val action = actions.firstOrNull() ?: Action.Wait
            println(action)
            board.update(action)

        }.let {
            if (it > maxTime) maxTime = it
            System.err.println("Solution found in ${it}ms")
        }
        System.err.println("Max turn in ${maxTime}ms")
    }
}


