package fr.vco.codingame.puzzles.mars.lander

import java.util.*
import kotlin.math.*
import kotlin.random.Random
import kotlin.system.measureTimeMillis

const val MARS_GRAVITY = 3.711

fun boundedValue(value: Int, min: Int, max: Int) = when {
    value <= min -> min
    value >= max -> max
    else -> value
}

fun generateAction(rotate: Int, power: Int) = Action(
    boundedValue(rotate + (-15..15).random(), -90, 90),
    boundedValue(power + (-1..+1).random(), 0, 4)
)


data class Action(val rotate: Int, val power: Int) {
    override fun toString() = "$rotate $power"
}


class Chromosome(var actions: Array<Action>) {
    var score = 0.0
}

class GeneticAlgorithm(
    val chromosomeSize: Int,
    val populationSize: Int,
    val surface: Surface,
    val initialState: State
) {

    fun generateChromosome(): Chromosome {

        var rotate = initialState.rotate
        var power = initialState.power
        return Chromosome((0 until chromosomeSize).map {
            power = boundedValue(power + (-1..1).random(), 0, 4)
            rotate = boundedValue(rotate + (-15..15).random(), -90, 90)
            Action(rotate, power)
        }.toTypedArray())
    }

    fun generateRandomPopulation(): Array<Chromosome> {
        return Array(populationSize) { generateChromosome() }
    }

    fun evaluation(population: Array<Chromosome>) {
        population.forEach {
            val state = initialState.copy()
            it.score = 1 / state.play(it.actions, surface)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun selection(population: Array<Chromosome>): Array<Chromosome> {

        val eliteSize = (populationSize * 0.3).toInt()
        val randomSize = (populationSize * 0.2).toInt()
        val randomIndices = buildList {
            while (this.size < randomSize) {
                val random = (eliteSize until populationSize).random()
                if (random !in this) add(random)
            }
        }
        val totalSize = eliteSize + randomSize
        population.sortByDescending { it.score }
        return Array(totalSize) {
            if (it < eliteSize) population[it]
            else population[randomIndices[it - eliteSize]]
        }

    }

    fun crossover(parent1: Chromosome, parent2: Chromosome): Chromosome {
        var childActions = parent1.actions.copyOf()
        for (i in chromosomeSize / 2 until chromosomeSize) {
            childActions[i] = parent2.actions[i]
        }
        return Chromosome(childActions)
    }

    fun mutation(chromosome: Chromosome) {
        val index = Random.nextInt(chromosomeSize)
        val (rotate, power) = if (index == 0) {
            initialState.rotate to initialState.power
        } else {
            chromosome.actions[index - 1].rotate to chromosome.actions[index - 1].power
        }
        chromosome.actions[index] = generateAction(rotate, power)
    }

    fun nextGeneration(population: Array<Chromosome>): Array<Chromosome> {

        val select = selection(population)

        val children = mutableListOf<Chromosome>()

        while (children.size < populationSize / 2) {
            val parent1 = select.random()
            val parent2 = select.random()
            val child = crossover(parent1, parent2).apply(::mutation)
            children.add(child)
        }
        return select + children
    }

    fun findBestResult(timeout: Int): Chromosome {

        val startTime = System.currentTimeMillis()
        var population = generateRandomPopulation()

        lateinit var result: Chromosome
        var generationCount = 0
        while (System.currentTimeMillis() < startTime + timeout) {
            evaluation(population)
            population = nextGeneration(population)
            generationCount++
            var bestScore = 0.0
            for (chromosome in population) {
                if (chromosome.score > bestScore) {
                    result = chromosome
                    bestScore = chromosome.score
                }
            }
            System.err.println("$generationCount -> $bestScore")
        }
        return result

    }

}

data class State(
    var x: Double,
    var y: Double,
    var xSpeed: Double,
    var ySpeed: Double,
    var fuel: Int,
    var rotate: Int,
    var power: Int
) {
    constructor(input: Scanner) : this(
        input.nextDouble(),
        input.nextDouble(),
        input.nextDouble(),
        input.nextDouble(),
        input.nextInt(),
        input.nextInt(),
        input.nextInt()
    )

    fun play(action: Action) {

        this.power = action.power
        this.rotate = action.rotate

        val rad = Math.toRadians(this.rotate.toDouble())
        val newXSpeed = (this.xSpeed - this.power * sin(rad))
        val newYSPeed = (this.ySpeed + this.power * cos(rad) - MARS_GRAVITY)

        this.x += (this.xSpeed + newXSpeed) * 0.5
        this.y += (this.ySpeed + newYSPeed) * 0.5

        this.xSpeed = newXSpeed
        this.ySpeed = newYSPeed

        this.fuel -= power
    }


    fun play(actions: Array<Action>, surface: Surface): Double {
        val lastX = x
        val lastY = y
        for (action in actions) {
            play(action)
            if (surface.cross(Segment(lastX to lastY, x to y))) {
                break
            }
        }
        return surface.distanceToLandingZone(x, y)
    }

}

data class Segment(val start: Pair<Double, Double>, val end: Pair<Double, Double>)

data class Surface(
    val segments: List<Segment>
) {

    constructor(input: Scanner) : this(
        List(input.nextInt()) { input.nextDouble() to input.nextDouble() }
            .windowed(2)
            .map { (a, b) -> Segment(a, b) }
    )

    val landingZoneY: Double
    val landingZoneX: Pair<Double, Double>

    init {
        val landingZone = segments.first { it.start.second == it.end.second }
        landingZoneY = landingZone.start.second
        landingZoneX = landingZone.start.first to landingZone.end.first
    }

    private fun cross(s1: Segment, s2: Segment): Boolean {
        val s1x = s1.end.first - s1.start.first
        val s1y = s1.end.second - s1.start.second
        val s2x = s2.end.first - s2.start.first
        val s2y = s2.end.second - s2.start.second

        val s =
            (-s1y * (s1.start.first - s2.start.first) + s1x * (s1.start.second - s2.start.second)) / (-s2x * s1y + s1x * s2y)
        val t =
            (-s2y * (s1.start.first - s2.start.first) + s2x * (s1.start.second - s2.start.second)) / (-s2x * s1y + s1x * s2y)

        return (s in 0.0..1.0 && t in 0.0..1.0)
    }

    fun cross(segment: Segment): Boolean {
        return segments.any { cross(it, segment) }
    }

    fun distanceToLandingZone(x: Double, y: Double): Double {
        return when {
            x < landingZoneX.first -> sqrt((x - landingZoneX.first).pow(2.0) + (y - landingZoneY).pow(2.0))
            x > landingZoneX.second -> sqrt((x - landingZoneX.second).pow(2.0) + (y - landingZoneY).pow(2.0))
            else -> abs(y - landingZoneY)
        }
    }
}

fun main() {
    val input = Scanner(System.`in`)

    val surface = Surface(input)

    val initialState = State(input)

    val algo = GeneticAlgorithm(40, 80, surface, initialState)
    val actions: Array<Action>
    measureTimeMillis {
        actions = algo.findBestResult(80).actions
    }.let { System.err.println("Solution find in ${it}ms") }

    actions.forEach {
        println(it)
        State(input)
    }

    // game loop
    while (true) {
        println("0 0")
//        val action = generateAction(initialState.rotate, initialState.power)
//        val score = initialState.play(listOf(action).toTypedArray(), surface)
//        System.err.println(initialState)
//        System.err.println("score : $score")
//
//        println(action)

        State(input)
    }
}
