package fr.vco.codingame.puzzles.mars.lander.engine

import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

data class Surface(
    val height: Int,
    val width: Int,
    val segments: List<Segment>
) {

    val landingZoneY: Double
    val landingZoneX: Pair<Double, Double>
    var distanceMax: Double = 0.0

    constructor(height: Int, width: Int, input: Scanner) : this(
        height,
        width,
        List(input.nextInt()) { Point(input.nextDouble(), input.nextDouble()) }
            .windowed(2)
            .map { (a, b) -> Segment(a, b) }
    )

    init {

        val landingZoneIndex = segments.indexOfFirst { it.start.y == it.end.y }
        segments[landingZoneIndex].isLandingZone = true
        landingZoneY = segments[landingZoneIndex].start.y
        landingZoneX = segments[landingZoneIndex].start.x to segments[landingZoneIndex].end.x


        var sum = 0.0
        for (i in landingZoneIndex - 1 downTo 0) {
            segments[i].distanceToLanding = sum
            segments[i].proportion =
                { segment, x -> (x - segment.end.x) / (segment.start.x - segment.end.x) }
            sum += segments[i].length

        }
        distanceMax = sum
        sum = 0.0
        for (i in landingZoneIndex + 1 until segments.size) {
            segments[i].distanceToLanding = sum
            segments[i].proportion =
                { segment, x -> (x - segment.start.x) / (segment.end.x - segment.start.x) }
            sum += segments[i].length
        }

        if (sum > distanceMax) {
            distanceMax = sum
        }

    }

    fun cross(s1: Segment, s2: Segment): Point? {
        val s1x = s1.end.x - s1.start.x
        val s1y = s1.end.y - s1.start.y
        val s2x = s2.end.x - s2.start.x
        val s2y = s2.end.y - s2.start.y

        if ((s2x * s1y == s1x * s2y)) return null

        val s =
            (-s1y * (s1.start.x - s2.start.x) + s1x * (s1.start.y - s2.start.y)) / (-s2x * s1y + s1x * s2y)
        val t =
            (-s2y * (s1.start.x - s2.start.x) + s2x * (s1.start.y - s2.start.y)) / (-s2x * s1y + s1x * s2y)

        if (s in 0.0..1.0 && t in 0.0..1.0) {
            return Point(
                s1.start.x + (t * s1x),
                s1.start.y + (t * s1y)
            )
        }
        return null
    }

    fun cross(path: Segment): Pair<Segment, Point>? {
        for (segment in segments) {
            cross(segment,path)?.let{
                return segment to it
            }
        }
        return null
    }


    fun distanceToLandingZone(x: Double, y: Double): Double {
        return when {
            x < landingZoneX.first -> sqrt((x - landingZoneX.first).pow(2) + (y - landingZoneY).pow(2))
            x > landingZoneX.second -> sqrt((x - landingZoneX.second).pow(2) + (y - landingZoneY).pow(2))
            else -> abs(y - landingZoneY)
        }
    }
}