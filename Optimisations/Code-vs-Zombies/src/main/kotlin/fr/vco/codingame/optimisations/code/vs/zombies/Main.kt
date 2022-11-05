package fr.vco.codingame.optimisations.code.vs.zombies

import java.util.*
import kotlin.math.sqrt
import kotlin.random.Random.Default.nextInt

const val WIDTH = 16_000
const val HEIGHT = 9_000
const val ASH_MOVE = 1_000
const val ASH_RANGE = 2_000
const val ZOMBIE_MOVE = 400
const val TIMEOUT = 950

fun log(any: Any) = System.err.println(any)

object Combo {
    private val combos = MutableList(100) { 0L }

    init {
        var a = 0L
        var b = 1L
        repeat(100) {
            val t = a
            a = b
            b += t
            combos[it] = b
        }
    }

    operator fun get(index: Int) = combos[index]
}


data class Pos(var x: Int = -1, var y: Int = -1) {
    override fun toString() = "$x $y"

    fun randomize() {
        x = nextInt(WIDTH)
        y = nextInt(HEIGHT)
    }

    fun loadFrom(source: Pos) {
        x = source.x
        y = source.y
    }

    fun dist(pos: Pos): Double {
        val x = pos.x - this.x
        val y = pos.y - this.y
        return sqrt((x * x + y * y).toDouble())
    }

    fun move(destination: Pos, distance: Int) {
        val ratio = distance / dist(destination)
        if (ratio > 1) {
            x = destination.x
            y = destination.y
        } else {
            x = (x + (destination.x - x) * ratio).toInt()
            y = (y + (destination.y - y) * ratio).toInt()
        }
    }
}

data class Entity(var id: Int = -1, val pos: Pos = Pos()) {
    fun loadFrom(source: Entity) {
        id = source.id
        pos.loadFrom(source.pos)
    }
}

class State(
    var ash: Pos = Pos(),
    var humanCount: Int = 0,
    var humans: Array<Entity> = Array(100) { Entity() },
    var zombieCount: Int = 0,
    var zombies: Array<Entity> = Array(100) { Entity() },
) {
    var score = 0L
    var turn = 0


    fun loadFrom(source: State) {
        ash.loadFrom(source.ash)
        humanCount = source.humanCount
        repeat(source.humanCount) {
            humans[it].loadFrom(source.humans[it])
        }
        zombieCount = source.zombieCount
        repeat(source.zombieCount) {
            zombies[it].loadFrom(source.zombies[it])
        }
        score = source.score
        turn = source.turn
    }

    private fun play(pos: Pos) {
        moveZombies()
        moveAsh(pos)
        killZombies()
        killHumans()
    }

    private fun moveZombies() {
        repeat(zombieCount) { i ->
            val zombie = zombies[i]
            var target = ash
            var targetId = -1
            var targetDist = zombie.pos.dist(ash)
            repeat(humanCount) { j ->
                val dist = humans[j].pos.dist(zombie.pos)
                if (dist == targetDist && humans[j].id < targetId) {
                    target = humans[j].pos
                    targetId = humans[j].id
                }
                if (dist < targetDist) {
                    target = humans[j].pos
                    targetId = humans[j].id
                    targetDist = dist
                }
            }
            zombie.pos.move(target, ZOMBIE_MOVE)
        }
    }

    private fun moveAsh(pos: Pos) {
        ash.move(pos, ASH_MOVE)
    }

    private fun killZombies() {
        var kills = 0

        for (i in zombieCount - 1 downTo 0) {
            val zombie = zombies[i]
            if (ash.dist(zombie.pos) < ASH_RANGE) {
//                log("Killing zombie ${zombie.id} ($i)! at turn $turn")
                zombies[i].loadFrom(zombies[zombieCount - 1])
                zombieCount--
                val points = 10 * humanCount * humanCount * Combo[kills]
                score += points
//                log("Score : $score (+$points) ($humanCount, $kills, ${Combo[kills]})")
                kills++
            }
        }
    }

    private fun killHumans() {
        repeat(zombieCount) { i ->
            val zombie = zombies[i]
            for (j in humanCount - 1 downTo 0) {
//            repeat(humanCount) { j ->
                if (zombie.pos == humans[j].pos) {
                    humans[j].loadFrom(humans[humanCount - 1])
                    humanCount--
//                    log("Killing human ${humans[j].id}!")
                }
            }
        }
    }

    private val destination = Pos()
    fun playUntilEnd(actions: Array<Pos>) {
        turn = 0
        destination.randomize()

        while (humanCount > 0 && zombieCount > 0 && turn < actions.size) {
            if (ash == destination) {
                destination.randomize()
            }
            actions[turn].loadFrom(destination)
            play(destination)
            turn++
        }
        if (zombieCount > 0) score = 0
    }
}


fun main() {
    val input = Scanner(System.`in`)

    val start = System.currentTimeMillis()

    val workingState = State()
    val bestState = State()
    var initialState = readState(input)

    var bestScore = -1L
    val bestActions = Array(50) { Pos() }
    val workingActions = Array(50) { Pos() }
    var simuCount = 0

    while (System.currentTimeMillis() - start < TIMEOUT) {
        workingState.loadFrom(initialState)
        workingState.playUntilEnd(workingActions)

        if (bestScore < workingState.score) {
            bestScore = workingState.score
            bestState.loadFrom(workingState)
            repeat(workingState.turn) {
                bestActions[it].loadFrom(workingActions[it])
            }
        }
        simuCount++
    }

    log("$simuCount simulations in ${System.currentTimeMillis() - start}ms. Best score : $bestScore")
//    log(bestActions.joinToString(", ") { "Pos(${it.x}, ${it.y})"  })
    for (action in bestActions) {
        println(action)
        initialState = readState(input)
    }
}

fun readState(input: Scanner): State {

    val ash = Pos(input.nextInt(), input.nextInt())
    val humanCount = input.nextInt()
    val humans = List(humanCount) {
        val humanId = input.nextInt()
        val humanX = input.nextInt()
        val humanY = input.nextInt()
        Entity(humanId, Pos(humanX, humanY))
    }

    val zombieCount = input.nextInt()
    val zombies = List(zombieCount) {
        val zombieId = input.nextInt()
        val zombieX = input.nextInt()
        val zombieY = input.nextInt()
        val zombieXNext = input.nextInt()
        val zombieYNext = input.nextInt()
        Entity(zombieId, Pos(zombieX, zombieY))
    }
    return State(
        ash,
        humanCount,
        humans.toTypedArray(),
        zombieCount,
        zombies.toTypedArray()
    )
}


