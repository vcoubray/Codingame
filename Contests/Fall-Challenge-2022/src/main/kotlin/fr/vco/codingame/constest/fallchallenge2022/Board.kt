package fr.vco.codingame.constest.fallchallenge2022

import java.util.*
import kotlin.collections.ArrayDeque

class Board(val height: Int, val width: Int) {

    val grid = List(height) { y -> List(width) { x -> Tile(y * width + x, Position(x, y)) } }
    val tiles = grid.flatten()

    lateinit var oppTiles: List<Tile>


    private val directions = listOf(
        Position(0, -1),
        Position(0, 1),
        Position(-1, 0),
        Position(1, 0)
    )

    init {
        tiles.forEach { tile ->
            tile.neighbours = directions.map(tile.pos::plus).mapNotNull(::get)
        }
    }

    operator fun get(position: Position) = grid.getOrNull(position.y)?.getOrNull(position.x)

    fun updateBoard(input: Scanner) {
        repeat(height) { y ->
            repeat(width) { x ->
                grid[y][x].apply {
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
        oppTiles = tiles.filter { it.owner == Owner.OPP }
    }

    fun computeZones(): List<Zone> {
        val zones = mutableListOf<Zone>()
        val visited = mutableListOf<Int>()

        tiles.forEach {
            if (!it.recycler && it.scrapAmount > 0 && it.id !in visited) {
                zones.add(getZone(it.id, visited))
            }
        }

        return zones
    }

    fun getZone(tileId: Int, visited: MutableList<Int>): Zone {
        val zone = Zone()
        val toVisit = ArrayDeque<Int>().apply { add(tileId) }
        while (toVisit.isNotEmpty()) {
            val currentId = toVisit.removeFirst()
            if (currentId in visited) continue
            val current = tiles[currentId]
            zone.addTile(current)
            visited.add(currentId)
            current.neighbours.forEach { neighbor ->
                if (!neighbor.recycler && neighbor.scrapAmount > 0 && neighbor.id !in visited) {
                    toVisit.addLast(neighbor.id)
                }
            }
        }
        return zone
    }

    data class BfsNode(var visitedCount: Int, var from: Int)

    private val visited = List(tiles.size) { BfsNode(0, -1) }
    private var currentVisit = 0

    fun searchPath(origin: Int, target: Int): List<Int> {
        return searchPath(origin) { it == target }
    }


    fun searchPath(origin: Int, target: (Int) -> Boolean): List<Int> {
        currentVisit++
        val toVisit = ArrayDeque<Int>().apply { addFirst(origin) }

        while (toVisit.isNotEmpty()) {
            val currentId = toVisit.removeFirst()
            if (target(currentId)) return getPath(origin, currentId)
            if (visited[currentId].visitedCount >= currentVisit) continue
            visited[currentId].visitedCount = currentVisit
            val current = tiles[currentId]
            current.neighbours.forEach { neighbor ->
                if (visited[neighbor.id].visitedCount < currentVisit && neighbor.scrapAmount > 0 && !neighbor.recycler) {
                    visited[neighbor.id].from = currentId
                    toVisit.add(neighbor.id)
                }
            }
        }
        return emptyList()
    }


    fun getPath(origin: Int, target: Int): List<Int> {
        val path = LinkedList<Int>()
        var current = target
        while (current != origin && current != -1) {
            path.addFirst(current)
            current = visited[current].from
        }
        return path
    }
}