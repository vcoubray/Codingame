package fr.vco.codingame.puzzles.voxcodei.episode2

import kotlin.collections.sortedBy
import kotlin.math.max
import kotlin.math.min
import kotlin.system.measureTimeMillis
import kotlin.time.DurationUnit
import kotlin.time.toDuration


data class Action(val turn: Int, val pos: Int, val nodesInRange: Long =0L)

/**
 * 1 - Group every actions by their explosions (the nodes they destroy)
 * 2 - Then find a valid combination of explosion within the bomb count
 * 3 - For each valid combination of explosion; find a valid combination of actions
 * 4 - Return the first valid combination of actions
 *
 */
class GameResolver(val simulator: GameSimulator) {

    private val groupedByExplosion = simulator.rounds
        .flatMapIndexed { turn, round ->
            round.actions.map{(pos, nodes) -> nodes to Action(turn - 3, pos )}.sortedBy { (_,action) -> action.turn }
        }
        .filter{(_, action) -> action.turn >= 0}
        .filter{(_, action) -> simulator.rounds[action.turn].grid[action.pos] == -1}
        .groupBy{ (node, _) -> node }
        .map{(key, value) -> key to value.map{it.second}}
        .toMap()

    private val sortedExplosions = groupedByExplosion.keys.sortedDescending()


    fun resolve(): List<Int> {
        measureTimeMillis {
            findValidExplosionCombination(0L, simulator.bombs)
        }.let{System.err.println("Resolved in ${it.toDuration(DurationUnit.MILLISECONDS)} ")}
        return usedPositions
    }

    private val selectedExplosions = MutableList(sortedExplosions.size) { false }
    private var currentExplosion = 0
    private val nodeMask = simulator.nodeMask

    /**
     * find recursively a valid combination of node explosions and
     * check if this combination has a valid combination of actions
     */
    private fun findValidExplosionCombination(
        nodes: Long,
        bombs: Int
    ): Boolean {
        if (bombs == 0) return false
        if (currentExplosion == sortedExplosions.size) {
            return false
        }

        if (sortedExplosions[currentExplosion] or nodes == nodeMask) {
            selectedExplosions[currentExplosion] = true
            resetSelectedActions( )
            if(findValidActionCombinations()) {
                return true
            } else {
                selectedExplosions[currentExplosion] = false
                return false
            }
        }
        if (sortedExplosions[currentExplosion] or nodes == nodes) {
            currentExplosion++
            val result = findValidExplosionCombination( nodes, bombs)
            currentExplosion--
            return result
        } else {
            val explodedNodes = nodes or sortedExplosions[currentExplosion]
            selectedExplosions[currentExplosion] = true
            currentExplosion++
            val result = findValidExplosionCombination(explodedNodes, bombs - 1)
            currentExplosion--
            if (result) {
                return true
            } else {
                selectedExplosions[currentExplosion] = false
            }
            currentExplosion++
            val result2 = findValidExplosionCombination(nodes, bombs)
            currentExplosion--
            return result2
        }
    }


    private val usedPositions = MutableList(simulator.rounds.size){-1}
    private var currentAction = 0
    private val actionCombinations = MutableList<List<Action>>(simulator.bombs){emptyList()}
    private var actionsCount = 0


    private fun resetSelectedActions ( ){
        actionsCount = 0
        repeat(selectedExplosions.size) {
            if(selectedExplosions[it]) {
                this.actionCombinations[actionsCount] = groupedByExplosion[sortedExplosions[it]]!!
                actionsCount++
            }
        }
    }

    /**
     * Find recursively a valid combination of action for the selected explosions combination
     */
    private fun findValidActionCombinations(): Boolean {
        if(currentAction == actionsCount) return true

        val currentActions = actionCombinations[currentAction]
        for (currentPos in currentActions) {
            if(isValidAction(currentPos)){
                usedPositions[currentPos.turn] = currentPos.pos
                currentAction++
                val result = findValidActionCombinations()
                currentAction--
                if(result) return true
                usedPositions[currentPos.turn] = -1
            } else {
                continue
            }
        }
        return false
    }

    private fun isValidAction(action: Action) : Boolean{
        if(usedPositions[action.turn] != -1) return false
        for (turn in max(0, action.turn-3) until min(action.turn+3, simulator.rounds.size) ) {
            if(usedPositions[turn] != 1 && simulator.ranges[turn].contains(action.pos)) {
                return false
            }
        }
        return true
    }

}
