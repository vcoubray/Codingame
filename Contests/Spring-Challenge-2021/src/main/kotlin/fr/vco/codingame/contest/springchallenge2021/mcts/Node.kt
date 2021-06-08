package fr.vco.codingame.contest.springchallenge2021.mcts

import fr.vco.codingame.contest.springchallenge2021.game.Action

class Node(
    val parent: Node?,
    var action: Action
) {
    var children: List<Node> = emptyList()
    var win: Float = 0f
    var visit: Int = 0

    fun getRandomChild() = children.random()
}
