package fr.vco.codingame.contest.springchallenge2023

import java.util.*

class Player {
    var score: Int = 0
    var totalAnts: Int = 0
    var bases: List<Int> = emptyList()
    var ants: MutableList<Int> = mutableListOf()


    var distanceFromBases: List<Int> = emptyList()
    var pathValues: MutableList<Int> = mutableListOf()
    val toVisit = PriorityQueue<Int> { a, b -> pathValues[b] - pathValues[a] }


    fun init(input: Scanner, numberOfBases: Int, cells: List<Cell>, distances: List<List<Int>>) {
        bases = List(numberOfBases) { input.nextInt() }
        ants = MutableList(Game.numberOfCells) { 0 }
        distanceFromBases = cells.map{ cell -> bases.minOf{ base -> distances[base][cell.id]}}
    }
}