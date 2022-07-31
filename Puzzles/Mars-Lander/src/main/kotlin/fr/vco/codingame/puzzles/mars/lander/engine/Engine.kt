package fr.vco.codingame.puzzles.mars.lander.engine

import kotlin.math.*



enum class CrossingEnum {
    NOPE, CRASH, LANDING_ZONE
}


data class Point(val x: Double, val y: Double)

data class Segment(val start: Point, val end: Point) {
    val length = sqrt((start.x - end.x).pow(2) + (start.y - end.y).pow(2))
    var isLandingZone: Boolean = false
    var distanceToLanding = 0.0
    lateinit var proportion: (Segment, Double) -> Double

    fun distanceToLanding(x: Double): Double {
        return if (isLandingZone) 0.0
        else distanceToLanding + (proportion(this, x) * length)
    }

    fun distanceForProjection(x: Double, y: Double): Double {
        return distanceToLanding(x) + (y - start.y)
    }

    fun getYProjection(x: Double): Double {
        return (start.x - x) / (start.x - end.x) * (start.y - end.y) + end.y
    }

    fun getProjection(x: Double, y: Double): Pair<Double, Double> {
        val yp = (start.x - x) / (start.x - end.x) * (start.y - end.y) + end.y
        val xp = (start.y - y) / (start.y - end.y) * (start.x - end.x) + end.x
        return xp to yp
    }

}

