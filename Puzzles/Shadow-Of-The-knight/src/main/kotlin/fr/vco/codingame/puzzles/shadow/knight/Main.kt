package fr.vco.codingame.puzzles.shadow.knight

import java.util.*
import kotlin.math.*

enum class Range { COLDER, WARMER, SAME, UNKNOWN }

data class Pixel(val x: Int, val y: Int) {
    operator fun plus(p: Pixel) = Pixel(x + p.x, y + p.y)
    operator fun minus(p: Pixel) = Pixel(x - p.x, y - p.y)
    operator fun div(divider: Double) = Point(x / divider, y / divider)

    fun dist(p: Pixel) = (p.x - x) * (p.x - x) + (p.y - y) * (p.y - y)
    fun center(p: Pixel) = (this + p) / 2.0
    fun rotate(center: Point, radian: Double): Pixel {
        return Pixel(
            ((x - center.x) * cos(radian) - (y - center.y) * sin(radian) + center.x).roundToInt(),
            ((x - center.x) * sin(radian) + (y - center.y) * cos(radian) + center.y).roundToInt()
        )
    }

    fun getNeighbours(): List<Pixel> = (0 until 9).map { Pixel(x + it % 3 - 1, y + it / 3 - 1) }
    override fun toString() = "$x $y"
}

data class Point(val x: Double, val y: Double)

data class Line(val a: Pixel, val b: Pixel) {
    private val vx = (b.x - a.x).toDouble()
    private val vy = (b.y - a.y).toDouble()

    operator fun contains(p: Pixel): Boolean {
        if (vx.absoluteValue >= vy.absoluteValue) {
            if (p.x !in min(a.x, b.x)..max(a.x, b.x)) return false
            return p.y == a.y + ((p.x - a.x) / vx * vy).roundToInt()
        } else {
            if (p.y !in min(a.y, b.y)..max(a.y, b.y)) return false
            return p.x == a.x + ((p.y - a.y) / vy * vx).roundToInt()
        }
    }

    fun getPerpendicularVector(): Pixel {
        val vector = b - a
        return Pixel(vector.y, -vector.x)
    }

    fun getCrossingPoint(start: Point, vector: Pixel): Pixel? {
        val v2x = vector.x
        val v2y = vector.y

        if (v2x * vy == vx * v2y) return null
        val t = (-v2y * (a.x - start.x) + v2x * (a.y - start.y)) / (-v2x * vy + vx * v2y)
        if (t in 0.0..1.0) {
            return Pixel(
                a.x + (t * vx).roundToInt(),
                a.y + (t * vy).roundToInt()
            )
        }
        return null
    }
}

data class Area(val height: Int, val width: Int, val vertices: Set<Pixel>) {
    companion object {
        fun ofRectangle(width: Int, height: Int) = Area(
            height,
            width,
            setOf(
                Pixel(0, 0),
                Pixel(0, height - 1),
                Pixel(width - 1, height - 1),
                Pixel(width - 1, 0)
            )
        )
    }

    private val lines = vertices.windowed(2).map { (a, b) -> Line(a, b) } + Line(vertices.last(), vertices.first())
    private val centroid = vertices.reduce { a, b -> a + b } / vertices.size.toDouble()

    fun cut(lastPos: Pixel, newPos: Pixel, range: Range): Area {
        val center = lastPos.center(newPos)
        val cuttingVector = Line(lastPos, newPos).getPerpendicularVector()

        val subAreas = lines.flatMap { line ->
            listOf(line.a) +
                (line.getCrossingPoint(center, cuttingVector)
                    ?.getNeighbours()
                    ?.filter { it in line }
                    ?.groupBy { getRange(lastPos, newPos, it) }
                    ?.mapNotNull { (distance, pixels) ->
                        when (distance) {
                            Range.WARMER -> pixels.maxByOrNull { it.dist(newPos) }
                            Range.COLDER -> pixels.minByOrNull { it.dist(newPos) }
                            Range.SAME, Range.UNKNOWN -> pixels.firstOrNull()
                        }
                    }
                    ?: emptyList())
        }.groupBy { getRange(lastPos, newPos, it) }
        return copy(vertices = subAreas[range]!!.toSet())
    }

    private fun getRange(lastPos: Pixel, newPos: Pixel, point: Pixel): Range {
        return when {
            point.dist(lastPos) > point.dist(newPos) -> Range.WARMER
            point.dist(lastPos) < point.dist(newPos) -> Range.COLDER
            point.dist(lastPos) == point.dist(newPos) -> Range.SAME
            else -> Range.UNKNOWN
        }
    }

    private val cuttingAxis = listOf(
        0.0,        // x-axis first
        2 * PI / 4, // then y-axis
        PI / 4,     // ending with diagonals
        3 * PI / 4
    )

    fun getNextPos(batman: Pixel, lastPos: Pixel): Pixel {
        val batmanAngle = atan2(batman.y - centroid.y, batman.x - centroid.x)
        return cuttingAxis
            .asSequence()
            .map { 2 * (it - batmanAngle) }
            .map { batman.rotate(centroid, it) }
            .filterNot { it == batman }
            .filterNot { vertices.all { batman.dist(it) == lastPos.dist(it) } }
            .firstOrNull { it.x in 0 until width && it.y in 0 until height }
            ?: vertices.maxByOrNull(batman::dist)!!
    }
}

fun main() {
    val input = Scanner(System.`in`)
    val (width, height, _, batmanX, batmanY) = List(5) { input.nextInt() }

    var area = Area.ofRectangle(width, height)
    var batman = Pixel(batmanX, batmanY)
    var lastPos = batman.copy()

    while (true) {
        val range = Range.valueOf(input.next())
        if (range != Range.UNKNOWN) {
            area = area.cut(lastPos, batman, range)
        }
        val nextPos = area.getNextPos(batman, lastPos)
        lastPos = batman
        batman = nextPos

        println(batman)
    }
}