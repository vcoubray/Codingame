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
    val grids = List(9){Grid(IN_PROGRESS, freeCells.toMutableList())}
    val board = Board(grids)


    // game loop
    while (true) {
        val opponentRow = input.nextInt()
        val opponentCol = input.nextInt()
        if (opponentCol >= 0) {
            val opAction = Action(opponentRow, opponentCol, OPP)
            board.play(opAction)
        }

        val validActionCount = input.nextInt()
        val freeCells = List(validActionCount) {
            input.nextInt() to input.nextInt()
        }
//        freeCells.forEach(::log)
        val action = Mcts.findNextMove(board)!!
//        board.getActions().forEach(::log)

//        val action = freeCells.randomOrNull()?.let{Action(it.first,it.second,ME)}!!

        println(action)
        board.play(action)
//        log(board)
//        log(board.getStatus())

    }
}