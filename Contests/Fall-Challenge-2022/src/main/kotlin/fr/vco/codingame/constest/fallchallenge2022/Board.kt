package fr.vco.codingame.constest.fallchallenge2022

import java.util.*
import kotlin.collections.ArrayDeque

class Board(val height: Int, val width: Int) {

    var turn = 0
    val grid = List(height) { y -> List(width) { x -> Tile(y * width + x, Position(x, y)) } }
    val tiles = grid.flatten()

    lateinit var oppTiles: List<Tile>
    lateinit var myTiles: List<Tile>

    val recyclerTargets = mutableListOf<Int>()

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
        myTiles = tiles.filter { it.owner == Owner.ME }

        if (turn == 0) {
            val myOrigin = myTiles.first { it.units == 0 }
            val oppOrigin = oppTiles.first { it.units == 0 }
            bfs(myOrigin.id) { current, bfsNode -> tiles[current].minDistMine = bfsNode.depth }
            bfs(oppOrigin.id) { current, bfsNode ->
                tiles[current].minDistOpp = bfsNode.depth
                tiles[current].shouldBeMine = tiles[current].minDistMine <= tiles[current].minDistOpp
            }

            val totalTiles = tiles.count { it.scrapAmount > 0 }
            val maxRecycler = totalTiles / 40
            log("maxRecycler : $maxRecycler")

            val availableRecyclers = tiles.filter { it.shouldBeMine }.sortedByDescending { it.recyclerScore() }.toMutableList()
            while (recyclerTargets.size < maxRecycler && availableRecyclers.isNotEmpty()) {
                val possibleRecycler = availableRecyclers.removeFirst()
                if ( recyclerTargets.none{ possibleRecycler.pos.dist(tiles[it].pos) < 3} ) {
                    recyclerTargets.add(possibleRecycler.id)
                }
            }

            recyclerTargets.forEach{log(tiles[it].pos)}


        }

        turn++
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
            if(currentId in recyclerTargets) zone.recyclerTargets.add(current)
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

    data class BfsNode(var visitedCount: Int, var from: Int, var depth: Int)

    private val toVisit = ArrayDeque<Int>()
    private val visited = List(tiles.size) { BfsNode(0, -1, 0) }
    private var currentVisit = 0

    fun searchPath(origin: Int, target: Int): List<Int> {
        return searchPath(origin) { it == target }
    }

    fun searchPath(origin: Int, target: (Int) -> Boolean): List<Int> {
        currentVisit++
        toVisit.clear()
        toVisit.addFirst(origin)

        visited[origin].depth = 0
        while (toVisit.isNotEmpty()) {
            val currentId = toVisit.removeFirst()
            if (target(currentId)) return getPath(origin, currentId)
            if (visited[currentId].visitedCount >= currentVisit) continue
            visited[currentId].visitedCount = currentVisit
            val current = tiles[currentId]
            current.neighbours.forEach { neighbor ->
                if (visited[neighbor.id].visitedCount < currentVisit
                    && neighbor.canCross(visited[currentId].depth + 1)
                ) {
                    visited[neighbor.id].from = currentId
                    visited[neighbor.id].depth = visited[currentId].depth + 1
                    toVisit.add(neighbor.id)
                }
            }
        }
        return emptyList()
    }

    fun bfs(origin: Int, action: (current: Int, bfsNode: BfsNode) -> Unit) {
        currentVisit++
        toVisit.clear()
        toVisit.addFirst(origin)

        visited[origin].depth = 0
        while (toVisit.isNotEmpty()) {
            val currentId = toVisit.removeFirst()
            if (visited[currentId].visitedCount >= currentVisit) continue
            visited[currentId].visitedCount = currentVisit
            action(currentId, visited[currentId])
            tiles[currentId].neighbours.forEach { neighbor ->
                if (visited[neighbor.id].visitedCount < currentVisit
                    && neighbor.scrapAmount > 0
                ) {
                    visited[neighbor.id].from = currentId
                    visited[neighbor.id].depth = visited[currentId].depth + 1
                    toVisit.add(neighbor.id)
                }
            }
        }
    }

    fun bfs2(origin: Int) {
        currentVisit++
        toVisit.clear()
        toVisit.addFirst(origin)

        visited[origin].depth = 0
        tiles[origin].minDistMine = -1
        tiles[origin].minDistOpp = -1
        while (toVisit.isNotEmpty()) {
            val currentId = toVisit.removeFirst()
            if (visited[currentId].visitedCount >= currentVisit) continue
            visited[currentId].visitedCount = currentVisit
            if (tiles[origin].minDistMine == -1 && tiles[currentId].owner == Owner.ME) {
                tiles[origin].minDistMine = visited[currentId].depth
            }
            if (tiles[origin].minDistOpp == -1 && tiles[currentId].owner == Owner.OPP) {
                tiles[origin].minDistOpp = visited[currentId].depth
            }
            if (tiles[origin].minDistOpp >= 0 && tiles[origin].minDistMine >= 0) {
                return
            }
            tiles[currentId].neighbours.forEach { neighbor ->
                if (visited[neighbor.id].visitedCount < currentVisit
                    && neighbor.scrapAmount > 0
                ) {
                    visited[neighbor.id].from = currentId
                    visited[neighbor.id].depth = visited[currentId].depth + 1
                    toVisit.add(neighbor.id)
                }
            }
        }
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
