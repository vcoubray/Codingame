package fr.vco.codingame.puzzles.the.fall.episode2

import java.lang.Exception
import java.util.*
import kotlin.math.absoluteValue
import kotlin.system.measureTimeMillis

enum class Direction(val x: Int, val y: Int) {
    TOP(0, 1),
    RIGHT(-1, 0),
    LEFT(1, 0),
    NONE(0, 0)
}

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
}

object CellType {

    fun rotate(type: Int, rotation: String) = when (type) {
        2 -> 3
        3 -> 2
        4 -> 5
        5 -> 4
        6 -> if (rotation == "RIGHT") 7 else 9
        7 -> if (rotation == "RIGHT") 8 else 6
        8 -> if (rotation == "RIGHT") 9 else 7
        9 -> if (rotation == "RIGHT") 6 else 8
        10 -> if (rotation == "RIGHT") 11 else 13
        11 -> if (rotation == "RIGHT") 12 else 10
        12 -> if (rotation == "RIGHT") 13 else 11
        13 -> if (rotation == "RIGHT") 10 else 12
        else -> type
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

    fun possibleExit(type: Int?, dir: Direction, isFixed: Boolean) =
        if (isFixed) listOf(outputDirection(type, dir))
        else if (dir == Direction.TOP) {
            when (type) {
                1, 2, 3, 6, 7, 8, 9 -> listOf(Direction.TOP)
                4, 5, 10, 11, 12, 13 -> listOf(Direction.RIGHT, Direction.LEFT)
                null, 0 -> listOf(Direction.NONE)
                else -> throw Exception("Should not happen !")
            }
        } else {
            when (type) {
                1, 4, 5, 10, 11, 12, 13 -> listOf(Direction.TOP)
                2, 3 -> listOf(dir)
                6, 7, 8, 9 -> listOf(dir, Direction.TOP)
                null, 0 -> listOf(Direction.NONE)
                else -> throw Exception("Should not happen !")
            }
        }
}

sealed interface Action {
    object Wait : Action {
        override fun toString() = "WAIT"
    }

    class Rotate(val x: Int, val y: Int, val rotation: String) : Action {
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
    fun get(x: Int, y: Int) = grid.getOrNull(y)?.getOrNull(x)


    fun update(action: Action) {
        if (action is Action.Rotate) {
            grid[action.y][action.x] = CellType.rotate(grid[action.y][action.x], action.rotation)
        }
    }

    fun findPaths(start: State): List<State> {
        val validStates = mutableListOf<State>()
        val toVisit = ArrayDeque<State>().apply { add(start) }
        val visited = mutableListOf<State>()

        while (toVisit.isNotEmpty()) {
            val current = toVisit.pop()
            visited.add(current)
            if (current.indy.x == exitX && current.indy.y == exitY) {
                validStates.add(current)
                continue
            }
            val type = get(current.indy.x, current.indy.y)
            val neighbors = CellType.possibleExit(
                type,
                current.indy.dir,
                fixed.getOrNull(current.indy.y)?.getOrNull(current.indy.x) ?: true
            )
            neighbors.filterNot { it == Direction.NONE }.forEach {
                val indy = current.indy + it
                val path = current.path + (indy.x to indy.y)
                val direction = current.directions + it
                toVisit.add(State(indy, path, direction))
            }
        }
        return validStates
    }


    fun getRotations(directions: List<Direction>, path: List<Pair<Int, Int>>): List<Action> {
        val actions = mutableListOf<Action>()
        var inputDir = directions.first()
        for (i in 0 until directions.size - 1) {
            val (x, y) = path[i]
            var type = get(x, y)!!
            val outputDir = directions[i + 1]
            var rotateCount = 0
            while (CellType.outputDirection(type, inputDir) != outputDir) {
                type = CellType.rotate(type, "RIGHT")
                rotateCount++
            }
            when (rotateCount) {
                1 -> actions.add(Action.Rotate(x, y, "RIGHT"))
                2 -> repeat(2) { actions.add(Action.Rotate(x, y, "RIGHT")) }
                3 -> actions.add(Action.Rotate(x, y, "LEFT"))
            }

            inputDir = outputDir
        }
        return actions
    }
}

class State(
    val indy: Position,
    val path: List<Pair<Int, Int>> = emptyList(),
    val directions: List<Direction> = emptyList()
) {

    override fun equals(other: Any?): Boolean {
        return if (other is State) other.indy == indy
        else false
    }

    override fun hashCode() = indy.hashCode()
}

fun main() {
    val input = Scanner(System.`in`)
    Board.init(input)

    // game loop
    while (true) {
        val indy = Position(input)
        val r = input.nextInt() // the number of rocks currently in the grid.
        val rocks = List(r) { Position(input) }
        val state = State(indy)

        measureTimeMillis {
            val validStates = Board.findPaths(state)
            val validState = validStates.minByOrNull { it.path.size }!!

            val action = Board.getRotations(validState.directions, validState.path).firstOrNull() ?: Action.Wait
            println(action)
            Board.update(action)
        }.let{System.err.println("Solution found in ${it}ms")}
    }

}