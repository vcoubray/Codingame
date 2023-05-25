package fr.vco.codingame.contest.springchallenge2023

import java.util.*
import kotlin.collections.ArrayDeque

fun log(message: Any?) = System.err.println(message?.toString()?: "null")

class Cell(
    val id: Int,
    val neighbors: List<Int>,
    val type: Int = 0,
    var resources: Int = 0,
    var myAnts: Int = 0,
    var oppAnts: Int = 0,
)

typealias Action = String

fun beacon(cellId: Int, strength: Int) = "BEACON $cellId $strength"
fun beacon(cell: Cell, strength: Int) = "BEACON ${cell.id} $strength"
fun line(sourceId: Int, targetId: Int, strength: Int) = "LINE $sourceId $targetId $strength"
fun line(source: Cell, target: Cell, strength: Int) = "LINE ${source.id} ${target.id} $strength"
fun wait() = "WAIT"
fun message(message: String) = "MESSAGE $message"

fun List<Cell>.computeDistances() = List(this.size) { distancesFrom(it) }

fun List<Cell>.distancesFrom(sourceId: Int): List<Int> {
    val distances = this.map { -1 }.toMutableList()
    distances[sourceId] = 0
    val toVisit = ArrayDeque<Int>().apply { add(sourceId) }

    while (toVisit.isNotEmpty()) {
        val curr = toVisit.removeFirst()
        this[curr].neighbors.filter { distances[it] == -1 }.forEach {
            distances[it] = distances[curr] + 1
            toVisit.add(it)
        }
    }
    return distances
}

fun main() {
    val input = Scanner(System.`in`)

    val numberOfCells = input.nextInt() // amount of hexagonal cells in this map
    val cells = List(numberOfCells) {
        val type = input.nextInt() // 0 for empty, 1 for eggs, 2 for crystal
        val initialResources = input.nextInt() // the initial amount of eggs/crystals on this cell
        val neighbors = List(6) { input.nextInt() }.filter { nId -> nId >= 0 }
        Cell(it, neighbors, type, initialResources)
    }

    val distances = cells.computeDistances()

    val numberOfBases = input.nextInt()
    val myBases = List(numberOfBases) { input.nextInt() }
    val oppBases = List(numberOfBases) { input.nextInt() }

    // game loop
    while (true) {

        var myAntsTotal = 0
        cells.onEach {
            it.apply {
                resources = input.nextInt() // the current amount of eggs/crystals on this cell
                myAnts = input.nextInt() // the amount of your ants on this cell
                oppAnts = input.nextInt() // the amount of opponent ants on this cell

                myAntsTotal += myAnts
            }
        }

        val actions = mutableListOf<String>()

        val starts = myBases.toMutableList()
        val targets =  cells.filterNot { it.type == 0 || it.resources == 0 }.toMutableList()

        while(targets.isNotEmpty()) {
            val bestWays = starts.associateWith { s -> targets.minBy{distances[s][it.id]}}
            val (sourceId, target) = bestWays.minBy{(s,t) -> distances[s][t.id]}
            targets.remove(target)

            myAntsTotal -= distances[sourceId][target.id]
            if (myAntsTotal < 0)
                break
            starts.add(target.id)
            actions.add(line(sourceId, target.id,1))
        }

        println(actions.joinToString(";"))
    }
}


