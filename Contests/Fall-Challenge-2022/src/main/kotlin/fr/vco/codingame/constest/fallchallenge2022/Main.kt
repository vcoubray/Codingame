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
        board.updateBoard(input)

        val tiles = board.grid.flatten()
        val myRobots = tiles.filter { it.owner == Owner.ME && it.units > 0 }


        val actions = mutableListOf<Action>()
        // Moves
        actions.addAll(myRobots.mapNotNull { origin ->
            board.findNearestTargetTile(origin)?.let { target -> Action.Move(origin.units, origin.pos, target.pos) }
        })

//        // Builds
//        if (board.tiles.count { it.owner == Owner.ME && it.recycler } < 1 && myMatter >= 10) {
//            actions.add(Action.Build(board.tiles.filter { it.canBuild }.random()))
//            myMatter -= 10
//        }

        //Spawns
        actions.addAll(List(myMatter / 10) {
            tiles.filter { it.canSpawn }.random().let { Action.Spawn(1, it.pos) }
        })

        println(actions.joinToString(";"))
    }
}

sealed interface Action {

    class Move(val n: Int, val source: Position, val target: Position) : Action {
        override fun toString() = "MOVE $n $source $target"
    }

    class Spawn(val n: Int, val target: Position) : Action {
        override fun toString() = "SPAWN $n $target"
    }

    class Build(val target: Position) : Action {
        override fun toString() = "BUILD $target}"
    }

    object Wait : Action {
        override fun toString() = "WAIT"
    }

    class Message(val message: String) : Action {
        override fun toString() = "MESSAGE $message"
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
    val pos: Position,
    var scrapAmount: Int = 0,
    var owner: Owner = Owner.NEUTRAL,
    var units: Int = 0,
    var recycler: Boolean = false,
    var canBuild: Boolean = false,
    var canSpawn: Boolean = false,
    var inRangeOfRecycler: Boolean = false,
) {
    fun dist(tile: Tile) = pos.dist(tile.pos)
}

data class Position(val x: Int, val y: Int) {
    fun dist(pos: Position) = (x - pos.x).absoluteValue + (y - pos.y).absoluteValue
    override fun toString() = "$x $y"
}

class Board(val height: Int, val width: Int) {

    val grid = List(height) { y -> List(width) { x -> Tile(Position(x,y)) } }

    fun updateBoard(input: Scanner) {
        repeat(height) { y ->
            repeat(width) { x ->
                grid[y][x].apply {
                    scrapAmount = input.nextInt()
                    owner = Owner.fromInt(input.nextInt())
                    units = input.nextInt()
                    recycler = input.nextInt() == 1
                    canBuild = input.nextInt()  == 1
                    canSpawn = input.nextInt() == 1
                    inRangeOfRecycler = input.nextInt()  == 1
                }
            }
        }
    }

    fun findNearestTargetTile(origin: Tile): Tile? {
        return grid.flatten().filter { it.owner != Owner.ME && it.scrapAmount > 0 }.minByOrNull(origin::dist)
    }

}

