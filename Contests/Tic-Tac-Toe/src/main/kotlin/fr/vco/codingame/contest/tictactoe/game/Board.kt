package fr.vco.codingame.contest.tictactoe.game

import fr.vco.codingame.contest.tictactoe.DRAW
import fr.vco.codingame.contest.tictactoe.IN_PROGRESS
import fr.vco.codingame.contest.tictactoe.ME
import fr.vco.codingame.contest.tictactoe.OPP

class Board(
    var currentPlayer: Int = ME,
    val freeCell: MutableList<Pair<Int, Int>> = mutableListOf(),
    val grid: List<MutableList<Int>> = List(3) { MutableList(3) { IN_PROGRESS } }
) {

    fun getActions() = freeCell.map { (row, col) -> Action(row, col, currentPlayer) }

    fun play(action: Action) {
        grid[action.row][action.col] = action.player
        freeCell.remove(action.row to action.col)
        currentPlayer = 1 - action.player
    }

    fun getStatus(): Int {
        if (grid[1][1] != IN_PROGRESS && grid[1][1] == grid[0][0] && grid[1][1] == grid[2][2]) return grid[1][1]
        if (grid[1][1] != IN_PROGRESS && grid[1][1] == grid[0][2] && grid[1][1] == grid[2][0]) return grid[1][1]
        repeat(3) { i ->
            if (grid[i][0] != IN_PROGRESS && grid[i][0] == grid[i][1] && grid[i][0] == grid[i][2]) return grid[i][0]
            if (grid[0][i] != IN_PROGRESS && grid[0][i] == grid[1][i] && grid[0][i] == grid[2][i]) return grid[0][i]
        }
        return if (freeCell.size > 0) IN_PROGRESS else DRAW
    }

    override fun toString(): String {
        return grid.joinToString("\n") { row ->
            row.joinToString(" ") {
                when (it) {
                    ME -> "X"
                    OPP -> "O"
                    else -> "_"
                }
            }
        }
    }

//    fun copy(): Grid {
//        return Grid(
//            freeCell.toMutableList(),
//            grid.map{it.toMutableList()}
//        )
//    }

    fun load(board: Board) {
        currentPlayer = board.currentPlayer
        freeCell.clear()
        freeCell.addAll(board.freeCell)
        board.grid.forEachIndexed { iRow, row ->
            row.forEachIndexed { iCol, col ->
                grid[iRow][iCol] = col
            }
        }

    }

    fun simulateRandomGame() : Int {
        while (getStatus() == IN_PROGRESS) {
            val action = getActions().random()
            play(action)
        }
        return getStatus()
    }

}