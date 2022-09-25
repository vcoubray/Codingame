package fr.vco.codingame.puzzles.shadow.knight

import java.util.*
import kotlin.math.roundToInt

enum class Distance { COLDER, WARMER, SAME, UNKNOWN }

data class Point(val x: Int, val y: Int) {
    operator fun plus(p: Point) = Point(x + p.x, y + p.y)
    operator fun minus(p: Point) = Point(x - p.x, y - p.y)
    operator fun div(divider: Double) = (x / divider) to (y / divider)
    fun center(p: Point) = (x + (p.x - x) / 2.0) to (y + (p.y - y) / 2.0)
    fun mirror(p: Pair<Double, Double>) = Point((2 * p.first - x).roundToInt(), (2 * p.second - y).roundToInt())
    fun dist(p: Point) = (p.x - x) * (p.x - x) + (p.y - y) * (p.y - y)
    override fun toString() = "$x $y"
}

data class Area(val xMin: Int, val xMax: Int, val yMin: Int, val yMax: Int) {
    private val sizeX = xMax - xMin
    private val sizeY = yMax - yMin
    private val centroid = (xMin + sizeX / 2.0) to (yMin + sizeY / 2.0)

    private fun toPoints() = listOf(
        Point(xMin, yMin),
        Point(xMax, yMin),
        Point(xMax, yMax),
        Point(xMin, yMax)
    )

    fun cut(lastPos: Point, newPos: Point, distance: Distance): Area {
        val (cutX, cutY) = lastPos.center(newPos)

        return if (lastPos.x == newPos.x) cutY(newPos.y, cutY, distance)
        else if (lastPos.y == newPos.y) cutX(newPos.x, cutX, distance)
        else this
    }

    private fun cutX(newX: Int, cutX: Double, distance: Distance): Area {
        val minCenter = (cutX - 0.5).toInt()
        val maxCenter = (cutX + 1).toInt()

        val minArea = this.copy(xMax = minCenter)
        val maxArea = this.copy(xMin = maxCenter)

        return when {
            distance == Distance.SAME -> Area(cutX.toInt(), cutX.toInt(), yMin, yMax)
            distance == Distance.WARMER && newX <= minCenter -> minArea
            distance == Distance.COLDER && newX >= maxCenter -> minArea
            else -> maxArea
        }
    }

    private fun cutY(newY: Int, cutY: Double, distance: Distance): Area {
        val minCenter = (cutY - 0.5).toInt()
        val maxCenter = (cutY + 1).toInt()

        val minArea = this.copy(yMax = minCenter)
        val maxArea = this.copy(yMin = maxCenter)

        return when {
            distance == Distance.SAME -> Area(xMin, xMax, cutY.toInt(), cutY.toInt())
            distance == Distance.WARMER && newY <= minCenter -> minArea
            distance == Distance.COLDER && newY >= maxCenter -> minArea
            else -> maxArea
        }
    }

    fun getNextPos(batman: Point, height: Int, width: Int): Point {
        val nextPos = batman.mirror(centroid)
        val (centerX, centerY) = batman.center(nextPos)

        return when {
            sizeX > 0 &&
                batman.x != nextPos.x &&
                nextPos.x in 0..width &&
                centerX in xMin.toDouble()..xMax.toDouble() -> Point(nextPos.x, batman.y)

            sizeY > 0 &&
                batman.y != nextPos.y &&
                nextPos.y in 0..height &&
                centerY in yMin.toDouble()..yMax.toDouble() -> Point(batman.x, nextPos.y)

            else -> toPoints().minByOrNull(batman::dist)!!
        }
    }
}

fun main() {
    val input = Scanner(System.`in`)
    val (width, height, _, batmanX, batmanY) = List(5) { input.nextInt() }

    var area = Area(0, width - 1, 0, height - 1)
    var batman = Point(batmanX, batmanY)
    var lastPos = batman.copy()

    while (true) {
        val bombDir = Distance.valueOf(input.next())
        if (bombDir != Distance.UNKNOWN) {
            area = area.cut(lastPos, batman, bombDir)
        }
        val nextPos = area.getNextPos(batman, height, width)
        lastPos = batman
        batman = nextPos

        println(batman)
    }
}