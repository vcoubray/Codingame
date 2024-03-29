package fr.vco.codingame.contest.tictactoe.game

import fr.vco.codingame.contest.tictactoe.*


class Board(
    val grids: Array<Grid> = Array(9) { Grid() },
    var currentPlayer: Int = ME,
    var nextGridId: Int = -1,
    var status: Int = IN_PROGRESS
) {

    fun getActions(): List<Action> {
        val nextGrid = grids.getOrNull(nextGridId)
        return if (nextGrid?.status == IN_PROGRESS) nextGrid.freeCell.map {
            Action(
                it.first + nextGridId / 3 * 3,
                it.second + nextGridId % 3 * 3,
                currentPlayer
            )
        }
        else grids.flatMapIndexed { i, grid ->
            if (grid.status == IN_PROGRESS) grid.freeCell.map {
                Action(
                    it.first + i / 3 * 3,
                    it.second + i % 3 * 3,
                    currentPlayer
                )
            }
            else emptyList()
        }

    }

    fun play(action: Action) {
        val gridNumber = action.row / 3 * 3 + action.col / 3
        val row = action.row % 3
        val col = action.col % 3

        grids[gridNumber].play(Action(row, col, action.player))
        currentPlayer = 1 - action.player
        nextGridId = row * 3 + col
        status = calcStatus()
    }

    fun calcStatus(): Int {

        if (grids[4].status != IN_PROGRESS && grids[4].status == grids[0].status && grids[4].status == grids[8].status) return grids[4].status
        if (grids[4].status != IN_PROGRESS && grids[4].status == grids[2].status && grids[4].status == grids[6].status) return grids[4].status
        repeat(3) { i ->
            val j = 3 * i
            if (grids[j].status != IN_PROGRESS && grids[j].status == grids[j + 1].status && grids[j].status == grids[j + 2].status) return grids[j].status
            if (grids[i].status != IN_PROGRESS && grids[i].status == grids[i + 3].status && grids[i].status == grids[i + 6].status) return grids[i].status
        }


        var winner = 0
        grids.forEach {
            when (it.status) {
                IN_PROGRESS -> return IN_PROGRESS
                ME -> winner++
                OPP -> winner--
            }
        }
        return when {
            winner > 0 -> ME
            winner < 0 -> OPP
            else -> DRAW
        }

    }

    fun load(board: Board) {
        currentPlayer = board.currentPlayer
        nextGridId = board.nextGridId
        status = board.status
        board.grids.forEachIndexed { i, grid ->
            grids[i].load(grid)
        }
    }


    fun simulateRandomGame(): Int {
        while (status == IN_PROGRESS) {
            play(getActions().random())
        }
        return status
    }


    override fun toString(): String {
        val sb = StringBuffer()
        repeat(9) { row ->
            repeat(3) { col ->
                sb.append("${
                    grids[row / 3 * 3 + col].grid[row % 3].joinToString(" ") {
                        when (it) {
                            ME -> "X"
                            OPP -> "O"
                            else -> "_"
                        }
                    }
                }|")
            }
            sb.append("\n")
        }
        return sb.toString()
    }

}

