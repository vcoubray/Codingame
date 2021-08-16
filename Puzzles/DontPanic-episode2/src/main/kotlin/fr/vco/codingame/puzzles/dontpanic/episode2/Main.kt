package fr.vco.codingame.puzzles.dontpanic.episode2

import java.util.*

const val IN_PROGRESS = 0
const val WIN = 1
const val LOOSE = 2

enum class Action {
    BLOCK, ELEVATOR, WAIT, WAIT_ELEVATOR
}

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

class BFS (h : Int, w: Int, nbElevators : Int) {
    private val visited = Array(2) { Array(nbElevators + 1) { Array(h) { IntArray(w) { 0 } } } }

    private fun isVisited(state: State): Boolean {
        val dirIndex = if (state.direction == -1) 0 else 1
        return if (visited[dirIndex][state.nbElevators][state.cloneFloor][state.clonePos] < state.turnLeft) {
            visited[dirIndex][state.nbElevators][state.cloneFloor][state.clonePos] = state.turnLeft
            false
        } else true
    }

    fun proceed(root: State): State {
        val toVisit = LinkedList<State>()
        toVisit.add(root)
        while (toVisit.isNotEmpty()) {
            val current = toVisit.pop()
            val actions = current.availableAction()
            actions.forEach {
                val child = current.copy()
                child.nextTurn(it)
                child.parent = current
                val status = child.status()
                if (status == WIN) return child
                if (status == IN_PROGRESS && !isVisited(child)) toVisit.addLast(child)
            }
        }
        return root
    }
}

data class State(
    var turnLeft: Int,
    var cloneFloor: Int,
    var clonePos: Int,
    var nbElevators: Int,
    var direction: Int,
    var clones: Int
) {
    var parent: State? = null
    var action: Action? = null

    fun nextTurn(action: Action) {
        this.action = action
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
            Action.WAIT_ELEVATOR -> {
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

    fun availableAction(): List<Action> {
        val actions = mutableListOf<Action>()
        if (nbElevators > 0 && Game.elevators[cloneFloor][clonePos] != true) actions.add(Action.ELEVATOR)

        val lastAction = this.action ?: Action.ELEVATOR
        if (lastAction == Action.WAIT_ELEVATOR || lastAction == Action.ELEVATOR) actions.add(Action.BLOCK)

        if ((Game.elevators[cloneFloor][clonePos] == true)) actions.add(Action.WAIT_ELEVATOR)
        else actions.add(Action.WAIT)
        return actions
    }

    fun status(): Int {
        return when {
            turnLeft <= 0 -> LOOSE
            cloneFloor > Game.exitFloor -> LOOSE
            clonePos !in (0 until Game.width) -> LOOSE
            cloneFloor == Game.exitFloor && clonePos == Game.exitPos -> WIN
            else -> IN_PROGRESS
        }
    }

    fun getActions(): List<Action> {
        val actions = LinkedList<Action>()
        actions.add(Action.WAIT)
        var current = this
        while (current.parent != null && current.action != null) {
            if (current.action == Action.ELEVATOR || current.action == Action.BLOCK) {
                repeat(3) { actions.addFirst(Action.WAIT) }
            }
            actions.addFirst(current.action)

            current = current.parent!!
        }
        return actions
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

    val root = State(
        Game.nbRounds,
        cloneFloor,
        clonePos,
        Game.nbAdditionalElevators,
        if (direction == "RIGHT") 1 else -1,
        Game.nbTotalClones
    )

    val bfs = BFS(Game.nbFloors, Game.width, Game.nbAdditionalElevators)
    val actions = bfs.proceed(root).getActions()
    actions.forEach{
        when (it) {
            Action.WAIT_ELEVATOR -> println("WAIT")
            else -> println(it)
        }
        // Read Input to avoid Warning
        cloneFloor = input.nextInt()
        clonePos = input.nextInt()
        direction = input.next()
    }
}
