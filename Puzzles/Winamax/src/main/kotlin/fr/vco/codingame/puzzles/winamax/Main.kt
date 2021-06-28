package fr.vco.codingame.puzzles.winamax

import java.util.*

var WIDTH: Int = 0
var HEIGHT: Int = 0

fun Char.toDigit() = this.toString().toInt()

enum class Direction(val vx: Int, val vy: Int, val symbol: Char) {
    LEFT(-1, 0, '<'),
    RIGHT(1, 0, '>'),
    TOP(0, -1, '^'),
    BOT(0, 1, 'v')
}

class State {
    val originalBoard: CharArray = CharArray(HEIGHT * WIDTH)
    val balls: MutableList<Int> = mutableListOf()
    lateinit var board: CharArray

    fun print() = board.map { if (it in "<>v^.") it else '.' }.joinToString("").chunked(WIDTH).forEach(::println)

    fun printDebug() = board.joinToString("").chunked(WIDTH).forEach(System.err::println)

    private fun isValidMove(ballId: Int, direction: Direction): Boolean {
        val ballPos = balls[ballId]
        val ballMove = board[ballPos].toDigit()
        if (ballMove <= 0) return false
        var x = ballPos % WIDTH
        var y = ballPos / WIDTH

        repeat(ballMove - 1) {
            x += direction.vx
            y += direction.vy
            if (!(x in 0 until WIDTH && y in 0 until HEIGHT && board[x + y * WIDTH] in "X.")) return false
        }

        x += direction.vx
        y += direction.vy
        return (x in 0 until WIDTH && y in 0 until HEIGHT && board[x + y * WIDTH] in "H.")
    }

    private fun drawMove(ballId: Int, direction: Direction) {
        val ballPos = balls[ballId]
        val ballMoves = board[ballPos].toDigit()
        var pos = ballPos

        repeat(ballMoves) {
            board[pos] = direction.symbol
            pos += direction.vx + direction.vy * WIDTH
        }

        balls[ballId] = pos
        board[pos] = (ballMoves - 1).digitToChar()
    }

    private fun undrawMove(ballId: Int, direction: Direction) {
        val ballPos = balls[ballId]
        val move = board[ballPos].toDigit() + 1
        var pos = ballPos

        repeat(move) {
            board[pos] = originalBoard[pos]
            pos -= direction.vx + direction.vy * WIDTH
        }

        balls[ballId] = pos
        board[pos] = move.digitToChar()
    }


    fun moveBall(id: Int): Boolean {
        if (originalBoard[balls[id]] == 'H') {
            return if (id == balls.size - 1) true
            else moveBall(id + 1)
        } else {
            Direction.values().forEach {
                if (isValidMove(id, it)) {
                    drawMove(id, it)
                    if (moveBall(id)) return true
                    undrawMove(id, it)
                }
            }
        }
        return false
    }
}

fun main() {

    val input = Scanner(System.`in`)
    WIDTH = input.nextInt()
    HEIGHT = input.nextInt()

    val state = State()
    repeat(HEIGHT) { y ->
        val line = input.next()
        line.forEachIndexed { x, c ->
            val pos = x + y * WIDTH
            state.originalBoard[pos] = c
            if (c.isDigit()) state.balls.add(pos)
        }
    }
    state.board = state.originalBoard.copyOf()

    state.moveBall(0)
    state.print()
}
