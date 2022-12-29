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
    var minDistMine : Int = 0
    var minDistOpp: Int = 0

    fun dist(tile: Tile) = pos.dist(tile.pos)
    fun canSpawn() = canSpawn && (!inRangeOfRecycler || scrapAmount > 2)

    fun recyclerScore() =
        if (scrapAmount <= 5 || inRangeOfRecycler || neighbours.any { it.inRangeOfRecycler }) 0
        else scrapToRecycle()

    fun scrapToRecycle() = scrapAmount + neighbours.sumOf{ min(it.scrapAmount, scrapAmount) }

    fun turnBeforeDisappear() = if (inRangeOfRecycler && neighbours.any{it.recycler && it.scrapAmount >= scrapAmount}) scrapAmount
        else Int.MAX_VALUE

    fun canCross(turn: Int = 0) : Boolean {
        return when {
            recycler -> false
            scrapAmount == 0 -> false
            turnBeforeDisappear() <= turn -> false
            else -> true
        }
    }
}
