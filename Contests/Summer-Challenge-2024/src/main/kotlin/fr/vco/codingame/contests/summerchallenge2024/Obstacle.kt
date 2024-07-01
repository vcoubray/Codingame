package fr.vco.codingame.contests.summerchallenge2024

import kotlin.math.ceil
import kotlin.math.roundToInt

class Obstacle(me: Int) : Game(me) {

    class Player(
        var pos: Int = 0,
        var stuns: Int = 0,
    )

    val DISTANCE = listOf(1, 3, 2, 2)
    val bestRun = MutableList(30 + SIMU_DEEP) { 0 }
    val players = List(3) { Player() }
    var track: String = ""

    override fun updateGame(gpu: String, registry: List<Int>) {
        track = gpu
    
        players.forEachIndexed { i, p ->
            p.pos = registry[i]
            p.stuns = registry[i + 3]
        }
        repeat(SIMU_DEEP) {
            bestRun[track.length + it] = -it - 1
        }
        for (i in (0 until track.length)) {
            bestRun[i] = minTurn(i, 0)
        }
    }

    private fun minTurn(pos: Int, stuns: Int): Int {
        val segments = track.drop(pos + 1).split("#.").map { it.length }
        val turns = segments.size - 1 + segments.sumOf { ceil((it) / 3.0).roundToInt() } + stuns
        return turns
    }

    override fun normalize() {
        val min = scores.min()
        val max = scores.max()
        log("$min - $max")

        val dist = max - min
        if(dist == 0.0) return
        for (i in scores.indices) {
            scores[i] = 1.0 - (scores[i] - min) / dist
        }
    }

    override fun evalScores() {
        if(track == GAME_OVER) {
            emptyScore()
            return
        }
        play(players[me].pos, players[me].stuns, 0, SIMU_DEEP)
    }

    private fun play(pos: Int, stuns: Int, actions: Int, deep: Int) {

        if (deep == 0) {
            scores[actions] = (bestRun[pos] + stuns).toDouble()
            return
        }
        val nextObstacle = track.drop(pos).indexOfFirst { it == '#' }.takeIf { it != -1 } ?: 4
        for (action in 0 until 4) {
            var newPos = pos
            var newStuns = stuns

            if (newPos >= 29) {
                newPos += 1
            } else if (stuns > 0) {
                newStuns--
            } else {
                if (nextObstacle == 1 && action == UP) {
                    newPos += 2
                } else {
                    if (DISTANCE[action] >= nextObstacle) {
                        newPos = pos + nextObstacle
                        newStuns = 2
                    } else {
                        newPos = pos + DISTANCE[action]
                        if (newPos > 29) newPos = 29
                    }
                }
            }
            play(newPos, newStuns, actions * 4 + action, deep - 1)
        }
    }
}