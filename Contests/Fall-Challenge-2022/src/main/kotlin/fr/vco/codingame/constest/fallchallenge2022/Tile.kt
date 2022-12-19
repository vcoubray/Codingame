package fr.vco.codingame.constest.fallchallenge2022

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
    var neighbours: List<Tile> = emptyList(),
) {
    fun dist(tile: Tile) = pos.dist(tile.pos)

    fun recyclerScore() =
        if (scrapAmount <= 5 || inRangeOfRecycler || neighbours.any { it.inRangeOfRecycler }) 0
        else scrapAmount + neighbours.sumOf { it.scrapAmount }
}