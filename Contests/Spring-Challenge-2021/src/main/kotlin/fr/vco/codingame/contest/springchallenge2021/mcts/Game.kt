package fr.vco.codingame.contest.springchallenge2021.mcts

import fr.vco.codingame.contest.springchallenge2021.*
import java.util.*


class Game {
    var day: Int = 0
    var nutrients: Int = INITIAL_NUTRIENTS
    var sun: Int = 0
    var score: Int = 0
    var oppSun: Int = 0
    var oppScore: Int = 0
    var oppIsWaiting: Boolean = false
    var trees: List<Tree> = List(BOARD_SIZE) { Tree(it) }

    var turn = 0

    private var startTime = System.currentTimeMillis()


    fun readInput(input: Scanner) {
        day = input.nextInt() // the game lasts 24 days: 0-23
        startTime = System.currentTimeMillis()
        nutrients = input.nextInt() // the base score you gain from the next COMPLETE action
        sun = input.nextInt() // your sun points
        score = input.nextInt() // your current score
        oppSun = input.nextInt() // opponent's sun points
        oppScore = input.nextInt() // opponent's score
        oppIsWaiting = input.nextInt() != 0 // whether your opponent is asleep until the next day
        trees.forEach { it.size = NONE }
        List(input.nextInt()) {
            Tree(
                cellIndex = input.nextInt(),
                size = input.nextInt(),
                owner = if (input.nextInt() == 1) ME else OPP,
                isDormant = input.nextInt() == 1
            )
        }.forEach {
            trees[it.cellIndex].size = it.size
            trees[it.cellIndex].isDormant = it.isDormant
            trees[it.cellIndex].owner = it.owner
        }

    }

    fun currentExecutionTime() = System.currentTimeMillis() - startTime

}


