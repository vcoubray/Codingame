package fr.vco.codingame.puzzles.mars.lander

import java.util.*
import kotlin.math.*

const val MARS_GRAVITY = 3.711

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

    fun play(rotate: Int, power: Int) {

        this.power = power
        this.rotate = rotate

        val rad = Math.toRadians(this.rotate.toDouble())
        val newXSpeed = (this.xSpeed - this.power * sin(rad))
        val newYSPeed = (this.ySpeed + this.power * cos(rad) - MARS_GRAVITY)

        this.x += (this.xSpeed + newXSpeed) * 0.5
        this.y += (this.ySpeed + newYSPeed) * 0.5

        this.xSpeed = newXSpeed
        this.ySpeed = newYSPeed

        this.fuel -= power
    }

}

data class Surface(
    val segments: List<Segment>
) {
    constructor(input: Scanner) : this(
        List(input.nextInt()) { input.nextInt() to input.nextInt() }
            .windowed(2)
            .map { (a, b) -> Segment(a, b) }
    )
}

data class Segment(val start: Pair<Int, Int>, val end: Pair<Int, Int>)

fun minMax(value: Int, min: Int, max: Int) = max(min(value, max), min)

fun main() {
    val input = Scanner(System.`in`)

    val surface = Surface(input)

    val realState = State(input)
    // game loop
    while (true) {


        val power = minMax(realState.power + (-1..1).random(), 0, 4)
        val rotate = minMax(realState.rotate + (-15..15).random(), -90, 90)

        realState.play(rotate, power)
        System.err.println(realState)
        println("$rotate $power")

        State(input)
    }
}




