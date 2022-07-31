package fr.vco.codingame.puzzles.mars.lander

import fr.vco.codingame.puzzles.mars.lander.engine.Action
import fr.vco.codingame.puzzles.mars.lander.engine.State
import fr.vco.codingame.puzzles.mars.lander.engine.Surface
import fr.vco.codingame.puzzles.mars.lander.engine.boundedValue
import java.util.*
import kotlin.system.measureTimeMillis




fun main() {
    val input = Scanner(System.`in`)

    val surface = Surface(HEIGHT, WIDTH, input)

    val initialState = State(input)

    val genAlgo = GeneticAlgorithm(
        initialState,
        surface,
        CHROMOSOME_SIZE,
        POPULATION_SIZE,
        MUTATION_PROBABILITY,
        ELITISM,
        SPEED_MAX,
        SPEED_WEIGHT
    )
    val actions: Array<Action>
    measureTimeMillis {
        actions = genAlgo.search(900).actions
    }.let { System.err.println("Solution find in ${it}ms") }

    var rotate = initialState.rotate
    var power = initialState.power
    actions.forEach {
        rotate = boundedValue(rotate + it.rotate, -90, 90)
        power = boundedValue(power + it.power, 0, 4)
        println("$rotate $power")
        State(input)
    }


}
