package fr.vco.codingame.puzzles.voxcodei.episode2


class GameSimulator(
    val width: Int,
    val height: Int,
    val bombs: Int,
    val turns: Int,
    nodeCount: Int,
    val ranges: List<List<Int>>,
    val rounds: List<Round>
) {
    val nodeMask =  (1L shl nodeCount) - 1
    fun indexToPosition(index: Int) = Position(index % width, index / width)
}

class Round(
    val actions: List<Pair<Int, Long>>, // list of positions associate with the targeted nodes (inverse bit mask)
    val grid: List<Int>, // Grid
)