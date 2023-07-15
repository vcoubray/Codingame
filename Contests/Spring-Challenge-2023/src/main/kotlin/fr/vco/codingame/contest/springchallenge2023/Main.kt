package fr.vco.codingame.contest.springchallenge2023

import java.util.*

const val TYPE_EMPTY = 0
const val TYPE_EGGS = 1
const val TYPE_CRYSTAL = 2

fun log(message: Any?) = System.err.println(message?.toString() ?: "null")

object Game {
    var numberOfCells: Int = 0
    var cells: List<Cell> = emptyList()

    var totalCrystal: Int = 0
    var potentialAnts: Int = 0
    var remainingEggs: Int = 0
    var remainingCrystals: Int = 0
    var firstTurn: Boolean = true

    var paths: List<List<List<Int>>> = emptyList()
    var distances: List<List<Int>> = emptyList()
    var distancesBaseScore: List<Int> = emptyList()


    val me: Player = Player()
    val opp: Player = Player()

    fun init(input: Scanner) {
        numberOfCells = input.nextInt() // amount of hexagonal cells in this map
        val start = System.currentTimeMillis()

        /** Init Board **/
        val crystalQuantities = MutableList(numberOfCells) { 0 }
        val eggsQuantities = MutableList(numberOfCells) { 0 }

        cells = List(numberOfCells) {
            val type = input.nextInt()
            val initialResources = input.nextInt()
            when (type) {
                TYPE_EGGS -> eggsQuantities[it] = initialResources
                TYPE_CRYSTAL -> crystalQuantities[it] = initialResources
            }
            val neighbors = List(6) { input.nextInt() }.filter { nId -> nId >= 0 }
            Cell(it, neighbors, type, initialResources)
        }

        /** Compute path and distances **/
        paths = cells.computePaths()
        distances = paths.map { it.map { path -> path.size - 1 } }

        /** Init Players **/
        val numberOfBases = input.nextInt()
        me.init(input, numberOfBases, cells, distances)
        opp.init(input, numberOfBases, cells, distances)

        distancesBaseScore = cells.map { cell -> opp.distanceFromBases[cell.id] - me.distanceFromBases[cell.id] }

        /** Init Score max **/
        potentialAnts = cells.filter { it.type == TYPE_EGGS }.sumOf { it.resources } * numberOfBases
        totalCrystal = cells.filter { it.type == TYPE_CRYSTAL }.sumOf { it.resources }
        remainingEggs = eggsQuantities.sum()
        remainingCrystals = crystalQuantities.sum()

        log("Init in ${System.currentTimeMillis() - start}ms")
    }

    fun update(input: Scanner) {
        /** Update players score */
        me.score = input.nextInt()
        opp.score = input.nextInt()

        /** Update Cells **/
        var newMyAntsTotal = 0
        var newOppAntsTotal = 0
        cells.onEach {
            it.apply {
                resources = input.nextInt() // the current amount of eggs/crystals on this cell
                this.myAnts = input.nextInt() // the amount of your ants on this cell
                this.oppAnts = input.nextInt() // the amount of opponent ants on this cell
                newMyAntsTotal += this.myAnts
                newOppAntsTotal += this.oppAnts
                me.ants[it.id] = this.myAnts
                opp.ants[it.id] = this.oppAnts
            }
        }

        updatePathValue(opp)

        if (firstTurn) {
            potentialAnts += opp.totalAnts + me.totalAnts
        }

        val newAntsPerBase = newMyAntsTotal - me.totalAnts
        me.totalAnts = newMyAntsTotal
        opp.totalAnts = newOppAntsTotal
    }


    private fun updatePathValue(player: Player) {
        player.pathPower = MutableList(numberOfCells) { 0 }
        player.toVisit.clear()
        player.bases.forEach {
            player.pathPower[it] = player.ants[it]
            player.toVisit.add(it)
        }

        while (player.toVisit.isNotEmpty()) {
            val curr = player.toVisit.poll()
            if (player.ants[curr] < player.pathPower[curr]) {
                player.pathPower[curr] = player.ants[curr]
            }
            cells[curr].neighbors.filter { player.ants[it] > 0 && player.pathPower[it] == 0 }.forEach {
                player.pathPower[it] = player.ants[curr]
                player.toVisit.add(it)
            }
        }
    }

    fun needMoreAnts(): Boolean {
        return me.totalAnts < potentialAnts / 2 && remainingEggs > 0
    }
}


fun main() {
    val input = Scanner(System.`in`)

    Game.init(input)
    val pathGenerator = PathGenerator(Game.numberOfCells)

    // game loop
    while (true) {
        Game.update(input)
        val start = System.currentTimeMillis()

        val defaultBeacons = pathGenerator.generateClassicPath(Game.me)
        val actions = defaultBeacons.mapIndexedNotNull { i, it -> if (it > 0) beacon(i, it) else null }.toMutableList()
        actions.add(message("${System.currentTimeMillis() - start}ms"))
        println(actions.joinToString(";"))

        Game.firstTurn = false
    }
}


