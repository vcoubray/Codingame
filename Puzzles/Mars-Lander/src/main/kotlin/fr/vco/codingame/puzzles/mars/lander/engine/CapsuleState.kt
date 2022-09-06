package fr.vco.codingame.puzzles.mars.lander.engine

import fr.vco.codingame.puzzles.mars.lander.MARS_GRAVITY
import fr.vco.codingame.puzzles.mars.lander.ROTATE_RANGE
import fr.vco.codingame.puzzles.mars.lander.algorithm.FitnessResult
import kotlin.math.absoluteValue
import kotlin.math.max


data class CapsuleState(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var xSpeed: Double = 0.0,
    var ySpeed: Double = 0.0,
    var fuel: Int = 0,
    var rotate: Int = 0,
    var power: Int = 0,
) {
    fun loadFrom(state: CapsuleState) {
        x = state.x
        y = state.y
        xSpeed = state.xSpeed
        ySpeed = state.ySpeed
        fuel = state.fuel
        rotate = state.rotate
        power = state.power
    }
}


fun CapsuleState.play(action: Action) {

    this.power = boundedValue(this.power + action.power, 0, 4)
    this.rotate = boundedValue(this.rotate + action.rotate, -90, 90)

    val newXSpeed = (this.xSpeed + this.power * X_VECTOR[this.rotate]!!)
    val newYSPeed = (this.ySpeed + this.power * Y_VECTOR[this.rotate]!!) - MARS_GRAVITY

    this.x += (this.xSpeed + newXSpeed) * 0.5
    this.y += (this.ySpeed + newYSPeed) * 0.5

    this.xSpeed = newXSpeed
    this.ySpeed = newYSPeed

    this.fuel -= power
}

fun CapsuleState.play(actions: Array<Action>, surface: Surface): FitnessResult {

    var lastX: Double
    var lastY: Double
    var lastRotate = rotate
    var distance: Double = -1.0

    var i = 0
    for (action in actions) {
        lastX = x
        lastY = y
        lastRotate = rotate
        play(action)
        distance = surface.cross(lastX, lastY, x, y)
        if (distance >= 0 || x !in surface.widthRange || y !in surface.heightRange) {
            break
        }
        i++
    }

    if (distance >= 0.0) {
        if (distance == 0.0) {
            if (lastRotate in ROTATE_RANGE) rotate = 0
            actions[i].rotate = -lastRotate
        }
    } else if (x in surface.widthRange && y in surface.heightRange) {
        for (segment in surface.segments) {
            if (x in segment.xRange) {
                val crossingY = segment.start.y + (x - segment.start.x) / segment.vx * segment.vy
                if (crossingY < y) {
                    val yDist = y - crossingY
                    distance = boundedValue(
                        yDist * yDist + segment.distanceToLanding(x),
                        0.0,
                        surface.distanceMax
                    )
                    break
                }
            }
        }
    } else {
        distance = surface.distanceMax
    }

    return FitnessResult(
        distance = distance,
        xSpeedOverflow = max(xSpeed.absoluteValue - 20, 0.0),
        ySpeedOverflow = max(ySpeed.absoluteValue - 40, 0.0),
        rotateOverflow = max(lastRotate.absoluteValue - 10, 0),
    )
}