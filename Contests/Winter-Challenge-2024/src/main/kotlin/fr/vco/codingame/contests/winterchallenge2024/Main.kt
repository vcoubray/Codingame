package fr.vco.codingame.contests.winterchallenge2024

import kotlin.math.abs

fun readInt() = readln().toInt()
fun readInts() = readln().split(" ").map { it.toInt() }

data class Position(var x: Int, var y: Int) {
    operator fun plus(pos: Position) = Position(x + pos.x, y + pos.y)
    fun distance(pos: Position) = abs(x - pos.x) + abs(y - pos.y)
}

data class Stock(
    var a: Int = 0,
    var b: Int = 0,
    var c: Int = 0,
    var d: Int = 0
) {
    fun update() {
        val (a, b, c, d) = readInts()
        this.a = a
        this.b = b
        this.c = c
        this.d = d
    }
}

val DIRECTIONS = mapOf(
    "S" to Position(0, 1),
    "N" to Position(0, -1),
    "E" to Position(1, 0),
    "W" to Position(-1, 0),
)

data class Cell(
    val position: Position,
    var organId: Int = 0, // id of this entity if it's an organ, 0 otherwise
    var type: String = "", // WALL, ROOT, BASIC, TENTACLE, HARVESTER, SPORER, A, B, C, D
    var owner: Int = -1, // 1 if your organ, 0 if enemy organ, -1 if neither
    var organDir: String = "X", // N,E,S,W or X if not an organ
    var organParentId: Int = 0,
    var organRootId: Int = 0,
)

class Board(val width: Int, val height: Int) {

    val grid = List(height) { y -> List(width) { x -> Cell(Position(x, y)) } }.flatten()

    val myStock = Stock()
    val oppStock = Stock()

    fun update() {
        val entityCount = readInt()
        repeat(entityCount) {
            val entity = readln().split(" ")
            val (x, y) = entity.take(2).map { it.toInt() }

            val cell = grid[positionToCellId(x, y)]
            val (type, owner, organId, organDir, organParentId, organRootId) = entity.drop(2)
            cell.type = type
            cell.owner = owner.toInt()
            cell.organId = organId.toInt()
            cell.organDir = organDir
            cell.organParentId = organParentId.toInt()
            cell.organRootId = organRootId.toInt()
        }

        myStock.update()
        oppStock.update()
    }

    fun cellIdToPosition(id: Int) = Position(id % width, id / width)
    fun positionToCellId(pos: Position) = positionToCellId(pos.x, pos.y)
    fun positionToCellId(x: Int, y: Int) = y * width + x

    fun getCell(position: Position) = grid[positionToCellId(position)]

    fun getValidNeighbour(position: Position) =
        DIRECTIONS.values.map { it + position }
            .filter { it.x in 0 until width && it.y in 0 until height }
            .filterNot { getCell(it).type == "WALL" }

}

private operator fun <E> List<E>.component6(): E {
    return this[5]
}


fun main() {

    val (width, height) = readInts()

    val board = Board(width, height)


    // game loop
    while (true) {
        board.update()
        val requiredActionsCount = readInt() // your number of organisms, output an action for each one in any order

        val myCells = board.grid.filter { it.owner == 1 }


        for (i in 0 until requiredActionsCount) {

            if (board.myStock.a > 0) {

                val proteins = board.grid.filter { it.type == "A" }

                val harvestableProtein = proteins.filter { cell ->
                    val neighbour = board.getValidNeighbour(cell.position)
                    neighbour.none { board.getCell(it).type == "HARVESTER" && board.getCell(it).owner == 1 } &&
                            neighbour.any { board.getCell(it).type == "" }
                }
                System.err.println("harvestable Protein: $harvestableProtein")

                val harvesterTargets = harvestableProtein.flatMap { board.getValidNeighbour(it.position) }
                    .onEach { System.err.println(board.getCell(it)) }
                    .filter { board.getCell(it).owner == -1 }

                System.err.println("harvester Target: $harvesterTargets")
                val alreadyHarvested = proteins.filter { cell ->
                    val neighbour = board.getValidNeighbour(cell.position)
                    neighbour.any { board.getCell(it).type == "HARVESTER" && board.getCell(it).owner == 1 }
                }
                System.err.println(alreadyHarvested)
                System.err.println("Already harvested : $alreadyHarvested")

                val extends =
                    myCells.flatMap { cell -> board.getValidNeighbour(cell.position).map { cell.organId to it } }
                        .filterNot { (_, target) -> target in alreadyHarvested.map { it.position } }
                        .filter { (_, target) -> board.getCell(target).type == "A" || board.getCell(target).type == "" }
                System.err.println("extends: $extends")
                if (harvesterTargets.isNotEmpty()) {

                    val (id, dest) = extends.minBy { (_, extend) ->
                        harvesterTargets.minOf { target -> target.distance(extend) }
                    }
                    if (dest in harvesterTargets) {
                        val (dir, _) = DIRECTIONS.toList().first{(_, dir) -> board.getCell(dir + dest).type == "A" }

                        println("GROW $id ${dest.x} ${dest.y} HARVESTER $dir")
                    } else {
                        println("GROW $id ${dest.x} ${dest.y} BASIC")
                    }
                } else {
                    val (id, dest) = extends.first()
                    println("GROW $id ${dest.x} ${dest.y} BASIC")
                }

            } else {
                println("WAIT")
            }
        }
    }
}