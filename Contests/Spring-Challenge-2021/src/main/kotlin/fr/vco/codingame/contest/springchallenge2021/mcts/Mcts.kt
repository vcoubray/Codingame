package fr.vco.codingame.contest.springchallenge2021.mcts

import fr.vco.codingame.contest.springchallenge2021.*
import kotlin.math.ln
import kotlin.math.sqrt

var LOG_NEP = MutableList(10000) { ln(it.toFloat()) }


class MctsNode(
    val parent: MctsNode?,
    var action: Action
) {
    var children: List<MctsNode> = emptyList()
    var win: Float = 0f
    var visit: Int = 0

    fun getRandomChild() = children.random()
}

object Mcts {
    var createdNodes = 0

    var executionTime = 0L
    var totalSimulation = 0
    private val simulationState = State()
    private val rootState = State()
    lateinit var rootNode: MctsNode
    lateinit var bestNode: MctsNode

    fun findNextMove(game: Game, timeout: Int = 90): Action {
        rootState.loadFromGame(game)
        return findNextMove(rootState, timeout)
    }

    private fun findNextMove(state: State, timeout: Int = 90): Action {
        val start = System.currentTimeMillis()
        val end = System.currentTimeMillis() + timeout

        rootNode = MctsNode(null, WaitAction(ME))
        createdNodes = 1
        totalSimulation = 0
        while (System.currentTimeMillis() < end) {

            simulationState.loadFromState(rootState)

            // 1 - Selection
            val promisingNode = selectPromisingNode(rootNode)

            // 2 - Expansion
            if (simulationState.getStatus() == IN_PROGRESS) {
                expand(promisingNode)
            }

            val nodeToExplore = if (promisingNode.children.isNotEmpty()) {
                val child = promisingNode.getRandomChild()
                simulationState.play(child.action)
                child
            } else promisingNode

            // 3 - Simulation
            val winner = simulateRandomGame()

            // 4 - BackPropagation
            backPropagation(nodeToExplore, winner)
        }


        executionTime = System.currentTimeMillis() - start
        log("MCTS Execution Time : ${executionTime}")
        log("Total simulation : ${rootNode.visit}")
        log("Total Node created : $createdNodes")
        log("Total victory : ${rootNode.win}")


        bestNode = rootNode.children.maxByOrNull { it.visit } ?: rootNode
        return bestNode.action
    }

    fun summary() = "${bestNode.win} ${rootNode.visit} ${executionTime}ms"

    private fun selectPromisingNode(node: MctsNode): MctsNode {
        var bestNode = node
        while (bestNode.children.isNotEmpty()) {
            val parentVisit = LOG_NEP.getOrElse(bestNode.visit) { ln(it.toFloat()) }
            bestNode = bestNode.children.maxByOrNull { uct(it, parentVisit) }!!
            //bestNode = bestNode.children.maxByOrNull { if (it.visit ==  0 ) Double.MAX_VALUE else it.win.toDouble() / bestNode.visit.toDouble() }!!
            simulationState.play(bestNode.action)
        }
        return bestNode
    }

    private fun uct(node: MctsNode, totalVisit: Float): Float {
        if (node.visit == 0) return Float.MAX_VALUE
        return node.win / node.visit + sqrt(totalVisit / node.visit)
    }

    private fun expand(node: MctsNode) {
        node.children = simulationState.getAvailableActions().map { MctsNode(node, it) }
        createdNodes += node.children.size
    }

    private fun backPropagation(node: MctsNode, winner: Int) {
        var current: MctsNode? = node

        while (current != null) {
            current.visit++
            current.win += when (winner) {
                current.action.player -> 1f
                DRAW -> 0.5f
                else -> 0f
            }
            current = current.parent
        }

    }

    fun simulateRandomGame(): Int {
        return simulationState.simulateRandomGame()
    }


}