package fr.vco.codingame.puzzles.mars.lander.algorithm


data class FitnessResult(
    var distance: Double,
    var xSpeedOverflow: Double,
    var ySpeedOverflow: Double,
    var rotateOverflow: Int
)
