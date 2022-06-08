package fr.vco.codingame.puzzles.the.fall.episode2

import java.lang.Exception
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.math.absoluteValue
import kotlin.system.measureTimeMillis

enum class Direction(val x: Int, val y: Int) {
    TOP(0, 1),
    RIGHT(-1, 0),
    LEFT(1, 0),
    NONE(0, 0)
}

enum class Rotation { RIGHT, LEFT }

data class Position(
    val x: Int,
    val y: Int,
    val dir: Direction,
) {
    constructor(input: Scanner) : this(input.nextInt(), input.nextInt(), Direction.valueOf(input.next()))

    operator fun plus(dir: Direction) = Position(
        x = x + dir.x,
        y = y + dir.y,
        dir = dir
    )

    fun move(dir: Direction) = Position(
        x = x + this.dir.x,
        y = y + this.dir.y,
        dir = dir
    )
}

object CellType {

    fun rotate(type: Int, rotation: Rotation) = when (type) {
        2 -> 3
        3 -> 2
        4 -> 5
        5 -> 4
        6 -> if (rotation == Rotation.RIGHT) 7 else 9
        7 -> if (rotation == Rotation.RIGHT) 8 else 6
        8 -> if (rotation == Rotation.RIGHT) 9 else 7
        9 -> if (rotation == Rotation.RIGHT) 6 else 8
        10 -> if (rotation == Rotation.RIGHT) 11 else 13
        11 -> if (rotation == Rotation.RIGHT) 12 else 10
        12 -> if (rotation == Rotation.RIGHT) 13 else 11
        13 -> if (rotation == Rotation.RIGHT) 10 else 12
        else -> type
    }

    fun getRotations(origin: Int, target: Int): List<Rotation> {
        var rotateCount = 0
        var currentType = origin
        while (currentType != target && rotateCount < 3) {
            currentType = rotate(currentType, Rotation.RIGHT)
            rotateCount++
        }
        return when (rotateCount) {
            0 -> listOf()
            1 -> listOf(Rotation.RIGHT)
            2 -> listOf(Rotation.RIGHT, Rotation.RIGHT)
            3 -> listOf(Rotation.LEFT)
            else -> throw IllegalArgumentException("[$origin] can't be rotate in [$target]")
        }
    }


    fun outputDirection(type: Int?, inputDir: Direction): Direction {
        return when (type) {
            null -> Direction.NONE
            0 -> Direction.NONE
            1 -> Direction.TOP
            2 -> if (inputDir == Direction.LEFT || inputDir == Direction.RIGHT) inputDir else Direction.NONE
            3 -> if (inputDir == Direction.TOP) Direction.TOP else Direction.NONE
            4 -> if (inputDir == Direction.RIGHT) Direction.TOP else if (inputDir == Direction.TOP) Direction.RIGHT else Direction.NONE
            5 -> if (inputDir == Direction.LEFT) Direction.TOP else if (inputDir == Direction.TOP) Direction.LEFT else Direction.NONE
            6 -> if (inputDir == Direction.LEFT || inputDir == Direction.RIGHT) inputDir else Direction.NONE
            7 -> if (inputDir == Direction.TOP || inputDir == Direction.RIGHT) Direction.TOP else Direction.NONE
            8 -> if (inputDir == Direction.RIGHT || inputDir == Direction.LEFT) Direction.TOP else Direction.NONE
            9 -> if (inputDir == Direction.LEFT || inputDir == Direction.TOP) Direction.TOP else Direction.NONE
            10 -> if (inputDir == Direction.TOP) Direction.RIGHT else Direction.NONE
            11 -> if (inputDir == Direction.TOP) Direction.LEFT else Direction.NONE
            12 -> if (inputDir == Direction.RIGHT) Direction.TOP else Direction.NONE
            13 -> if (inputDir == Direction.LEFT) Direction.TOP else Direction.NONE
            else -> throw Exception("Should not happen !")
        }
    }

    fun possibleExits(type: Int, dir: Direction, isFixed: Boolean) =
        if (isFixed) listOf(outputDirection(type, dir))
        else if (dir == Direction.TOP) {
            when (type) {
                1 -> listOf(Direction.TOP)
                2, 3, 6, 7, 8, 9 -> listOf(Direction.TOP, Direction.NONE)
                4, 5 -> listOf(Direction.RIGHT, Direction.LEFT)
                10, 11, 12, 13 -> listOf(Direction.RIGHT, Direction.LEFT, Direction.NONE)
                0 -> listOf(Direction.NONE)
                else -> throw Exception("Should not happen !")
            }
        } else {
            when (type) {
                1 -> listOf(Direction.TOP)
                4, 5, 10, 11, 12, 13 -> listOf(Direction.TOP, Direction.NONE)
                2, 3 -> listOf(dir, Direction.NONE)
                6, 7, 8, 9 -> listOf(dir, Direction.TOP, Direction.NONE)
                0 -> listOf(Direction.NONE)
                else -> throw Exception("Should not happen !")
            }
        }
}

sealed interface Action {
    object Wait : Action {
        override fun toString() = "WAIT"
    }

    class Rotate(val x: Int, val y: Int, val rotation: Rotation) : Action {
        override fun toString() = "$x $y $rotation"
    }
}

object Board {
    var width: Int = 0
    var height: Int = 0
    var exitX: Int = 0
    var exitY: Int = 0
    lateinit var grid: Array<IntArray>
    lateinit var fixed: Array<BooleanArray>

    fun init(input: Scanner) {
        val (width, height) = input.nextLine().split(" ").map { it.toInt() }
        this.width = width
        this.height = height
        val lines = List(height) { input.nextLine().split(" ").map { it.toInt() }.toTypedArray() }

        grid = lines.map { it.map { n -> n.absoluteValue }.toIntArray() }.toTypedArray()
        fixed = lines.map { it.map { n -> n <= 0 }.toBooleanArray() }.toTypedArray()
        exitX = input.nextInt() // the coordinate along the X axis of the exit.
        exitY = height - 1
    }

    fun get(x: Int, y: Int) = grid.getOrNull(y)?.getOrNull(x) ?: 0

    fun isExit(x: Int, y: Int) = x == exitX && y == exitY
    fun isExit(position: Position) = isExit(position.x, position.y)

    fun update(action: Action) {
        if (action is Action.Rotate) {
            grid[action.y][action.x] = CellType.rotate(grid[action.y][action.x], action.rotation)
        }
    }

    fun findIndyPaths(start: Position): List<Path> {
        val validPaths = mutableListOf<Path>()
        val toVisit = ArrayDeque<Path>().apply { add(Path(start)) }

        while (toVisit.isNotEmpty()) {
            val current = toVisit.pop()

            if (isExit(current.entity)) {
                validPaths.add(current)
                continue
            }

            val type = get(current.entity.x, current.entity.y)
            val neighbors = CellType.possibleExits(
                type,
                current.entity.dir,
                fixed.getOrNull(current.entity.y)?.getOrNull(current.entity.x) ?: true
            )

            neighbors.filterNot { it == Direction.NONE }.forEach {
                val indy = current.entity + it
                val path = current.path + (indy.x to indy.y)
                val direction = current.directions + it
                toVisit.add(Path(indy, path, direction))
            }
        }
        return validPaths
    }

    fun findRockPaths(start: Position, indyPath: List<Pair<Int,Int>>, log: Boolean = false): List<Path> {
        val validPaths = mutableListOf<Path>()
        val toVisit = ArrayDeque<Path>().apply { add(Path(start)) }

        while (toVisit.isNotEmpty()) {
            val current = toVisit.pop()
            val indyPos = indyPath.getOrNull(current.path.size-1)

            if (current.entity.dir == Direction.NONE) {
                if (current.path.size > 1) validPaths.add(current)
                continue
            }

            val type = get(current.entity.x, current.entity.y)
            val isFixed = (fixed.getOrNull(current.entity.y)?.getOrNull(current.entity.x) ?: true)  ||
                (indyPos?.first == current.entity.x && indyPos.second == current.entity.y)

            val neighbors = CellType.possibleExits(
                type,
                current.entity.dir,
                isFixed
            )

            if(log){
                System.err.println("${current.path.size} -> $indyPos ${current.entity}")
                System.err.println("$type $neighbors")
            }

            neighbors.forEach {
                val rock = current.entity + it
                val path = current.path + (rock.x to rock.y)
                val direction = current.directions + it

                toVisit.add(Path(rock, path, direction))
            }
        }
        return validPaths
    }

    fun getActions(indyPath: Path2, rockPaths: List<Path2>): List<Action>? {
        var rockCount = rockPaths.size
        val rocks = rockPaths.toTypedArray()

        val actions = mutableListOf<Action>()
        for (i in 0 until indyPath.cellTypes.size) {
            val (x, y) = indyPath.path[i]
            val targetType = indyPath.cellTypes[i]
            val originType = get(x, y)

            if (targetType != originType) {
                CellType.getRotations(originType, targetType).forEach { actions.add(Action.Rotate(x, y, it)) }
            }

            for (j in 0 until rockCount) {
                val (rx, ry) = rocks[j].path[i]
                val originRockType = get(rx, ry)
                val targetRockType = rocks[j].cellTypes[i]
                val rotations = CellType.getRotations(originRockType, targetRockType)

                //if (rotations.isNotEmpty() && rx == x && ry == y) return null // TODO check the real position
                rotations.forEach { actions.add(Action.Rotate(rx, ry, it)) }
            }

            for (j in rockCount - 1 downTo 0) {
                if (rocks[j].cellTypes.size <= i + 1) {
                    rocks[j] = rocks[rockCount - 1]
                    rockCount--
                }
            }

            for (j in 0 until rockCount) {
                if (rocks[j].path[i] == indyPath.path[i]) return null
            }

            if (actions.size > i + 1 ) return null

        }
        return actions
    }


//    fun getRotations(type: Int, inputDir: Direction, outputDir: Direction): List<Rotation> {
//        var rotateCount = 0
//        var currentType = type
//        while (CellType.outputDirection(currentType, inputDir) != outputDir && rotateCount < 3) {
//            currentType = CellType.rotate(currentType, Rotation.RIGHT)
//            rotateCount++
//        }
//        return when (rotateCount) {
//            1 -> listOf(Rotation.RIGHT)
//            2 -> listOf(Rotation.RIGHT, Rotation.RIGHT)
//            3 -> listOf(Rotation.LEFT)
//            else -> emptyList()
//        }
//    }

    fun extendPath(path: Path, isFixed: (x: Int, y: Int, i :Int) -> Boolean = {_,_,_->false}): List<Path2> {
        val paths = mutableListOf<List<Int>>()

        for (i in 0 until path.directions.size - 1) {
            val (x, y) = path.path[i]
            val type = get(x, y)

            val isFixed = (fixed.getOrNull(y)?.getOrNull(x)?: true) || isFixed(x,y,i)

            if(isFixed) {
                paths.add(listOf(type))
                continue
            }
            val inputDir = path.directions[i]
            val outputDir = path.directions[i + 1]
            paths.add(getPossibleRotations(type, inputDir, outputDir).toList())
        }
        return paths.getCombinations().map { Path2(path.entity, path.path, it) }
    }

    fun getPossibleRotations(type: Int, inputDir: Direction, outputDir: Direction): Set<Int> {
        var currentType = type

        val possibleRotations = mutableSetOf<Int>()
        repeat(4) {
            if (CellType.outputDirection(currentType, inputDir) == outputDir) {
                possibleRotations.add(currentType)
            }
            currentType = CellType.rotate(currentType, Rotation.RIGHT)
        }
        return possibleRotations
    }
}


class Path2(
    val entity: Position,
    val path: List<Pair<Int, Int>> = emptyList(),
    val cellTypes: List<Int> = emptyList()
)

class Path(
    val entity: Position,
    val path: List<Pair<Int, Int>> = emptyList(),
    val directions: List<Direction> = emptyList()
) {

    override fun equals(other: Any?): Boolean {
        return if (other is Path) other.entity == entity
        else false
    }

    override fun hashCode() = entity.hashCode()
}

fun <T : Any> List<List<T>>.getCombination(i: Long): List<T> {
    val result = mutableListOf<T>()
    var index = i
    this.filter { it.isNotEmpty() }.forEach {
        val current = index % it.size
        index /= it.size
        result.add(it[current.toInt()])
    }
    return result
}

fun <T : Any> List<List<T>>.getCombinationCount(): Long {
    return this.filter { it.isNotEmpty() }.fold(1L) { acc, a -> a.size * acc }
}

fun <T : Any> List<List<T>>.getCombinations(): List<List<T>> {
    return List(getCombinationCount().toInt()) { getCombination(it.toLong()) }
}

fun main() {
    val input = Scanner(System.`in`)
    Board.init(input)

    var maxTime = 0L
    // game loop
    while (true) {
        val indy = Position(input)
        val r = input.nextInt() // the number of rocks currently in the grid.
        val rocks = List(r) { Position(input) }

        measureTimeMillis {
            val validIndyPaths = Board.findIndyPaths(indy).flatMap(Board::extendPath)
            val validIndyPath = validIndyPaths.minByOrNull { it.path.size }!!
            val completePath = listOf(indy.x to indy.y) + validIndyPath.path

            val rockPaths = rocks.mapIndexed { i, it ->  Board.findRockPaths(it, completePath, i == rocks.size-1 )}
            val extendedRockPath = rockPaths.map{ path ->
                path.flatMap {
                    Board.extendPath(it) { x, y, i ->
                        val (xi, yi) = completePath[i]
                        xi == x && yi == y
                    }
                }
            }

            var actions: List<Action>? = null
            val totalRockCombination = extendedRockPath.getCombinationCount()
            System.err.println("Rocks Combinations : $totalRockCombination")

            for (indyPath in validIndyPaths) {

                var i = 0L
                while (actions == null && i < totalRockCombination) {
                    actions = Board.getActions(indyPath, extendedRockPath.getCombination(i))
                    i++
                }
                if (actions != null) {
                    System.err.println(indyPath.path)
                    System.err.println(indyPath.cellTypes)

                    extendedRockPath.getCombination(i - 1).forEach {
                        System.err.println(it.path)
                        System.err.println(it.cellTypes)
                    }
                    break
                }
            }

            System.err.println(actions)

            val action = actions?.firstOrNull() ?: Action.Wait
//            val action = Board.getRotations(validIndyPath.directions, validIndyPath.path).firstOrNull() ?: Action.Wait
            println(action)
            Board.update(action)
        }.let {
            if (it > maxTime) maxTime = it
            System.err.println("Solution found in ${it}ms")
        }

        System.err.println("Max turn in ${maxTime}ms")
    }
}


