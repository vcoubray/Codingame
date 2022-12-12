package fr.vco.codingame.constest.fallchallenge2022

import java.util.*
import kotlin.math.absoluteValue

fun log(message: Any?) = System.err.println(message)

fun main() {
    val input = Scanner(System.`in`)
    val width = input.nextInt()
    val height = input.nextInt()
    val board = Board(height, width)

    while (true) {
        var myMatter = input.nextInt()
        val oppMatter = input.nextInt()
        board.updateTiles(input)

        val myRobots = board.tiles.filter { it.owner == Owner.ME && it.units > 0 }


        val actions = mutableListOf<Action>()
        // Moves
        actions.addAll(myRobots.mapNotNull { origin ->
            board.findNearestTargetTile(origin)?.let { target -> Action.Move(origin.units, origin, target) }
        })

//        // Builds
//        if (board.tiles.count { it.owner == Owner.ME && it.recycler } < 1 && myMatter >= 10) {
//            actions.add(Action.Build(board.tiles.filter { it.canBuild }.random()))
//            myMatter -= 10
//        }

        //Spawns
        actions.addAll(List(myMatter / 10) { board.tiles.filter { it.canSpawn }.random().let { Action.Spawn(1, it) } })

        println(actions.joinToString(";"))
    }
}

sealed interface Action {

    class Move(val n: Int, val source: Tile, val target: Tile) : Action {
        override fun toString() = "MOVE $n ${source.x} ${source.y} ${target.x} ${target.y}"
    }

    class Spawn(val n: Int, val target: Tile) : Action {
        override fun toString() = "SPAWN $n ${target.x} ${target.y}"
    }

    class Build(val target: Tile) : Action {
        override fun toString() = "BUILD ${target.x} ${target.y}"
    }

    object Wait : Action {
        override fun toString() = "WAIT"
    }

    class Message : Action {
        override fun toString() = "MESSAGE "
    }
}

enum class Owner {
    ME, OPP, NEUTRAL;

    companion object {
        fun fromInt(value: Int) = when (value) {
            1 -> ME
            0 -> OPP
            -1 -> NEUTRAL
            else -> throw Exception("oh no")
        }
    }
}

class Tile(
    val x: Int,
    val y: Int,
    val scrapAmount: Int,
    val owner: Owner,
    val units: Int,
    val recycler: Boolean,
    val canBuild: Boolean,
    val canSpawn: Boolean,
    val inRangeOfRecycler: Boolean
) {
    fun dist(tile: Tile) = (x - tile.x).absoluteValue + (y - tile.y).absoluteValue

}


class Board(val height: Int, val width: Int) {

    lateinit var tiles: MutableList<Tile>

    fun updateTiles(input: Scanner) {
        tiles = mutableListOf()
        repeat(height) { y ->
            repeat(width) { x ->
                val scrapAmount = input.nextInt()
                val owner = input.nextInt()
                val units = input.nextInt()
                val recycler = input.nextInt()
                val canBuild = input.nextInt()
                val canSpawn = input.nextInt()
                val inRangeOfRecycler = input.nextInt()

                val tile = Tile(
                    x,
                    y,
                    scrapAmount,
                    Owner.fromInt(owner),
                    units,
                    recycler == 1,
                    canBuild == 1,
                    canSpawn == 1,
                    inRangeOfRecycler == 1
                )
                this.tiles.add(tile)
            }
        }
    }

    fun findNearestTargetTile(origin: Tile): Tile? {
        return tiles.filter { it.owner != Owner.ME && it.scrapAmount > 0 }.minByOrNull(origin::dist)
    }

}

