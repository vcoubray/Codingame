package fr.vco.codingame.contest.springchallenge2023

import kotlin.math.min


class Cell(
    val id: Int,
    val neighbors: List<Int>,
    val type: Int = 0,
    var resources: Int = 0,
    var myAnts: Int = 0,
    var oppAnts: Int = 0,
)


fun List<Cell>.computePaths() = List(this.size) { pathFrom(it) }

fun List<Cell>.pathFrom(sourceId: Int): List<List<Int>> {
    val paths = this.map { mutableListOf<Int>() }
    paths[sourceId].add(sourceId)

    val toVisit = ArrayDeque<Int>().apply { add(sourceId) }
    while (toVisit.isNotEmpty()) {
        val curr = toVisit.removeFirst()
        this[curr].neighbors.filter { paths[it].isEmpty() }.forEach {
            paths[it] += paths[curr] + it
            toVisit.add(it)
        }
    }
    return paths
}

fun List<Cell>.generateClassicPath(bases: List<Int>, antsCount: Int): List<Int> {
    val beacons = MutableList(size) { 0 }
    val starts = bases.toMutableList()

    val predicate = if (Game.needMoreAnts()) {
        { type: Int -> type == TYPE_EGGS }
    } else {
        { type: Int -> type != TYPE_EMPTY }
    }
    val targets = Game.cells.filter { predicate(it.type) && it.resources > 0 }.toMutableList()

    var remAnts = antsCount
    while (targets.isNotEmpty()) {
        val bestWays = starts.associateWith { s -> targets.minBy { Game.distances[s][it.id] } }
        val (sourceId, target) = bestWays.minBy { (s, t) -> Game.distances[s][t.id] }
        targets.remove(target)

        remAnts -= Game.distances[sourceId][target.id] * 2
        if (remAnts < 0)
            break
        starts.add(target.id)
        Game.paths[sourceId][target.id].forEach {
            beacons[it] = 1
        }
    }
    return beacons
}


fun List<Cell>.generateRandomPath(bases: List<Int>, antsCount: Int): List<Int> {
    val beacons = MutableList(size) { 0 }
    val starts = bases.toMutableList()
    val targets = Game.cells.filter{ it.resources > 0 }.toMutableList()

    var remAnts = antsCount
    while (targets.isNotEmpty()) {
        if (remAnts <= 0) break
        val target = targets.random()
        targets.remove(target)

        if(beacons[target.id] > 0)
            continue

        val start = starts.minBy { Game.distances[it][target.id] }
        val path = Game.paths[start][target.id].filter { beacons[it] == 0 }
        if (path.isEmpty() || path.size > remAnts) {
            continue
        }
        val maxPower = remAnts / path.size
        val power = if(beacons[start] == 0) {
            (1..maxPower).random()
        } else {
            min(beacons[start], (1..maxPower).random())
        }
        remAnts -= path.size * power

        starts.add(target.id)
        path.forEach {
            beacons[it] = power
        }
    }

    return beacons
}