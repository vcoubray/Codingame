package fr.vco.codingame.contest.fallchallenge2023

import java.util.*

fun log(value: Any?) = System.err.println(value)

data class Position(val x: Int, val y: Int) {
    fun dist(p: Position) = (x - p.x) * (x - p.x) + (y - p.y) * (y - p.y)
}

data class Creature(
    val id: Int,
    val color: Int,
    val type: Int,
    var visible: Boolean = false,
    var position: Position? = null,
    var vector: Position? = null,
)

data class Player(
    var score: Int = 0,
    var scans: List<Int> = emptyList(),
    var drones: List<Drone> = emptyList(),
)

data class Drone(
    val id: Int,
    val position: Position,
    val emergency: Int,
    val battery: Int,
    val radars: MutableMap<Int, String> = mutableMapOf(),
    val scans: MutableList<Int> = mutableListOf(),
)

data class Radar(
    val droneId: Int,
    val creatureId: Int,
    val direction: String,
)


const val MONSTER = -1
const val JELLYFISH = 0
const val FISH = 1
const val CRAB = 2


object Game {
    val input = Scanner(System.`in`)

    val me = Player()
    val foe = Player()

    lateinit var creatures: Map<Int, Creature>

    fun init() {
        creatures = input.readCreatures()
    }

    fun update() {
        val myScore = input.nextInt()
        val foeScore = input.nextInt()

        val myScanCount = input.nextInt()
        val myScans = List(myScanCount) { input.nextInt() }
        val foeScanCount = input.nextInt()
        val foeScans = List(foeScanCount) { input.nextInt() }

        val myDrones = input.readDrones()
        val foeDrones = input.readDrones()
        val drones = (myDrones + foeDrones).associateBy { it.id }

        val droneScanCount = input.nextInt()
        repeat(droneScanCount) {
            val droneId = input.nextInt()
            val creatureId = input.nextInt()
            drones[droneId]!!.scans.add(creatureId)
        }

        val visibleCreatureCount = input.nextInt()

        creatures.forEach { (_, it) -> it.visible = false }
        repeat(visibleCreatureCount) {
            val id = input.nextInt()
            val pos = Position(x = input.nextInt(), y = input.nextInt())
            val vector = Position(x = input.nextInt(), y = input.nextInt())
            creatures[id]!!.visible = true
            creatures[id]!!.position = pos
            creatures[id]!!.vector = vector
        }

        val radarBlipCount = input.nextInt()
        repeat(radarBlipCount) {
            val droneId = input.nextInt()
            val creatureId = input.nextInt()
            val direction = input.next()
            drones[droneId]!!.radars[creatureId] = direction
        }

        me.score = myScore
        me.scans = myScans
        me.drones = myDrones
        foe.score = foeScore
        foe.scans = foeScans
        foe.drones = foeDrones
    }

    private fun Scanner.readCreatures(): Map<Int, Creature> {
        val creatureCount = nextInt()
        return List(creatureCount) {
            Creature(
                id = nextInt(),
                color = nextInt(),
                type = nextInt()
            )
        }.associateBy { it.id }
    }

    private fun Scanner.readDrones(): List<Drone> {
        val droneCount = nextInt()
        return List(droneCount) {
            Drone(
                id = nextInt(),
                Position(x = nextInt(), y = nextInt()),
                emergency = nextInt(),
                battery = nextInt()
            )
        }
    }

}

fun main() {
    Game.init()

    // game loop
    while (true) {
        Game.creatures.forEach(::log)

        val alreadyTargeted = mutableListOf<Int>()
        val (leftDrone, rightDrone) = Game.me.drones.sortedBy { it.position.x }

        var myDronesScans = Game.me.drones.flatMap { it.scans }


        var leftTarget = Game.creatures.filter {(_,creature) -> 
            creature.type != MONSTER
        }.map { (id, _) ->
            var score = 0
            if (id !in Game.foe.scans) score += 50
            if (id !in Game.me.scans && id !in myDronesScans) score += 50
            if (leftDrone.radars[id] == "BL") score += 200
            if (leftDrone.radars[id] == "TL") score += 150
            if (leftDrone.radars[id] == "BR") score += 100
            if (leftDrone.radars[id] == "TR") score += 50
            id to score
        }.maxByOrNull { it.second }


        if(leftTarget != null) {
            println("Move ${leftDrone.position.x} 0 1")
        } else {
            println("Move ${leftDrone.position.x} 0 1")
        }


            Game.me.drones.forEach { drone ->
                val radar = drone.radars.values.firstOrNull {
                    it.creatureId !in drone.scans &&
                        Game.creatures[it.creatureId]!!.type != MONSTER &&
                        it.creatureId !in Game.me.scans &&
                        it.creatureId !in alreadyTargeted
                }
                if (radar != null) {
                    alreadyTargeted.add(radar.creatureId)
                    val target = when (radar.direction) {
                        "BL" -> Position(drone.position.x - 600, drone.position.y + 600)
                        "BR" -> Position(drone.position.x + 600, drone.position.y + 600)
                        "TL" -> Position(drone.position.x - 600, drone.position.y - 600)
                        else -> Position(drone.position.x + 600, drone.position.y - 600)
                    }
                    println("MOVE ${target.x} ${target.y} 1")
                } else {
                    println("Move ${drone.position.x} 0 1")
                }
            }
    }
}