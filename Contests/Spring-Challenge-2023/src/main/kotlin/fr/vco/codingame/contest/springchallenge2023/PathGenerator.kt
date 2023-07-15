package fr.vco.codingame.contest.springchallenge2023

import java.util.*
import kotlin.math.max
import kotlin.math.min

class PathGenerator(cellCount: Int) {

    private val starts = mutableListOf<Int>()
    private val workingBeaconPath = MutableList(cellCount) { 0 }
    private val targets = mutableListOf<Int>()
    private var remAnts = 0

    fun generateClassicPath(player: Player): List<Int> {
        workingBeaconPath.map { 0 }
        starts.clear()
        starts.addAll(player.bases)

        val predicate = if (Game.needMoreAnts()) {
            { type: Int -> type == TYPE_EGGS }
        } else {
            { type: Int -> type != TYPE_EMPTY }
        }

        targets.clear()
        Game.cells.filter { predicate(it.type) && it.resources > 0 }
            .forEach { targets.add(it.id) }

        remAnts = player.totalAnts
        while (targets.isNotEmpty()) {
            val bestWays = starts.associateWith { s -> targets.minBy { Game.distances[s][it] } }
            val (sourceId, target) = bestWays.minBy { (s, t) -> Game.distances[s][t] }
            targets.remove(target)

            remAnts -= Game.distances[sourceId][target]
            if (remAnts < 0)
                break
            starts.add(target)
            Game.paths[sourceId][target].forEach {
                workingBeaconPath[it] = 1
            }
        }
        return workingBeaconPath
    }


    private var workingTargetId = 0
    fun generateRandomPath( player: Player): List<Int> {
        workingBeaconPath.map { 0 }
        starts.clear()
        starts.addAll(player.bases)

        targets.clear()
        Game.cells.filter{ it.resources > 0 }.forEach { targets.add(it.id) }
        remAnts = player.totalAnts

        while (targets.isNotEmpty()) {
            if (remAnts <= 0) break
            workingTargetId = targets.random()
            targets.remove(workingTargetId)

            if(workingBeaconPath[workingTargetId] > 0)
                continue

            val start = starts.minBy { Game.distances[it][workingTargetId] }
            val path = Game.paths[start][workingTargetId].filter { workingBeaconPath[it] == 0 }
            if (path.isEmpty() || path.size > remAnts) {
                continue
            }
            val maxPower = remAnts / path.size
            val power = if(workingBeaconPath[start] == 0) {
                (1..maxPower).random()
            } else {
                min(workingBeaconPath[start], (1..maxPower).random())
            }
            remAnts -= path.size * power

            starts.add(workingTargetId)
            path.forEach {
                workingBeaconPath[it] = power
            }
        }

        return workingBeaconPath
    }

    val toVisit = PriorityQueue<Int> { a, b -> workingBeaconPath[b] - workingBeaconPath[a] }

    fun computePathScore(beaconsPath: List<Int>): Double {
        //Update beacons path power with attack chain
        val pathPower = MutableList(Game.numberOfCells) { 0 }

        toVisit.clear()
        Game.me.bases.forEach {
            toVisit.add(it)
            pathPower[it] = beaconsPath[it]
        }

        while (toVisit.isNotEmpty()) {
            val curr = toVisit.poll()
            if (beaconsPath[curr] >= Game.opp.pathPower[curr]) {
                pathPower[curr] = beaconsPath[curr]
            } else {
                continue
            }

            Game.cells[curr].neighbors.filter { beaconsPath[it] > 0 }.forEach {
                toVisit.add(it)
            }
        }


        val antsList = Game.me.ants.mapIndexed { i, _ -> i }
            .filter { Game.me.ants[it] > 0 }

        // Compute cell score (base distances, EggNeeded, ant distance, remaining resource)
        val cellScores = Game.cells.map { cell ->
            val resources = min(cell.resources, pathPower[cell.id])
            val resourceScore = if (Game.needMoreAnts() && cell.type == TYPE_EGGS) resources * 3
            else resources

            val turnToReach = antsList.minOf { i -> max(1.0, Game.distances[i][cell.id].toDouble()) }

            resourceScore / turnToReach
        }

        return cellScores.sum()
    }



    val bestPath = MutableList(cellCount){0}
    val bestScore = 0.0
    fun findBestBeaconPath(timeout : Int) : List<Int>{
        val start = System.currentTimeMillis()

        generateClassicPath(Game.me)
        while (System.currentTimeMillis() - start < timeout) {
            generateRandomPath( Game.me)

        }

        return bestPath
    }
}