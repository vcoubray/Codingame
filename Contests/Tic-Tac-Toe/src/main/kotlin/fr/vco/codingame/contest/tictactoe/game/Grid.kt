package fr.vco.codingame.contest.tictactoe.game

import fr.vco.codingame.contest.tictactoe.DRAW
import fr.vco.codingame.contest.tictactoe.IN_PROGRESS
import fr.vco.codingame.contest.tictactoe.ME
import fr.vco.codingame.contest.tictactoe.OPP



class Grid(
    var status: Int = IN_PROGRESS,
    val freeCell: MutableList<Pair<Int, Int>> = mutableListOf(),
    val grid: Array<IntArray> = Array(3) { IntArray(3) { IN_PROGRESS } }
) {

//    fun getActions() = freeCell.map { (row, col) -> Action(row, col, currentPlayer) }

    fun play(action: Action) {
        grid[action.row][action.col] = action.player
        freeCell.remove(action.row to action.col)
        status = calcStatus()
    }

    private fun calcStatus(): Int {
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


    fun load(grid: Grid) {
        status = grid.status
        freeCell.clear()
        freeCell.addAll(grid.freeCell)
        grid.grid.forEachIndexed { iRow, row ->
            row.forEachIndexed { iCol, col ->
                this.grid[iRow][iCol] = col
            }
        }

    }


}