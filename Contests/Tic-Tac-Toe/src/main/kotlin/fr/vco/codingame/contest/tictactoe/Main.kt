package fr.vco.codingame.contest.tictactoe

import java.util.*

const val IN_PROGRESS = 0
const val PLAYER_1 = 1
const val PLAYER_2 = 2

fun log(message: Any?) = System.err.println(message)

data class Action(val row: Int, val col: Int) {
    override fun toString() = "$row $col"
}

class Grid(
    val freeCell: MutableList<Action>,
    val grid: List<MutableList<Int>> = List(3) { MutableList(3) { IN_PROGRESS } }
) {

    fun getActions() = freeCell

    fun play(action: Action, player: Int) {
        grid[action.row][action.col] = player
        freeCell.remove(action)
    }

    fun getStatus(): Int {
        if (grid[1][1] != IN_PROGRESS && grid[1][1] == grid[0][0] && grid[1][1] == grid[2][2]) return grid[1][1]
        if (grid[1][1] != IN_PROGRESS && grid[1][1] == grid[0][2] && grid[1][1] == grid[2][0]) return grid[1][1]
        repeat(3) { i ->
            if (grid[i][0] != IN_PROGRESS && grid[i][0] == grid[i][1] && grid[i][0] == grid[i][2]) return grid[i][0]
            if (grid[0][i] != IN_PROGRESS && grid[0][i] == grid[1][i] && grid[0][i] == grid[2][i]) return grid[0][i]
        }
        return IN_PROGRESS
    }
}


fun main() {
    val input = Scanner(System.`in`)


    val freeCells = mutableListOf<Action>()
    repeat(3) { row ->
        repeat(3) { col ->
            freeCells.add(Action(row, col))
        }
    }

    val grid = Grid(freeCells)

    // game loop
    while (true) {
        val opponentRow = input.nextInt()
        val opponentCol = input.nextInt()
        if (opponentCol >= 0) {
            val opAction = Action(opponentRow, opponentCol)
            grid.play(opAction, PLAYER_2)
        }

        val validActionCount = input.nextInt()

        val freeCells = List(validActionCount) {
            input.nextInt() to input.nextInt()
        }
        val action = grid.getActions().randomOrNull()!!

        println(action)
        grid.play(action, PLAYER_1)
        grid.grid.joinToString("\n") { line ->
            line.joinToString(" ") {
                when (it) {
                    PLAYER_1 -> "X"
                    PLAYER_2 -> "O"
                    else -> "_"
                }
            }
        }.let(::log)
        log(grid.getStatus())

    }
}