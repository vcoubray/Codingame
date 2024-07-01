package fr.vco.codingame.contests.summerchallenge2024

abstract class Game(val me: Int) {
    val opponents = (0..2).filterNot { it == me }
    val scores = DoubleArray(COMBINATION_COUNT) { 0.0 }

    abstract fun updateGame(gpu: String, registry: List<Int>)
    abstract fun evalScores()
    abstract fun normalize()
    
    fun update(input: String) {
        val inputs = input.split(" ")
        updateGame(inputs.first(), inputs.drop(1).map { it.toInt() })
    }

    fun computeScore() {
        evalScores()
        normalize()
    }

    fun emptyScore(){
        for (i in scores.indices) {
            scores[i] = 0.0
        }
    }

}

