package fr.vco.codingame.puzzles.the.fall.episode2

import java.util.*
import kotlin.math.absoluteValue

enum class Direction { TOP, RIGHT, LEFT }

data class Position(
    var x: Int,
    var y: Int,
    var dir: Direction,
) {
    constructor(input: Scanner) : this(input.nextInt(), input.nextInt(), Direction.valueOf(input.next()))

    fun move(dir: Direction) {
        when (dir) {
            Direction.LEFT -> x++
            Direction.RIGHT -> x--
            Direction.TOP -> y++
        }
        this.dir = dir
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
    lateinit var board: Array<IntArray>
    lateinit var fixed: Array<BooleanArray>

    fun init(input: Scanner) {

        val (width, height) = input.nextLine().split(" ").map { it.toInt() }
        this.width = width
        this.height = height
        val lines = List(height) { input.nextLine().split(" ").map { it.toInt() }.toTypedArray() }

        board = lines.map { it.map { n -> n.absoluteValue }.toIntArray() }.toTypedArray()
        fixed = lines.map { it.map { n -> n <= 0 }.toBooleanArray() }.toTypedArray()
        exitX = input.nextInt() // the coordinate along the X axis of the exit.
        exitY = height - 1
    }
}

class State(
    val indy: Position,
    val board: Array<IntArray>
) {

    fun copy() = State(
        indy.copy(),
        board.map { it.copyOf() }.toTypedArray()
    )

    fun play(action: Action) {

        if (action is Action.Rotate) {
            val cell = board[action.y][action.x]
            board[action.y][action.x] = when (cell) {
                2 -> 3
                3 -> 2
                4 -> 5
                5 -> 4
                6 -> if (action.rotation == "RIGHT") 7 else 9
                7 -> if (action.rotation == "RIGHT") 8 else 6
                8 -> if (action.rotation == "RIGHT") 9 else 7
                9 -> if (action.rotation == "RIGHT") 6 else 8
                10 -> if (action.rotation == "RIGHT") 11 else 13
                11 -> if (action.rotation == "RIGHT") 12 else 10
                12 -> if (action.rotation == "RIGHT") 13 else 11
                13 -> if (action.rotation == "RIGHT") 10 else 12
                else -> cell
            }
        }

        when (board[indy.y][indy.x]) {
            0, 1, 3, 7, 8, 9, 12, 13 -> indy.move(Direction.TOP)
            2, 6 -> if (indy.dir == Direction.LEFT) indy.move(Direction.LEFT) else indy.move(Direction.RIGHT)
            4, 10 -> if (indy.dir == Direction.RIGHT) indy.move(Direction.TOP) else indy.move(Direction.RIGHT)
            5, 11 -> if (indy.dir == Direction.LEFT) indy.move(Direction.TOP) else indy.move(Direction.LEFT)
        }
    }

    fun isValid(): Boolean {
        return when (board.getOrNull(indy.y)?.getOrNull(indy.x)) {
            null, 0 -> false
            2, 6, 8 -> indy.dir != Direction.TOP
            3, 10, 11 -> indy.dir == Direction.TOP
            4, 7 -> indy.dir != Direction.LEFT
            5, 9 -> indy.dir != Direction.RIGHT
            12 -> indy.dir == Direction.RIGHT
            13 -> indy.dir == Direction.LEFT
            1 -> true
            else -> false
        }
    }

    fun isValidPos() = board.getOrNull(indy.y)?.getOrNull(indy.x) != null

    override fun toString(): String {
        val sb = StringBuilder(indy.toString()).appendLine()
        sb.append(board.joinToString("\n") { it.joinToString(" ") })
        return sb.toString()
    }
}

fun isValidPath(state: State, exitX: Int, exitY: Int): Boolean {
    if (state.indy.y == exitY && state.indy.x == exitX) return true
    if (!state.isValid()) return false

    val nextPos = state.copy().apply { play(Action.Wait) }.takeIf { it.isValidPos() }?.indy ?: return false
    val actions = if (!Board.fixed[nextPos.y][nextPos.x]) {
        listOf(
            Action.Wait,
            Action.Rotate(nextPos.x, nextPos.y, "RIGHT"),
            Action.Rotate(nextPos.x, nextPos.y, "LEFT"),
        )
    } else listOf(Action.Wait)
    for (action in actions) {
        val child = state.copy().apply { play(action) }
        if (isValidPath(child, exitX, exitY)) return true
    }
    return false
}

fun main() {
    val input = Scanner(System.`in`)
    Board.init(input)

    Board.fixed.forEach { System.err.println(it.joinToString(" ")) }

    // game loop
    var currentBoard = Board.board
    while (true) {
        val indy = Position(input)
        System.err.println(indy)
        val state = State(indy, currentBoard)

        val R = input.nextInt() // the number of rocks currently in the grid.
        val rocks = List(R) { Position(input) }

        val nextPos = state.copy().apply { play(Action.Wait) }.indy

        val actions = if (!Board.fixed[nextPos.y][nextPos.x]) {
            listOf(
                Action.Wait,
                Action.Rotate(nextPos.x, nextPos.y, "RIGHT"),
                Action.Rotate(nextPos.x, nextPos.y, "LEFT"),
            )
        } else listOf(Action.Wait)

        var actionToPlay: Action? = null
        for (action in actions) {
            val child = state.copy().apply { play(action) }
            if (isValidPath(child, Board.exitX, Board.exitY)) {
                currentBoard = child.board
                actionToPlay = action
                break
            }
        }

        if (actionToPlay == null) {
            System.err.println("There is no hope !")
        }
        println(actionToPlay ?: Action.Wait)
    }
}