package fr.vco.codingame.contest.springchallenge2021.game

import fr.vco.codingame.contest.springchallenge2021.NONE

data class Tree(
    val cellIndex: Int,
    var size: Int = NONE,
    var owner: Int = -1,
    var isDormant: Boolean = false
)

fun List<Tree>.getBits() = this.fold(0L) { acc, tree -> acc.addTree(tree.cellIndex) }
