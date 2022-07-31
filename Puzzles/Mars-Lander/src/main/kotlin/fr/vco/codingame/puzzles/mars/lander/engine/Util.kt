package fr.vco.codingame.puzzles.mars.lander.engine

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun toRadians(value: Double) = value * PI / 180
val Y_VECTOR = (-90..90).associateWith { cos(toRadians(it.toDouble())) }
val X_VECTOR = (-90..90).associateWith { -sin(toRadians(it.toDouble())) }

fun boundedValue(value: Int, min: Int, max: Int) = when {
    value <= min -> min
    value >= max -> max
    else -> value
}

fun boundedValue(value: Double, min: Double, max: Double) = when {
    value <= min -> min
    value >= max -> max
    else -> value
}

