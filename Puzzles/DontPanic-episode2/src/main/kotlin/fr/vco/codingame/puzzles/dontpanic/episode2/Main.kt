package fr.vco.codingame.puzzles.dontpanic.episode2

import java.util.*

enum class Action(val play: String) {
    BLOCK("BLOCK"), ELEVATOR("ELEVATOR"), WAIT("WAIT"), WAIT_ASCENCEUR("ASCENCEUR")
}

const val IN_PROGRESS = 0
const val WIN = 1
const val LOOSE = 2

object Game {
    var nbFloors: Int = 0
    var width: Int = 0
    var nbRounds: Int = 0
    var exitFloor = 0
    var exitPos = 0
    var nbTotalClones = 0
    var nbAdditionalElevators = 0
    var nbElevators = 0
    var elevators = List(nbFloors) { mutableMapOf<Int, Boolean>() }
}


data class State(
    var turnLeft: Int,
    var cloneFloor: Int,
    var clonePos: Int,
    var nbElevators: Int,
    var direction: Int,
    var clones: Int
) {
    var actions = LinkedList<Action>()
//    var positions = LinkedList<Pair<Int, Int>>()

    fun nextTurn(action: Action) {
        when (action) {
            Action.BLOCK -> {
                direction = -direction
                turnLeft -= 3
                clonePos += direction
                clones--
            }
            Action.WAIT -> {
                clonePos += direction
            }
            Action.WAIT_ASCENCEUR -> {
                cloneFloor++
            }
            Action.ELEVATOR -> {
                cloneFloor++
                turnLeft -= 3
                nbElevators--
                clones--
            }
        }
        turnLeft--
    }

    fun undoTurn(action: Action) {
        when (action) {
            Action.BLOCK -> {
                direction = -direction
                turnLeft += 3
                clonePos += direction
                clones++
            }
            Action.WAIT -> {
                clonePos -= direction
            }
            Action.WAIT_ASCENCEUR -> {
                cloneFloor--
            }
            Action.ELEVATOR -> {
                cloneFloor--
                nbElevators++
                turnLeft += 3
                clones++
            }
        }
        turnLeft++
    }

    fun availableAction(): List<Action> {
        val actions = mutableListOf<Action>()
        if (nbElevators > 0 && Game.elevators[cloneFloor][clonePos] != true) actions.add(Action.ELEVATOR)

        val lastAction = this.actions.lastOrNull() ?: Action.ELEVATOR
        if (lastAction == Action.WAIT_ASCENCEUR || lastAction == Action.ELEVATOR) actions.add(Action.BLOCK)

        if ((Game.elevators[cloneFloor][clonePos] == true)) actions.add(Action.WAIT_ASCENCEUR)
        else actions.add(Action.WAIT)
        return actions
    }


    fun status(): Int {
        return when {
            turnLeft < 0 -> LOOSE
            cloneFloor > Game.exitFloor -> LOOSE
            clonePos !in (0 until Game.width) -> LOOSE
            cloneFloor == Game.exitFloor && clonePos == Game.exitPos -> WIN
            else -> IN_PROGRESS
        }
    }

    fun findActions(): Boolean {

        return when (status()) {
            WIN -> true
            LOOSE -> false
            else -> {
                availableAction().forEach {
                    nextTurn(it)
                    actions.addLast(it)
//                    positions.addLast(cloneFloor to clonePos)
                    if (findActions()) return true
                    undoTurn(it)
                    actions.removeLast()
//                    positions.removeLast()
                }
                false
            }
        }
    }

}


fun main() {
    val input = Scanner(System.`in`)
    Game.nbFloors = input.nextInt() // number of floors
    Game.width = input.nextInt() // width of the area
    Game.nbRounds = input.nextInt() // maximum number of rounds
    Game.exitFloor = input.nextInt() // floor on which the exit is found
    Game.exitPos = input.nextInt() // position of the exit on its floor
    Game.nbTotalClones = input.nextInt() // number of generated clones
    Game.nbAdditionalElevators = input.nextInt() // ignore (always zero)
    Game.nbElevators = input.nextInt() // number of elevators

    Game.elevators = List(Game.nbFloors) { mutableMapOf() }
    val elevators = MutableList(Game.nbElevators) { input.nextInt() to input.nextInt() }
    elevators.forEach { Game.elevators[it.first][it.second] = true }


    var cloneFloor = input.nextInt() // floor of the leading clone
    var clonePos = input.nextInt() // position of the leading clone on its floor
    var direction = input.next() // direction of the leading clone: LEFT or RIGHT

    val state = State(
        Game.nbRounds,
        cloneFloor,
        clonePos,
        Game.nbAdditionalElevators,
        if (direction == "RIGHT") 1 else -1,
        Game.nbTotalClones
    )

    state.findActions()
  //  System.err.println(state.actions)
//    val positions = state.positions.toArray()
    state.actions.forEach {
        when (it) {
            Action.WAIT, Action.WAIT_ASCENCEUR -> println("WAIT")
            else -> {
                println(it)
                repeat(3) {
                    cloneFloor = input.nextInt() // floor of the leading clone
                    clonePos = input.nextInt() // position of the leading clone on its floor
                    direction = input.next()
                    println("WAIT")

                }
            }
        }
        cloneFloor = input.nextInt() // floor of the leading clone
        clonePos = input.nextInt() // position of the leading clone on its floor
        direction = input.next() // direction of the leading clone: LEFT or RIGHT

//        System.err.println(positions[i])
//        System.err.println("$cloneFloor, $clonePos")
    }
    println("WAIT")
}