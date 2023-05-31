package fr.vco.codingame.contest.springchallenge2023

import java.util.*
import kotlin.math.max
import kotlin.math.min

const val TYPE_EMPTY = 0
const val TYPE_EGGS = 1
const val TYPE_CRYSTAL = 2

fun log(message: Any?) = System.err.println(message?.toString() ?: "null")

data class Gain(val eggs: Double, val crystals: Double)

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
        me.score = input.nextInt()
        opp.score = input.nextInt()

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

        if (firstTurn) {
            potentialAnts += opp.totalAnts + me.totalAnts
        }

        val newAntsPerBase = newMyAntsTotal - me.totalAnts
        me.totalAnts = newMyAntsTotal
        opp.totalAnts = newOppAntsTotal

    }


    fun computePlayerGains(player: Player, beacons: List<Int>): Gain {
        // compute Path score (before Eggs)
        player.pathValues = MutableList(numberOfCells) { 0 }
        player.toVisit.clear()
        player.bases.forEach {
            player.pathValues[it] = beacons[it]
            player.toVisit.add(it)
        }

        while (player.toVisit.isNotEmpty()) {
            val curr = player.toVisit.poll()
            if (beacons[curr] < player.pathValues[curr]) {
                player.pathValues[curr] = beacons[curr]
            }
            cells[curr].neighbors.filter { beacons[curr] > 0 && player.pathValues[it] == 0 }.forEach {
                player.pathValues[it] = beacons[curr]
                player.toVisit.add(it)
            }
        }

        val antsList = player.ants.mapIndexed { i, ant -> i to ant }
            .filter { (_, ant) -> ant > 0 }
        val turnToReach = cells.map { antsList.minOf { (i, _) -> max(1.0, distances[i][it.id].toDouble()) } }

        // compute Gained Eggs
        val gainedEggs = cells.mapIndexed { i, it -> i to it }
            .filter { (i, it) -> it.type == TYPE_EGGS && cells[i].resources > 0 }
            .sumOf { (i, it) -> min(player.pathValues[i], it.resources) / turnToReach[i] }


        // compute gained crystals
        val gainedCrystals = cells.mapIndexed { i, it -> i to it }
            .filter { (i, it) -> it.type == TYPE_CRYSTAL && cells[i].resources > 0 }
            .sumOf { (i, it) -> min(player.pathValues[i], it.resources) / turnToReach[i] }

        return Gain(gainedEggs * player.bases.size, gainedCrystals)
    }

    fun computePathScore(beaconsPath: List<Int>): Double {
        // Compute paths Power opp (current path)
        // Compute paths Power for me (beaconList)
        // Compute cell score (base distances, EggNeeded, ant distance, remaining resource)
        // Compute attack Chain
        // Compute eggs Gained (bonus if eggNeeded, Bonus if disputed Cell)
        // Compute Crystal Gained (Bonus if near of My ant, bonus if disputed Cell)

        return 0.0
    }

    fun updatePathValue(player: Player) {
        player.pathValues = MutableList(numberOfCells) { -1 }
        player.toVisit.clear()
        player.bases.forEach {
            player.pathValues[it] = player.ants[it]
            player.toVisit.add(it)
        }

        while (player.toVisit.isNotEmpty()) {
            val curr = player.toVisit.poll()
            if (player.ants[curr] < player.pathValues[curr]) {
                player.pathValues[curr] = player.ants[curr]
            }
            cells[curr].neighbors.filter { player.ants[it] > 0 && player.pathValues[it] == -1 }.forEach {
                player.pathValues[it] = player.ants[curr]
                player.toVisit.add(it)
            }
        }
    }

    fun needMoreAnts(): Boolean {
        return me.totalAnts < potentialAnts / 2 && remainingEggs > 0
    }

}


fun Gain.isBetter(gain: Gain): Boolean {
    return if (Game.needMoreAnts()) {
        (eggs > gain.eggs) ||
            eggs == gain.eggs && crystals > gain.crystals
    } else {
        (crystals > gain.crystals) ||
            crystals == gain.crystals && eggs > gain.eggs
    }
}

fun main() {
    val input = Scanner(System.`in`)

    Game.init(input)



    // game loop
    while (true) {
        // Update Board
        Game.update(input)
        val start = System.currentTimeMillis()
        var beacons = List(Game.numberOfCells) { 0 }
        var gain = Game.computePlayerGains(Game.me, beacons)
        val defaultBeacons = Game.cells.generateClassicPath(Game.me.bases, Game.me.totalAnts)
        val defaultGain = Game.computePlayerGains(Game.me, defaultBeacons)
        if (defaultGain.isBetter(gain)) {
            beacons = defaultBeacons
            gain = defaultGain
        }
        log("Default Gain : $defaultGain")
        repeat(250) {
            val randomBeacon = Game.cells.generateRandomPath(Game.me.bases, Game.me.totalAnts)
            val randomGain = Game.computePlayerGains(Game.me, randomBeacon)
            if (randomGain.isBetter(gain)) {
                beacons = randomBeacon
                gain = randomGain
            }
        }

        val actions = beacons.mapIndexedNotNull { i, it -> if (it > 0) beacon(i, it) else null }.toMutableList()
        //val actions = defaultBeacons.mapIndexedNotNull { i, it -> if (it > 0) beacon(i, it) else null }.toMutableList()
//        val actions = mutableListOf(line(6, 0,1))

        log("Best gain : $gain")
        actions.add(message("${System.currentTimeMillis() - start}ms"))
        println(actions.joinToString(";"))
        Game.firstTurn = false

//        var rem = Game.me.totalAnts - 10
//        println(listOf(
//            line(81,75, 1),
//            line(15,10, 3),
//            beacon(47,rem)
//        ).joinToString(";"))

    }
}


