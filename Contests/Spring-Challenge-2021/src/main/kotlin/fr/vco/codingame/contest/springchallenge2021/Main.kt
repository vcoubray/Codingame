package fr.vco.codingame.contest.springchallenge2021

import fr.vco.codingame.contest.springchallenge2021.game.Board
import fr.vco.codingame.contest.springchallenge2021.game.Game
import fr.vco.codingame.contest.springchallenge2021.heuristic.Heuristic
import fr.vco.codingame.contest.springchallenge2021.mcts.Mcts
import java.util.*

fun log(message: Any?) = System.err.println(message.toString())




fun readPossibleMoves(input: Scanner): List<String> {
    val numberOfPossibleMoves = input.nextInt()
    if (input.hasNextLine()) {
        input.nextLine()
    }
    return List(numberOfPossibleMoves) { input.nextLine() }
}

fun main() {
    val input = Scanner(System.`in`)

    val startInit = System.currentTimeMillis()
    Board.init(input)
    log("init board in ${System.currentTimeMillis() - startInit}ms")

    val game = Game()

    while (true) {
        game.readInput(input)
        game.turn++
        readPossibleMoves(input)

        log("Read state in ${game.currentExecutionTime()}ms")
        // MCTS
        val timeout = if (game.turn == 1) 900 else 90
        val action = Mcts.findNextMove(game, timeout)
        action.print(Mcts.summary())

        // Heuristic
//        Heuristic(game).bestAction().print()

        val executionTime = game.currentExecutionTime()
        log("End turn in ${executionTime}ms ")
    }
}
