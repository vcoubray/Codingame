package fr.vco.codingame.constest.fallchallenge2022

import java.util.*
import kotlin.collections.ArrayDeque

data class BfsNode(var visitedCount: Int, var from: Int, var depth: Int)

class BFS (private val tiles: List<Tile>){

    private val toVisit = ArrayDeque<Int>()
    private val visited = List(tiles.size) { BfsNode(0, -1, 0) }
    private var currentVisit = 0


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


    private fun getPath(origin: Int, target: Int): List<Int> {
        val path = LinkedList<Int>()
        var current = target
        while (current != origin && current != -1) {
            path.addFirst(current)
            current = visited[current].from
        }
        path.add(origin)
        return path
    }


}