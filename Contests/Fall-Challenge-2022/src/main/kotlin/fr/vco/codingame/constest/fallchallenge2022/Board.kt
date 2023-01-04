package fr.vco.codingame.constest.fallchallenge2022

import java.util.*

class Board(val height: Int, val width: Int) {

    private val grid = List(height) { y -> List(width) { x -> Tile(y * width + x, Position(x, y)) } }
    val tiles: List<Tile> = grid.flatten()
    lateinit var oppTiles: List<Tile>
    lateinit var myTiles: List<Tile>

    private val directions = listOf(
        Position(0, -1),
        Position(0, 1),
        Position(-1, 0),
        Position(1, 0)
    )

    init {
        tiles.forEach { tile ->
            tile.neighbours = directions.map(tile.pos::plus).mapNotNull { grid.getOrNull(it.y)?.getOrNull(it.x) }
        }
    }


    operator fun get(position: Position) =  tiles[position.y * width + position.x]
    operator fun get(x: Int, y: Int) = tiles[y * width + x]
    operator fun get(i: Int) = tiles[i]

    fun update(input: Scanner) {
        repeat(height) { y ->
            repeat(width) { x ->
                this[x, y].apply {
                    scrapAmount = input.nextInt()
                    owner = Owner.fromInt(input.nextInt())
                    units = input.nextInt()
                    recycler = input.nextInt() == 1
                    canBuild = input.nextInt() == 1
                    canSpawn = input.nextInt() == 1
                    inRangeOfRecycler = input.nextInt() == 1
                }
            }
        }
        tiles.forEach{it.compute()}
        oppTiles = tiles.filter { it.owner == Owner.OPP }
        myTiles = tiles.filter { it.owner == Owner.ME }

    }
}