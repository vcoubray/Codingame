package fr.vco.codingame.contest.tictactoe.mcts

import fr.vco.codingame.contest.tictactoe.game.Action

class Node(
    val parent: Node?,
    var action: Action?
) {
    var children: List<Node> = emptyList()
    var win: Float = 0f
    var visit: Int = 0

    fun getRandomChild() = children.random()
}
