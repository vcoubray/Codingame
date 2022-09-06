package fr.vco.codingame.puzzles.mars.lander

import fr.vco.codingame.puzzles.mars.lander.algorithm.GeneticAlgorithm
import fr.vco.codingame.puzzles.mars.lander.engine.*
import java.util.*


fun main() {
    val input = Scanner(System.`in`)

    val surface = readSurface(input)
    val initialState = readState(input)
    val genAlgo = GeneticAlgorithm(surface, initialState)

    val actions = genAlgo.search(TIMEOUT).actions
    var rotate = initialState.rotate
    var power = initialState.power
    actions.forEach {
        rotate = boundedValue(rotate + it.rotate, -90, 90)
        power = boundedValue(power + it.power, 0, 4)
        println("$rotate $power")
        readState(input)
    }

}


fun readSurface(input: Scanner) = Surface(
    HEIGHT, WIDTH,
    List(input.nextInt()) { Point(input.nextDouble(), input.nextDouble()) }
        .windowed(2)
        .map { (a, b) -> Segment(a, b) }
)

fun readState(input: Scanner) = CapsuleState(
    input.nextDouble(),
    input.nextDouble(),
    input.nextDouble(),
    input.nextDouble(),
    input.nextInt(),
    input.nextInt(),
    input.nextInt()
)
