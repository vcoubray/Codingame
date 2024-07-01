package fr.vco.codingame.contests.summerchallenge2024

import kotlin.math.min

class Diving(playerIndex: Int) : Game(playerIndex) {

    var objective = GAME_OVER
    var points = listOf<Int>()
    var combos = listOf<Int>()
    var maxOppScore = 0.0

    override fun updateGame(gpu: String, registry: List<Int>) {
        objective = gpu
        points = registry.take(3)
        combos = registry.subList(3, 6)
    }

    override fun evalScores() {
        if(objective == GAME_OVER) {
            emptyScore()
            return
        }
        
        maxOppScore = opponents.maxOf{maxPoints(points[it], combos[it], objective.length)}.toDouble()
        play(points[me], combos[me], 0,0, SIMU_DEEP)
    }

    private fun play(point: Int, combo: Int, pos: Int, actions: Int, deep: Int) {
        
        if (deep == 0) {
            scores[actions] = min(point.toDouble(), maxOppScore)
            return
        }
      
        for (action in 0 until 4) {
            var newPoint = point
            var newCombo = combo
    
            if(pos < objective.length) {
                if(objective[pos] == ACTIONS[action].first()) {
                    newCombo = combo + 1
                    newPoint = point + combo
                } else {
                    newCombo = 0
                }
            }
          
            play(newPoint, newCombo, pos+1,actions * 4 + action, deep - 1)
        }
    }

    override fun normalize() {
        val min = scores.min()
        val max = scores.max()
        log("$min - $max")

        val dist = max - min
        if(dist == 0.0) return
        for (i in scores.indices) {
            scores[i] = (scores[i] - min) / dist
        }
    }
    
    private fun maxPoints(points: Int, combos: Int, remainingTurn: Int): Int {
        return points + (combos + combos + remainingTurn - 1) * remainingTurn / 2
    }

}