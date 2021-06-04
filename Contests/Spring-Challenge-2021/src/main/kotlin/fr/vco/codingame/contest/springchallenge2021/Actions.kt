package fr.vco.codingame.contest.springchallenge2021

sealed class Action(val player: Int, val cost: Int) {
    fun print(message: String = "") = println("$this $message")
}

class SeedAction(player: Int, val source: Int, val target: Int, cost: Int) : Action(player, cost) {
    constructor(player: Int, source: Tree, target: Cell, cost: Int = 0) : this(
        player,
        source.cellIndex,
        target.index,
        cost
    )
    override fun toString() = "SEED $source $target"
}

class GrowAction(player: Int, val treeId: Int, val size: Int, cost: Int) : Action(player, cost) {
    constructor (player: Int, tree: Tree, cost: Int = 0) : this(player, tree.cellIndex, tree.size, cost)

    override fun toString() = "GROW $treeId"
}

class CompleteAction(player: Int, val treeId: Int) : Action(player, COMPLETE_COST) {
    constructor(player: Int, tree: Tree) : this(player, tree.cellIndex)

    override fun toString() = "COMPLETE $treeId"
}

class WaitAction(player: Int) : Action(player, 0) {
    override fun toString() = "WAIT"
}
