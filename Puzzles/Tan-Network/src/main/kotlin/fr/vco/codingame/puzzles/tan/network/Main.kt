package fr.vco.codingame.puzzles.tan.network

import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

fun Double.toRadians() = this * PI / 180

data class Station(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val type: String,
) {
    val children = mutableListOf<Pair<String, Double>>()

    fun dist(station: Station): Double {
        val x = (station.lon - lon) * cos((lat + station.lat) / 2)
        val y = (station.lat - lat)
        return sqrt(x * x + y * y) * 6371
    }
}

data class Path(
    val dist: Double,
    val parent: String?,
)

fun Map<String, Station>.dijkstra(start: String, end: String): List<String> {
    val costComparator = compareBy<Pair<String, Path>> { it.second.dist }
    val toVisit = PriorityQueue(costComparator).apply { add(start to Path(0.0, null)) }
    val visited = mutableMapOf<String, Path>()
    while (toVisit.isNotEmpty()) {
        val (id, path) = toVisit.poll()
        val current = this[id]!!

        if ((visited[id]?.dist ?: Double.MAX_VALUE) > path.dist) {
            visited[id] = path
            current.children.forEach { (childId, childDist) ->
                toVisit.add(childId to Path(path.dist + childDist, id))
            }
        }
    }

    return getPath(end, visited)
}

fun Map<String, Station>.getPath(end: String, paths: Map<String, Path>): List<String> {
    if (paths.containsKey(end)) {
        val path = mutableListOf<String>()

        var currentId: String? = end
        while (currentId != null) {
            path.add(this[currentId]!!.name)
            currentId = paths[currentId]!!.parent
        }
        return path.reversed()
    }
    return listOf("IMPOSSIBLE")
}


fun main() {
    val input = Scanner(System.`in`)
    val startPoint = input.next()
    val endPoint = input.next()
    val stationCount = input.nextInt()
    if (input.hasNextLine()) {
        input.nextLine()
    }

    val stations = List(stationCount) {
        input.nextLine()
            .split(",")
            .filterNot { it.isEmpty() }
            .let { (id, name, lat, lon, type) ->
                Station(
                    id,
                    name.removeSurrounding("\""),
                    lat.toDouble().toRadians(),
                    lon.toDouble().toRadians(),
                    type
                )
            }
    }.associateBy { it.id }


    val linkCount = input.nextInt()
    if (input.hasNextLine()) {
        input.nextLine()
    }

    repeat(linkCount) {
        val (parent, child) = input.nextLine().split(" ").map { stations[it]!! }
        val dist = parent.dist(child)
        parent.children.add(child.id to dist)
    }

    stations
        .dijkstra(startPoint, endPoint)
        .forEach(::println)
}

