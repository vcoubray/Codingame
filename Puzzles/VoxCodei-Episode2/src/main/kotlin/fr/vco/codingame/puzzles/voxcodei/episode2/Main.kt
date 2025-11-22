package fr.vco.codingame.puzzles.voxcodei.episode2


fun main() {
    val (width, height) = readln().split(" ").map { it.toInt() }
    val gridBuilder = SimulatorBuilder(width, height)
    val simulator = gridBuilder.buildSimulator()
    System.err.println("Simulator is ready")

    val resolver = GameResolver(simulator)
    println("WAIT")

    val actions = resolver.resolve()

    actions.map { if (it == -1) "WAIT" else "${simulator.indexToPosition(it)}" }
        .forEach { action ->
            readln().split(" ").map { it.toInt() }
            repeat(height) { readln() }
            println(action)
        }
}
