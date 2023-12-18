package fr.vco.codingame.contest.fallchallenge2023

import java.util.*
import kotlin.random.Random

data class Position(val x: Int, val y: Int) {
    fun dist(p: Position) = (x - p.x) * (x - p.x) + (y - p.y) * (y - p.y)
}

data class CreatureType(
    val id: Int,
    val color: Int,
    val type: Int,
)

data class VisibleCreature(
    val id: Int,
    val position: Position,
    val creatureVx: Int,
    val creatureVy: Int,
)

data class Drone(
    val id: Int,
    val position: Position,
    val emergency: Int,
    val battery: Int,
)

fun Scanner.readCreatureTypes(): List<CreatureType> {
    val creatureCount = nextInt()
    return List(creatureCount) {
        CreatureType(
            id = nextInt(),
            color = nextInt(),
            type = nextInt()
        )
    }
}

fun Scanner.readDrones(): List<Drone> {
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


fun randomPosition() = Position(Random.nextInt(10_000), Random.nextInt())
fun main() {
    val input = Scanner(System.`in`)
    val creatureTypes = input.readCreatureTypes()
    
    
    // game loop
    while (true) {
        val myScore = input.nextInt()
        val foeScore = input.nextInt()

        val myScanCount = input.nextInt()
        val myScans = List(myScanCount) { input.nextInt() }
        val foeScanCount = input.nextInt()
        val foeScans = List(foeScanCount) { input.nextInt() }

        val myDrones = input.readDrones()
        val foeDrones = input.readDrones()

        val droneScanCount = input.nextInt()
        for (i in 0 until droneScanCount) {
            val droneId = input.nextInt()
            val creatureId = input.nextInt()
        }

        val visibleCreatureCount = input.nextInt()
        val visibleCreatures = List(visibleCreatureCount) {
            VisibleCreature(
                id = input.nextInt(),
                Position(x = input.nextInt(), y = input.nextInt()),
                creatureVx = input.nextInt(),
                creatureVy = input.nextInt()
            )
        }

        val radarBlipCount = input.nextInt()
        for (i in 0 until radarBlipCount) {
            val droneId = input.nextInt()
            val creatureId = input.nextInt()
            val radar = input.next()
        }

        myDrones.forEach { drone ->
            
            val target = visibleCreatures.filter{it.id !in myScans}
                .minByOrNull{it.position.dist(drone.position)}
            if (target != null) {
                println("MOVE ${target.position.x} ${target.position.y} 0")
            } else {
                println("WAIT 1") // MOVE <x> <y> <light (1|0)> | WAIT <light (1|0)>    
            }
        }
    }
}