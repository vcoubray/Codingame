package fr.vco.codingame.contest.springchallenge2022

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

data class Pos(val x: Int, val y: Int) {

    constructor (radian: Double, distance: Int) : this(
        (cos(radian * PI) * distance).toInt(),
        (sin(radian * PI) * distance).toInt()
    )

    operator fun plus(pos: Pos) = Pos(x + pos.x, y + pos.y)
    operator fun minus(pos: Pos) = Pos(x - pos.x, y - pos.y)
    fun dist(pos: Pos) = hypot((pos.x - x).toDouble(), (pos.y - y).toDouble()).toInt()
    override fun toString() = "$x $y"
}