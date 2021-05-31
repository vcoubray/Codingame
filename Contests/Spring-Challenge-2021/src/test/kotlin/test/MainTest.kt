package test

import fr.vco.codingame.contest.springchallenge2021.*
import fr.vco.codingame.contest.springchallenge2021.mcts.*
//import fr.vco.codingame.contest.springchallenge2021.mcts.PoolState


fun main() {
//    PoolState.reset()
    val game = initTestGame()

    val state = State().initFromGame(game)
    val state2 = state.getNextState(WaitAction(ME))
    val state3 = state2.getNextState(WaitAction(OPP))
    val node = MctsNode(null, state3)


//    val simulation = node.state.copyCustom()

//    simulation.simulateRandomGame()

    repeat(10) {
        Mcts.findNextMove(state3, 80)
        println("----")
    }
    println("Max Execution Time : ${Mcts.maxExecutionTime}ms")
//    Mcts.simulateRandomGame(node)

}

fun initTestGame(): Game {
    Board.cells = listOf(
        Cell(0, 3, listOf(1, 2, 3, 4, 5, 6)),
        Cell(1, 3, listOf(7, 8, 2, 0, 6, 18)),
        Cell(2, 3, listOf(8, 9, 10, 3, 0, 1)),
        Cell(3, 3, listOf(2, 10, 11, 12, 4, 0)),
        Cell(4, 3, listOf(0, 3, 12, 13, 14, 5)),
        Cell(5, 3, listOf(6, 0, 4, 14, 15, 16)),
        Cell(6, 3, listOf(18, 1, 0, 5, 16, 17)),
        Cell(7, 2, listOf(19, 20, 8, 1, 18, 36)),
        Cell(8, 2, listOf(20, 21, 9, 2, 1, 7)),
        Cell(9, 2, listOf(21, 22, 23, 10, 2, 8)),
        Cell(10, 2, listOf(9, 23, 24, 11, 3, 2)),
        Cell(11, 2, listOf(10, 24, 25, 26, 12, 3)),
        Cell(12, 2, listOf(3, 11, 26, 27, 13, 4)),
        Cell(13, 2, listOf(4, 12, 27, 28, 29, 14)),
        Cell(14, 2, listOf(5, 4, 13, 29, 30, 15)),
        Cell(15, 2, listOf(16, 5, 14, 30, 31, 32)),
        Cell(16, 2, listOf(17, 6, 5, 15, 32, 33)),
        Cell(17, 2, listOf(35, 18, 6, 16, 33, 34)),
        Cell(18, 2, listOf(36, 7, 1, 6, 17, 35)),
        Cell(19, 1, listOf(-1, -1, 20, 7, 36, -1)),
        Cell(20, 1, listOf(-1, -1, 21, 8, 7, 19)),
        Cell(21, 1, listOf(-1, -1, 22, 9, 8, 20)),
        Cell(22, 1, listOf(-1, -1, -1, 23, 9, 21)),
        Cell(23, 1, listOf(22, -1, -1, 24, 10, 9)),
        Cell(24, 0, listOf(23, -1, -1, 25, 11, 10)),
        Cell(25, 1, listOf(24, -1, -1, -1, 26, 11)),
        Cell(26, 1, listOf(11, 25, -1, -1, 27, 12)),
        Cell(27, 1, listOf(12, 26, -1, -1, 28, 13)),
        Cell(28, 1, listOf(13, 27, -1, -1, -1, 29)),
        Cell(29, 1, listOf(14, 13, 28, -1, -1, 30)),
        Cell(30, 1, listOf(15, 14, 29, -1, -1, 31)),
        Cell(31, 1, listOf(32, 15, 30, -1, -1, -1)),
        Cell(32, 1, listOf(33, 16, 15, 31, -1, -1)),
        Cell(33, 0, listOf(34, 17, 16, 32, -1, -1)),
        Cell(34, 1, listOf(-1, 35, 17, 33, -1, -1)),
        Cell(35, 1, listOf(-1, 36, 18, 17, 34, -1)),
        Cell(36, 1, listOf(-1, 19, 7, 18, 35, -1)),
    )
    repeat(BOARD_SIZE) { Board.initNeigh(it) }


    return Game().apply {
        day = 1
        sun = 2
        score = 0
        oppSun = 2
        oppScore = 0
        oppIsWaiting = false
        nutrients = 20
        trees[23].apply {
            isDormant = false
            owner = ME
            size = LITTLE
        }
        trees[19].apply {
            isDormant = false
            owner = ME
            size = LITTLE
        }

        trees[28].apply {
            isDormant = false
            owner = OPP
            size = LITTLE
        }
        trees[32].apply {
            isDormant = false
            owner = OPP
            size = LITTLE
        }
    }


}

