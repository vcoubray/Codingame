package fr.vco.codingame.contest.springchallenge2022

import kotlin.math.*

data class Pos(val x: Int, val y: Int) {

    constructor (radian: Double, distance: Int) : this(
        (cos(radian * PI) * distance).toInt(),
        (sin(radian * PI) * distance).toInt()
    )

    operator fun plus(pos: Pos) = Pos(x + pos.x, y + pos.y)
    operator fun minus(pos: Pos) = Pos(x - pos.x, y - pos.y)
    operator fun unaryMinus() = Pos(-x, -y)
    fun dist(pos: Pos) = hypot((pos.x - x).toDouble(), (pos.y - y).toDouble()).toInt()

    fun dir(dest: Pos, dist: Int = 1): Pos {
        val totalDist = dist(dest)
        return Pos(
            x = ((dest.x - this.x) * dist.toDouble() / totalDist).toInt(),
            y = ((dest.y - this.y) * dist.toDouble() / totalDist).toInt()
        )
    }

    override fun toString() = "$x $y"
}