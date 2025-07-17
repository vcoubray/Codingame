package fr.vco.codingame.puzzles.voxcodei.episode2


fun initNodeArray(size: Int) = (1L shl size) - 1
operator fun Long.get(i: Int): Boolean = (this.shr(i) and 1L) == 1L

data class Position(val x: Int, val y: Int) {
    operator fun plus(other: Position) = Position(x + other.x, y + other.y)
    operator fun minus(other: Position) = Position(x - other.x, y - other.y)
    operator fun times(times: Int) = Position(x * times, y * times)
    operator fun unaryMinus() = Position(-x, -y)
    override fun toString() = "$x $y"
}

class GameSimulator(
    val width: Int,
    val height: Int,
    val bombs: Int,
    val turns: Int,
    val nodeCount: Int,
    val ranges: List<List<Int>>,
    val rounds: List<Round>
) {
    val nodeMask = initNodeArray(nodeCount)
    fun indexToPosition(index: Int) = Position(index % width, index / width)
}

class Round(
    val actions: List<Pair<Int, Long>>, // list of positions associate with the targeted nodes (inverse bit mask)
    val grid: List<Int>, // Grid
)

fun main() {

    val (width, height) = readln().split(" ").map { it.toInt() }

    val gridBuilder = SimulatorBuilder(width, height)
    val simulator = gridBuilder.buildSimulator()
    System.err.println("Simulator is ready")

//    println("WAIT")

//    val (rounds, bombs) = readln().split(" ").map { it.toInt() }
//    List(height) { readln() }.joinToString("")
//
//    val resolver = GameResolver(simulator, rounds)
//    System.err.println(resolver.actions.size)
//
//    val actions = resolver.resolve().sortedByDescending { it.turn }.toMutableList()
//    System.err.println(actions)
//
//
//    if(actions.firstOrNull()?.turn ==  rounds) {
//        println(simulator.indexToPosition(actions.first().pos))
//        actions.removeFirst()
//    } else {
//        println("WAIT")
//    }

    println("WAIT")

//    val resolver = Resolver(simulator, simulator.turns-1)
    val resolver = GameResolver(simulator, simulator.turns-1)
    val actions = resolver.resolve()
    System.err.println("actions : ${actions.joinToString()}")

    actions.map {
        if (it == -1) "WAIT"
        else simulator.indexToPosition(it).toString()
    }.forEach { action ->
        readln().split(" ").map { it.toInt() }
        List(height) { readln() }.joinToString("")
        println(action)
    }

    while (true) {
        readln().split(" ").map { it.toInt() }
        List(height) { readln() }.joinToString("")
        println("WAIT")
    }
}
