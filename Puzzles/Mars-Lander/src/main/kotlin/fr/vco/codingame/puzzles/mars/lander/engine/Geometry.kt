package fr.vco.codingame.puzzles.mars.lander.engine

import kotlin.math.*

fun toRadians(value: Double) = value * PI / 180
val Y_VECTOR = (-90..90).associateWith { cos(toRadians(it.toDouble())) }
val X_VECTOR = (-90..90).associateWith { -sin(toRadians(it.toDouble())) }

data class Point(val x: Double, val y: Double)

data class Segment(val start: Point, val end: Point) {
    val vx = end.x - start.x
    val vy = end.y - start.y

    val length = sqrt(vx*vx + vy*vy)

    val xRange = if(vx > 0) start.x..end.x else end.x..start.x
    val yRange = if(vx > 0) start.y..end.y else end.y..start.y

    var isLandingZone: Boolean = false
    var distanceToLanding = 0.0
    lateinit var proportion: (Segment, Double) -> Double

    fun distanceToLanding(x: Double): Double {
        return if (isLandingZone) 0.0
        else distanceToLanding + (proportion(this, x) * length)
    }

    fun cross(p1x: Double, p1y: Double, p2x: Double, p2y: Double): Point? {
        val v2x = p2x - p1x
        val v2y = p2y - p1y

        if (v2x * vy == vx * v2y) return null

        val s = (-vy * (start.x - p1x) + vx * (start.y - p1y)) / (-v2x * vy + vx * v2y)
        val t = (-v2y * (start.x - p1x) + v2x * (start.y - p1y)) / (-v2x * vy + vx * v2y)

        if (s in 0.0..1.0 && t in 0.0..1.0) {
            return Point(
                start.x + (t * vx),
                start.y + (t * vy)
            )
        }
        return null
    }
}
