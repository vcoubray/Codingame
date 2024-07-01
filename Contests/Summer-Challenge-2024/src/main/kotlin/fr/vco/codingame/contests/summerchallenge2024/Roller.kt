package fr.vco.codingame.contests.summerchallenge2024

class Roller(me: Int) : Game(me) {

    lateinit var riskOrder: String
    var turn: Int = 0
    var risks = listOf<Int>()
    var positions = listOf<Int>()

    override fun updateGame(gpu: String, registry: List<Int>) {
        riskOrder = gpu
        positions = registry.take(3)
        risks = registry.subList(3, 6)
        turn = registry.last().toInt()
    }

    override fun evalScores() {

    }

    override fun normalize() {

    }
}