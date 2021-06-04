package fr.vco.codingame.contest.springchallenge2021.mcts

import fr.vco.codingame.contest.springchallenge2021.*
//import fr.vco.codingame.contest.springchallenge2021.PoolState
//import fr.vco.codingame.contest.springchallenge2021.mcts.PoolState
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

class MctsNode(
    val parent: MctsNode?,
    val state: State,
    var action: Action? = null

) {

    var children: List<MctsNode> = emptyList()
//    val actions by lazy { state.getAvailableActions() }
    var win: Int = 0
    var visit: Int = 0

    fun getRandomChild(): MctsNode {
        return children[Random.nextInt(children.size)]
    }
}

object Mcts {
    var createdNodes = 0
//    var maxExecutionTime = 0L
    var executionTime=0L
    var totalSimulation = 0
    private val simulationState = State()
    lateinit var rootNode : MctsNode

    fun findNextMove(game: Game, timeout: Int = 90): Action {
        val rootState = State().initFromGame(game)
        return findNextMove(rootState, timeout)
    }

    fun findNextMove(state: State, timeout: Int = 90): Action {
        val start = System.currentTimeMillis()
        val end = System.currentTimeMillis() + timeout

        rootNode = MctsNode(null, state,WaitAction(ME))
        createdNodes = 1
        totalSimulation = 0
        while (System.currentTimeMillis() < end ) {

            val promisingNode = selectPromisingNode(rootNode)

            if (promisingNode.state.getStatus() == IN_PROGRESS) {
                expand(promisingNode)
            }

            val nodeToExplore =
                if (promisingNode.children.isNotEmpty()) promisingNode.getRandomChild() else promisingNode
            val winner = simulateRandomGame(nodeToExplore)

            backPropagation(nodeToExplore, winner)
        }

//        if (timeout < 100) maxExecutionTime = max(maxExecutionTime, System.currentTimeMillis() - start)
        executionTime = System.currentTimeMillis() - start
        log("MCTS Execution Time : ${executionTime}")
        log("Total simulation : ${rootNode.visit}")
        log("Total Node created : $createdNodes")
        log("Total victory : ${rootNode.win}")


        val bestNode = rootNode.children.maxByOrNull { it.visit }
        return bestNode?.action
         ?: WaitAction(ME)

    }

    fun summary() = "${rootNode.win}/${rootNode.visit} -- ${executionTime}ms"

    private fun selectPromisingNode(node: MctsNode): MctsNode {
        var bestNode = node
        while (bestNode.children.isNotEmpty()) {
            val parentVisit = ln(bestNode.visit.toDouble())
            bestNode = bestNode.children.maxByOrNull { uct(it, parentVisit) }!!
            //bestNode = bestNode.children.maxByOrNull { if (it.visit ==  0 ) Double.MAX_VALUE else it.win.toDouble() / bestNode.visit.toDouble() }!!
        }
        return bestNode
    }

    private fun uct(node: MctsNode, totalVisit: Double): Double {
        if (node.visit == 0) return Double.MAX_VALUE
        return node.win.toDouble() / node.visit.toDouble() + 1.41* sqrt(totalVisit / node.visit.toDouble())
    }


    private fun expand(node: MctsNode) {

        node.children = node.state.getAvailableActions().map {
            MctsNode(node, node.state.getNextState(it), it)
        }
        createdNodes += node.children.size

    }

    private fun backPropagation(node: MctsNode, winner: Int) {
        var current: MctsNode? = node
        while (current != null) {
            current.visit++
            if (winner == current.action!!.player) {
                current.win++
            }
            current = current.parent
        }

    }


    fun simulateRandomGame(node: MctsNode): Int {
        simulationState.initFromState(node.state)
        return simulationState.simulateRandomGame()
    }


}