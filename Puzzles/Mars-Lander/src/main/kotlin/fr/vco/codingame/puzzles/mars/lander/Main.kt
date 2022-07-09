package fr.vco.codingame.puzzles.mars.lander

import java.util.*
import kotlin.math.*

const val MARS_GRAVITY = 3.711

fun minMax(value: Int, min: Int, max: Int) = max(min(value, max), min)

data class Action(
    val rotate: Int,
    val power: Int
)

class Chromosome(val actions: Array<Action>)

class GeneticAlgorithm(
    val chromosomeSize: Int,
    val populationSize: Int,
    val initialState: State
) {

    fun generateChromosome(): Chromosome {

        var rotate = initialState.rotate
        var power = initialState.power
        return Chromosome((0 until chromosomeSize).map {
            power = minMax(power + (-1..1).random(), 0, 4)
            rotate = minMax(rotate + (-15..15).random(), -90, 90)
            Action(rotate, power)
        }.toTypedArray())
    }

    fun generation(): Array<Chromosome> {
        return (0 until populationSize).map { generateChromosome() }.toTypedArray()
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


    fun play(actions: List<Action>, surface: Surface) {
        val lastX = x
        val lastY = y
        for (action in actions) {
            play(action)
            if(surface.cross(Segment(lastX to lastY, x to y ))) {
                System.err.println("crossing")
                break
            }
        }
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

    private fun cross(s1: Segment, s2: Segment): Boolean {
        val s1x = s1.end.first - s1.start.first
        val s1y = s1.end.second - s1.start.second
        val s2x = s2.end.first - s2.start.first
        val s2y = s2.end.second - s2.start.second

        val s =
            (-s1y * (s1.start.first - s2.start.first) + s1x * (s1.start.second - s2.start.second)) / (-s2x * s1y + s1x * s2y)
        val t =
            (-s2y * (s1.start.first - s2.start.first) + s2x * (s1.start.second - s2.start.second)) / (-s2x * s1y + s1x * s2y)

        return (s in 0.0..1.0 && t in 0.0.. 1.0)
    }

    fun cross(segment: Segment) :Boolean {
        return segments.any{cross(it, segment)}
    }
}

fun main() {
    val input = Scanner(System.`in`)

    val surface = Surface(input)

    val realState = State(input)
    // game loop
    while (true) {

        val power = minMax(realState.power + (-1..1).random(), 0, 4)
        val rotate = minMax(realState.rotate + (-15..15).random(), -90, 90)

        val action = Action(rotate, power)
        realState.play(listOf(action),surface)
        System.err.println(realState)

        println("$rotate $power")

        State(input)
    }
}
