package fr.vco.codingame.puzzles.voxcodei.episode2

const val BOMB_RANGE = 3
const val EMPTY = -1
const val WALL = -2

data class Position(val x: Int, val y: Int) {
    operator fun plus(other: Position) = Position(x + other.x, y + other.y)
    operator fun minus(other: Position) = Position(x - other.x, y - other.y)
    operator fun times(times: Int) = Position(x * times, y * times)
    operator fun unaryMinus() = Position(-x, -y)
}

class GameSimulator(
    val width: Int,
    val height: Int,
    val bombs: Int,
    val turns: Int,
    val nodeCount: Int,
    val grids: List<List<Int>>,
    val ranges: List<List<Int>>,
) {

    fun getFirstState() = State(
        turns,
        bombs,
        List(nodeCount){true}
    )

}

class State(
    val turn: Int,
    val remainingBombs: Int,
    val nodes: List<Boolean>,
    val bomb1: Int = -1,
    val bomb2: Int = -1,
    val bomb3: Int = -1,
) {


    fun isFinal(): Boolean {
        return turn >= 3 ||
            nodes.all { false } ||
            (remainingBombs == 0 && bomb1 == -1 && bomb2 == -1 && bomb3 == 1)
    }

    fun isWin(): Boolean = nodes.all { false }

    fun getActions(simulator: GameSimulator): List<Int> {
        val grid = simulator.grids[turn-3]
        return grid.mapIndexedNotNull { i, cell -> i.takeIf{isFreeCell(cell)} } + -1 
    }

    private fun isFreeCell(cell: Int) = (cell == EMPTY || (cell >= 0 && !nodes[cell]))
    
    
    private fun play(coord: Int, simulator: GameSimulator) : State {
        
        return State(
            turn -1,
                remainingBombs-1,
            explodeBombs(simulator),
            bomb2,
            bomb3,
            coord
        )
        
    }
    
    private fun explodeBombs(simulator: GameSimulator): List<Boolean> {
        val newNodes = nodes.toMutableList()
        if (bomb1 != -1) {
            simulator.ranges[bomb1].forEach {
                if (simulator.grids[turn][it] >= 0) {
                    newNodes[it] = false
                }
            }
        }
        return newNodes
    }
}


fun main() {
    val (width, height) = readln().split(" ").map { it.toInt() }

    val gridBuilder = SimulatorBuilder(width, height)
    val simulator = gridBuilder.buildSimulator()
    System.err.println("Simulator is ready")

    System.err.println(simulator.turns)
    val state = simulator.getFirstState()
    
    
    System.err.println(state.getActions(simulator))
    
    
    var i = 0
    while (true) {
        println("$i 1")
        i++
        val (rounds, bombs) = readln().split(" ").map { it.toInt() }
        val grid = List(height) { readln() }
        grid.forEach(System.err::println)
    }

}