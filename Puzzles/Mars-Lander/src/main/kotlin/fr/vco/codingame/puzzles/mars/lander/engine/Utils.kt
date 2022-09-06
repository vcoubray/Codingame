package fr.vco.codingame.puzzles.mars.lander.engine

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
