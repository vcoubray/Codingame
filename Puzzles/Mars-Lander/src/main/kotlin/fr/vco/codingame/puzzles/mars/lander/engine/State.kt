package fr.vco.codingame.puzzles.mars.lander.engine

import fr.vco.codingame.puzzles.mars.lander.*
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.max

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


    val path = mutableListOf(x to y)
    var status = CrossingEnum.NOPE
    var normalizedDistance = 0.0
    var normalizedSpeed = 0.0
    var normalizedRotate = 0.0

    fun play(action: Action) {

        this.power = boundedValue(this.power + action.power, 0, 4)
        this.rotate = boundedValue(this.rotate + action.rotate, -90, 90)

        val newXSpeed = (this.xSpeed + this.power * X_VECTOR[this.rotate]!!)
        val newYSPeed = (this.ySpeed + this.power * Y_VECTOR[this.rotate]!!) - MARS_GRAVITY

        this.x += (this.xSpeed + newXSpeed) * 0.5
        this.y += (this.ySpeed + newYSPeed) * 0.5

        this.xSpeed = newXSpeed
        this.ySpeed = newYSPeed

        this.fuel -= power
        path.add(this.x to this.y)
    }

    fun play(actions: Array<Action>, surface: Surface, speedMax: Double, speedWeight: Double): Double {

        var lastX: Double
        var lastY: Double
        var crossing: Pair<Segment, Point>? = null
        for (action in actions) {
            lastX = x
            lastY = y
            play(action)
            crossing = surface.cross(Segment(Point(lastX, lastY), Point(x, y)))
            if (crossing != null || x.toInt() !in (0..surface.width) || y.toInt() !in (0..surface.height)) {
                break
            }
        }

        val xSpeedDist = max(xSpeed.absoluteValue - 20, 0.0)
        val ySpeedDist = max(ySpeed.absoluteValue - 40, 0.0)
        val rotateDist = rotate.absoluteValue
        val rotateMax = 90

        normalizedSpeed = (speedMax - xSpeedDist - ySpeedDist) * 100.0 / speedMax
        normalizedRotate = (rotateMax - rotateDist) * 100.0 / rotateMax


        if ( crossing?.first?.isLandingZone == true) {
            status = CrossingEnum.LANDING_ZONE
            return 100.0 + normalizedRotate * 0.5 + normalizedSpeed * 0.5
        }

        val distance = if (crossing != null) {
            status = CrossingEnum.CRASH
            crossing.first.distanceToLanding(crossing.second.x)
        } else {
            val projection = Segment(Point(x, y), Point(x, 0.0))
            val crossings = surface.segments
                .mapNotNull { segment -> surface.cross(segment, projection)?.let { segment to it } }
            var yCrossing = 0.0
            crossing = null
            for (cross in crossings) {
                val (_, point ) = cross
                if (point.y < y && point.y > yCrossing) {
                    yCrossing = point.y
                    crossing = cross
                }
            }
            boundedValue(
                y - yCrossing + (crossing?.first?.distanceToLanding(crossing.second.x) ?: surface.distanceMax),
                0.0,
                surface.distanceMax
            )
        }

        normalizedDistance = (surface.distanceMax - distance) * 100 / surface.distanceMax

        return normalizedDistance * (1-speedWeight) + normalizedSpeed * speedWeight
    }
}