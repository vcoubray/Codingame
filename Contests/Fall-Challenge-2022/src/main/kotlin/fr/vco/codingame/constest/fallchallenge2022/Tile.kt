package fr.vco.codingame.constest.fallchallenge2022

import kotlin.math.min

class Tile(
    val id: Int,
    val pos: Position,
    var scrapAmount: Int = 0,
    var owner: Owner = Owner.NEUTRAL,
    var units: Int = 0,
    var recycler: Boolean = false,
    var canBuild: Boolean = false,
    var canSpawn: Boolean = false,
    var inRangeOfRecycler: Boolean = false,


    ) {
    var neighbours: List<Tile> = emptyList()
    var shouldBeMine: Boolean = false
    var minDistMine: Int = 0
    var minDistOpp: Int = 0

    var turnBeforeDestroyed = Int.MAX_VALUE
    var scrapToRecycle: Int = 0
    var accessible = true
    var destroyedNeighboursCount = 0

    fun compute() {
        this.turnBeforeDestroyed = when {
            recycler -> scrapAmount
            inRangeOfRecycler && neighbours.any { it.recycler && it.scrapAmount >= scrapAmount } -> scrapAmount
            else -> Int.MAX_VALUE
        }

        this.accessible = !recycler && scrapAmount > 0 && !(inRangeOfRecycler && scrapAmount == 1)
        this.scrapToRecycle = scrapAmount + neighbours.sumOf { min(it.scrapAmount, scrapAmount) }
        this.destroyedNeighboursCount = neighbours.count { it.scrapAmount <= scrapAmount }

    }

    fun dist(tile: Tile) = pos.dist(tile.pos)
    fun canSpawn() = canSpawn && (!inRangeOfRecycler || scrapAmount > 2)

    fun turnBeforeDisappear() =
        if (inRangeOfRecycler && neighbours.any { it.recycler && it.scrapAmount >= scrapAmount }) scrapAmount
        else Int.MAX_VALUE

    fun canCross(turn: Int = 0): Boolean {
        return when {
            recycler -> false
            scrapAmount == 0 -> false
            turnBeforeDisappear() <= turn -> false
            else -> true
        }
    }
}
