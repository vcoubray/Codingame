package fr.vco.codingame.puzzles.mars.lander

/** Puzzle **/
const val HEIGHT = 3000
const val WIDTH = 7000
const val MARS_GRAVITY = 3.711
val ROTATE_RANGE = -15..15
val POWER_RANGE = -1..1

const val TIMEOUT = 900L //ms

/** Algorithm **/
const val POPULATION_SIZE = 80
const val POPULATION_MIDDLE = POPULATION_SIZE / 2
const val CHROMOSOME_SIZE = 180
const val MUTATION_PROBABILITY = 0.02
const val ELITISM = 0.2
const val ELITE_SIZE = (POPULATION_SIZE * ELITISM * 2 + 1 ).toInt() / 2  // Make sure ELITE_SIZE is always even
const val CHILDREN_SIZE = POPULATION_SIZE - ELITE_SIZE

/** Score computation **/
const val SPEED_MAX = 100.0
const val X_SPEED_WEIGHT = 30.0
const val Y_SPEED_WEIGHT = 50.0
const val ROTATE_WEIGHT = 10.0
const val DISTANCE_WEIGHT = 110.0