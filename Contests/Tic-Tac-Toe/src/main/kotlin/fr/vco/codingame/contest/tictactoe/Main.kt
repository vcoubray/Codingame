package fr.vco.codingame.contest.tictactoe

import fr.vco.codingame.contest.tictactoe.game.Action
import fr.vco.codingame.contest.tictactoe.game.Board
import fr.vco.codingame.contest.tictactoe.game.Grid
import fr.vco.codingame.contest.tictactoe.mcts.Mcts
import java.util.*

const val ME = 0
const val OPP = 1
const val IN_PROGRESS = 2
const val DRAW = 3

fun log(message: Any?) = System.err.println(message)

fun main() {
    val input = Scanner(System.`in`)

    val freeCells = mutableListOf<Pair<Int,Int>>()
    repeat(3) { row ->
        repeat(3) { col ->
            freeCells.add(row to col)
        }
    }
    val grids = Array(9){Grid(IN_PROGRESS, freeCells)}
    val board = Board(grids)

    while (true) {
        input.readOpponentAction()?.let(board::play)
        input.readValidAction()

        val action = Mcts.findNextMove(board)!!

        println("$action ${Mcts.summary()}")
        board.play(action)
    }
}

fun Scanner.readOpponentAction(): Action? {
    val opponentRow = nextInt()
    val opponentCol = nextInt()
    return if (opponentCol >= 0) Action(opponentRow, opponentCol, OPP)
    else null
}

fun Scanner.readValidAction() {
    repeat(nextInt()) {
        nextInt()
        nextInt()
    }
}