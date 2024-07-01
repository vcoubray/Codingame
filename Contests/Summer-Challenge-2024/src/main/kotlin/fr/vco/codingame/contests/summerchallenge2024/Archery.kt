package fr.vco.codingame.contests.summerchallenge2024

import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

data class Position(val x: Int, val y: Int) {
    operator fun plus(other: Position) = Position(x + other.x, y + other.y)
    operator fun times(scalar: Int) = Position(x * scalar, y * scalar)
    fun distanceToOrigin() = x * x + y * y
    fun distanceToOriginManhattan() = x.absoluteValue + y.absoluteValue
}

val DIRECTIONS = mapOf(
    "LEFT" to Position(-1, 0),
    "RIGHT" to Position(1, 0),
    "UP" to Position(0, -1),
    "DOWN" to Position(0, 1)
)

class Archery(me: Int) : Game(me) {

    class Player(
        var pos: Position = Position(0, 0),
        var score: Int = 0,
    )

    var winds: List<Int> = emptyList()
    val players = List(3) { Player() }

    override fun updateGame(gpu: String, registry: List<Int>) {
        winds = if (gpu == GAME_OVER) emptyList() else gpu.map { it.digitToInt() }

        registry.take(6)
            .chunked(2)
            .map { (x, y) -> Position(x, y) }
            .forEachIndexed { i, pos ->
                players[i].pos = pos
            }
    }

    override fun normalize() {
        val min = scores.min()
        val max = scores.max()
        log("$min - $max")

        val dist = max - min
        if(dist == 0.0) {
            emptyScore()
            return
        }
        for (i in scores.indices) {
            scores[i] = 1.0 - (scores[i] - min) / dist
        }
    }

    override fun evalScores() {
        if(winds.isEmpty()) {
            emptyScore()
            return
        }
        play(players[me].pos, 0, 0, SIMU_DEEP)
    }



    private fun play(pos: Position, turn: Int, actions: Int, deep: Int) {
        if(deep == 0 ) {
            scores[actions] = pos.distanceToOriginManhattan().toDouble()
            return
        }

        for (action in 0 until 4) {
            var newPos = pos
            if(turn < winds.size) {
                newPos = pos + (DIRECTIONS[ACTIONS[action]]!! * winds[turn])
                newPos = truncate(newPos)
            }
            play (newPos, turn + 1 , actions * 4 + action, deep - 1 )
        }
        
    }

    fun truncate(pos: Position): Position {
        return Position(
            min(20, max(-20, pos.x)),
            min(20, max(-20, pos.y))
        )
    }
}